package com.mall.module.aftersale.service;

import com.mall.common.PageResult;
import com.mall.module.aftersale.vo.AfterSaleVO;

public interface AfterSaleService {

    /** 申请售后（整单） */
    Long apply(Long userId, Long orderId, String reason, String description);

    /** 我的售后列表 */
    PageResult<AfterSaleVO> pageMine(Long userId, Integer pageNum, Integer pageSize);

    /** 售后详情（校验归属） */
    AfterSaleVO detail(Long userId, Long id);

    /** 撤销申请（仅待审核可撤销） */
    void cancel(Long userId, Long id);

    /** 后台：售后列表 */
    PageResult<AfterSaleVO> adminPage(Integer status, Integer pageNum, Integer pageSize);

    /** 后台：审核（pass=true 通过并退款） */
    void audit(Long id, boolean pass, String remark);
}
