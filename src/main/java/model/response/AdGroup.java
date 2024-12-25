package model.response;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/29
 */
@Data
public class AdGroup {

    private String profile_id;

    private String campaign_id; // 广告组id

    private Long ad_group_id; // 广告组id

    /**
     * 名称
     */
    private String name;

    private String ads_type;

    /**
     * 曝光量
     */
    private int impressions;

    /**
     * 点击
     */
    private int clicks;

    /**
     * 点击百分比
     */
    private String clicks_percent;

    /**
     * ctr
     */
    private String ctr;

    /**
     * cpc
     */
    private String cpc;

    /**
     * 花费
     */
    private String spends;

    /**
     * 花费百分比
     */
    private String spends_percent;

    /**
     * 销售额
     */
    private String sales;

    /**
     * 销售额百分比
     */
    private String sales_percent;

    /**
     * 直接销售额
     */
    private String direct_sales;

    /**
     * acos
     */
    private String acos;

    /**
     * 广告订单
     */
    private String ad_units;

    /**
     * 直接成交订单
     */
    private int direct_orders;

    /**
     * cpa
     */
    private String cpa;

    /**
     * cvr
     */
    private String cvr;

    /**
     * 广告单笔价
     */
    private String unit_price;

    /**
     * 状态,paused
     */
    private String state;

    /**
     * 国家信息
     */
    private String store_country;

    /**
     * bid
     */
    private String default_bid;
}
