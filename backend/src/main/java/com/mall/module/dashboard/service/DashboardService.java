package com.mall.module.dashboard.service;

import com.mall.module.dashboard.vo.DashboardVO;

public interface DashboardService {

    /** 数据看板聚合（近 range 天） */
    DashboardVO overview(int range);
}
