package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class AdPlacementOperateRequestSingle {

    private String bid;

    private Long targetId; // 其他

    private Integer is_base_value;

    private String state;

    private Long keywordId;// 关键词
}
