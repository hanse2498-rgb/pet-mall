package com.mall.module.dashboard.mapper;

import com.mall.module.dashboard.vo.CategoryShareVO;
import com.mall.module.dashboard.vo.DashboardKpiVO;
import com.mall.module.dashboard.vo.StatusCountVO;
import com.mall.module.dashboard.vo.StockAlertVO;
import com.mall.module.dashboard.vo.TopProductVO;
import com.mall.module.dashboard.vo.TrendPointVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DashboardMapper {

    /** 核心指标 */
    DashboardKpiVO kpi(@Param("start") LocalDateTime start);

    /** 销售趋势（按天，仅已支付含退款） */
    List<TrendPointVO> salesTrend(@Param("start") LocalDateTime start);

    /** 订单状态分布（全部订单） */
    List<StatusCountVO> statusDist();

    /** 分类销售占比（区间内已支付订单项） */
    List<CategoryShareVO> categoryShare(@Param("start") LocalDateTime start);

    /** 热销商品 Top N */
    List<TopProductVO> topProducts(@Param("start") LocalDateTime start, @Param("limit") int limit);

    /** 库存预警 */
    List<StockAlertVO> stockAlerts(@Param("threshold") int threshold);
}
