package com.sky.mapper;

import com.sky.dto.GoodsSalesDTO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /**
     * 指定日期营业额统计
     */
    Double sumByMap(Map<String, Object> map);

    /**
     * 查询订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    Integer countOrderByDateAndStatus(LocalDateTime begin, LocalDateTime end, Integer status);

    /**
     * 统计前十商品销量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    List<GoodsSalesDTO> top10(LocalDate begin, LocalDate end, Integer status);


    /**
     * 用户数量统计
     * @param beginTime
     * @param endTime
     * @return
     */
    Integer countUserByDate(LocalDateTime beginTime, LocalDateTime endTime);
}
