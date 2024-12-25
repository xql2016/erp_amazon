package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class AdGroupChangeBidRequest {

    private Long adGroupId;

    private long profileId;

    private double changeToBid;
}
