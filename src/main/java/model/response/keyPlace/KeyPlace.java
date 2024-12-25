package model.response.keyPlace;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class KeyPlace {
    private Double bid;// bid竞价
    private Double real_bid;
    private Double cpc;// cpc价
    private String acos;// acos
    private Integer clicks;// 点击数
    private String department_id;
    private Long profile_id;
    private String country;
    private Long campaign_id;
    private Long ad_group_id;
    private Long keyword_id;
    private String keyword_text;
    private String state;
    private String match_type;
    private String created_at_api;
    private String serving_status;
    private String service_status;
    private String creation_date;
    private String searchrank;
    private String key;
    private String impressions;
    private String orders;
    private Double spends;
    private Double sales;
    private String cpa;
    private Double ctr;
    private Double cvr;
    private String roas;
    private String direct_unit_price;
    private String indirect_unit_price;
    private String direct_sales;
    private String direct_orders;
    private String indirect_sales;
    private String indirect_orders;
    private String indirect_orders_of_orders_percent;
    private String ad_units;
    private String direct_units;
    private String indirect_units;
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
    private String month_searchs;
    private String purchase;
    private String competing_num;
    private String snapshot_id;
    private String bidding_strategy;
    private String setting_id;
    private String local_queue_at;
    private String convert_sp_rank;
    private String convert_sr_rank;
    private String ad_console_id;
    private String ad_id_match;
    private String asin_match;
    private String asins;
    private String source;
    private String real_rank;
    private String is_campaign_apply_time;
    private String is_ad_group_apply_time;
    private String is_apply_time;
    private String campaign_applied_templates;
    private String ad_group_applied_templates;
    private String applied_templates;
    private String entity_level_hash;
    private String is_apply_rule;
    private String is_apply_grab;
    private String meta_uuid;
    private String st_negative;
    private String st_negatives;
    private String kw_negative;
    private String kw_negatives;
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
    private String theme_recommends;
    private String star;
}
