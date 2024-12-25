package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class AdPlacementOperateRequest {

    private long profile_id; // 1769812645248266

    private String token;

    private String api_method; // "put_targets"-category/自动,"put_keywords"-关键词

    private String api_version; // "v3"

    private String ad_type; // "sp"

    private int one_more; // 0

    /**
     * "{"targetingClauses":[{"bid":"0.30","targetId":57863557488741,"is_base_value":0}]}"category/自动/ASIN
     * "{"keywords":[{"bid":"0.35","keywordId":215155958066291,"is_base_value":0}]}"关键词
     * "{"targetingClauses":[{"state":"paused"/"enabled","targetId":57863557488741}]}"category/自动/ASIN
     * "{"keywords":[{"state":"paused"/"enabled","keywordId":57863557488741}]}"关键词
     * params[targetingClauses][0][state] = "paused"/"enabled", params[targetingClauses][0][targetId] = 57863557488741
     */
    private String params;
}
