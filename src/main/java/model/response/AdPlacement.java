package model.response;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/30
 */
@Data
public class AdPlacement {

    /**
     * 关键词类型的投放的名称
     */
    private String keyword_text;

    private long keyword_id;

    /**
     * 自动类型/category类型/asin类型的投放的名称
     */
    private String targeting_text;

    private String targeting_text_zh;

    private long target_id;

    private String ad_group_id;

    private String ad_group_name;

    private String ad_group_state;

    private String acos;

    private String bid;

    private Double real_bid;

    private String campaign_id;

    private String campaign_name;

    private String campaign_state;

    private String country;

    private String cpa;

    private String cpc;

    private int ad_units;

    private int direct_orders;

    private int orders;

    private int clicks;

    /**
     * 状态,paused
     */
    private String state;

    private Double default_bid;


}
