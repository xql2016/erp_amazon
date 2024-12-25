package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class AdGroupOperateRequestSingle {

    private double defaultBid;

    private Long adGroupId;

    private int is_base_value;
}
