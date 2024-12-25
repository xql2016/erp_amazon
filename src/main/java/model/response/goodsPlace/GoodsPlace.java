package model.response.goodsPlace;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class GoodsPlace {
    private Double bid;// bid竞价
    private Double real_bid;
    private Double cpc;// cpc价
    private String acos;// acos
    private Integer clicks;// 点击数
    private String department_id;
    private Long profile_id;
    private Long campaign_id;
    private Long ad_group_id;
    private Long target_id;
    private String expression_type;
    private String expression;
    private String state;
    private String created_at_api;
    private String creation_date;
    private String serving_status;
    private String service_status;
    private String key;
    private Integer impressions;
    private Integer orders;
    private Double spends;
    private Double sales;
    private String cpa;
    private Double ctr;
    private Double cvr;
    private String roas;
    private String unit_price;
    private String direct_unit_price;
    private String indirect_unit_price;
    private String direct_sales;
    private Integer direct_orders;
    private String indirect_sales;
    private Integer indirect_orders;
    private String indirect_orders_of_orders_percent;
    private Integer ad_units;
    private Integer direct_units;
    private Integer indirect_units;
    private String top_of_search_impression_share;
    private String campaign_name;
    private String campaign_state;
    private String targeting_type;
    private String bidding;
    private Long portfolio_id;
    private String portfolio_name;
    private String networks;
    private String ad_group_name;
    private String ad_group_state;
    private Double default_bid;
    private String tags;
    private Boolean is_campaign_apply_time;
    private Boolean is_ad_group_apply_time;
    private Boolean is_apply_time;
    private String entity_level_hash;
    private String bidding_strategy;
    private Boolean is_apply_rule;
    private String meta_uuid;
    private String impressions_percent;
    private String orders_percent;
    private String clicks_percent;
    private String sales_percent;
    private String spends_percent;
    private String oaOrders_percent;
    private String oaSales_percent;
    private String view_impressions_percent;
    private String ad_units_percent;
    private String direct_sales_percent;
    private String indirect_sales_percent;
    private String direct_orders_percent;
    private String indirect_orders_percent;
    private String direct_units_percent;
    private String indirect_units_percent;
    private String daily_budget;
    private String budget;
    private String recommends;
    private String expression_zh;
    private String exp_type;
    private String exp_type_zh;
    private String exp_value;
    private String exp_value_zh;
    private String targeting_text;
    private String targeting_text_zh;
    private String exp_type_text;
    private Boolean is_remarking;
    private String kw_negative;
    private Boolean kw_is_ad_group_negative;
    private String kw_campaign_negative;
    private Boolean kw_is_campaign_negative;
    private String st_negative;
    private String st_campaign_negative;
    private Boolean st_is_ad_group_negative;
    private Boolean st_is_campaign_negative;
    private String img_src;
    private String asin_title;
    private Boolean is_has_image;
    private String asin_price;
    private Double asin_stars;
    private Integer asin_review_count;
    private String theme_recommends;
    private String star;

}
