package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/21
 */
@Data
public class AdGroupOperateRequest {

    private long profile_id; // 1769812645248266

    private String _token;

    private String api_method; // put_adGroups

    private String api_version; // "v3"

    private String ad_type; // "sp"

    /**
     * {"adGroups":[{"defaultBid":"0.16","adGroupId":477228857436742,"is_base_value":0}]}
     */
    private String params;
}
