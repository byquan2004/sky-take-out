package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;

    private final ReportMapper reportMapper;

    private final UserMapper userMapper;

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
            Integer newUser = reportMapper.countByStatus(beginTime,endTime);
            newUserList.add(newUser);
            // 统计截至目前总用户数量
            Integer totalUser = reportMapper.countByStatus(null,endTime);
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
     * 根据状态统计订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    private Integer getCountByStatus(LocalDateTime begin, LocalDateTime end, Integer status){
        return reportMapper.countByStatistic(begin, end,status);
    }
}
