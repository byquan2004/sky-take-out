package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.ReportMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final WorkspaceService workspaceService;

    private final ReportMapper reportMapper;

    /**
     * 指定日期营业额统计
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        ArrayList<Double> turnoverList = new ArrayList<>();
        // 日期内 已完成
        Map<String, Object> map = new HashMap<>();
        map.put("status",Orders.COMPLETED);

        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Double turnover = reportMapper.sumByMap(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        });
        return TurnoverReportVO.builder()
                // 字符串并用,分隔
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 获取日期集合
     * @param begin
     * @param end
     * @return
     */
    private static List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        // 日期集合
        List<LocalDate> dateList = new ArrayList<>();
        while (!begin.equals(end)){
            dateList.add(begin);
            begin = begin.plusDays(1);
        }
        return dateList;
    }

    /**
     * 用户数量统计
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        // 日期集合
        List<LocalDate> dateList = getDateList(begin, end);
        // 统计今日新用户数量
        List<Integer> newUserList = new ArrayList<>();
        // 统计截至目前总用户数量
        List<Integer> totalUsers = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            // 统计今日新用户数量
            Integer newUser = reportMapper.countUserByDate(beginTime,endTime);
            newUserList.add(newUser);
            // 统计截至目前总用户数量
            Integer totalUser = reportMapper.countUserByDate(null,endTime);
            totalUsers.add(totalUser);
        });

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUsers, ","))
                .build();
    }

    /**
     * 订单数量统计
     */
    @Override
    public OrderReportVO orderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        // 每日有效订单数列表
        List<Integer> validOrderCountList = new ArrayList<>();
        // 每日总订单数列表
        List<Integer> orderCountList = new ArrayList<>();
        dateList.forEach(date -> {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Integer validOrderCount = getCountByStatus(beginTime, endTime, Orders.COMPLETED);
            validOrderCountList.add(validOrderCount);
            Integer totalCount = getCountByStatus(beginTime, endTime, null);
            orderCountList.add(totalCount);
        });

        // 有效订单总数
        Integer totalValidOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        // 总订单数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        // 订单完成率
        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0) {
            orderCompletionRate = Double.valueOf(totalValidOrderCount) / Double.valueOf(totalOrderCount);
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCompletionRate(orderCompletionRate)
                .validOrderCount(totalValidOrderCount)
                .totalOrderCount(totalOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .build();
    }

    /**
     * 销量排名
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<GoodsSalesDTO> list = reportMapper.top10(begin,end,Orders.COMPLETED);
        String nameList = StringUtils.join(list.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()), ",");
        String numberList = StringUtils.join(list.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()), ",");
        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运行数据报表
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        // 计算三十天日期
        LocalDateTime begin = LocalDateTime.now().minusMonths(1);
        LocalDateTime end = LocalDateTime.now();
        BusinessDataVO businessData = workspaceService.getBusinessData(begin, end);
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"运营数据报表.xlsx\"");
            InputStream inp = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            if(inp == null){
                throw new RuntimeException("文件不存在");
            }
            XSSFWorkbook workbook = new XSSFWorkbook(inp);
            Sheet sheet1 = workbook.getSheet("Sheet1");
            sheet1.getRow(1).getCell(1).setCellValue("统计日期:"+begin.toLocalDate() +"至"+ end.toLocalDate());

            sheet1.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet1.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet1.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet1.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet1.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());

            Row row = sheet1.getRow(7);
            for (int i = 1; i <= 30; i++) {
                LocalDateTime dateTime = begin.plusDays(i);
                LocalDateTime beginTime = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MIN);
                LocalDateTime endTime = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.MAX);
                businessData = workspaceService.getBusinessData(beginTime, endTime);
                row.getCell(i).setCellValue(dateTime.toLocalDate().toString());
                row.getCell(i+1).setCellValue(businessData.getTurnover());
                row.getCell(i+2).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(i+3).setCellValue(businessData.getNewUsers());
                row.getCell(i+4).setCellValue(businessData.getValidOrderCount());
                row.getCell(i+5).setCellValue(businessData.getUnitPrice());
            }
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush(); // 确保数据被写入
            workbook.close();
            inp.close();
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * 根据状态统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getCountByStatus(LocalDateTime begin, LocalDateTime end, Integer status){
        return reportMapper.countOrderByDateAndStatus(begin, end,status);
    }
}
