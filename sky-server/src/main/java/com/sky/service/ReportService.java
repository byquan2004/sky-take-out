package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /**
     * 指定日期营业额统计
     */
    TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户数量统计
     */
    UserReportVO userStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单数量统计
     */
    OrderReportVO orderStatistics(LocalDate begin, LocalDate end);

    /**
     * 销量排名
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    /**
     * 导出运行数据报表
     * @param response
     */
    void export(HttpServletResponse response);
}
