package model.configuration.spamTrafficFromAdGroup;

import lombok.Data;

/**
 * 控制广告入口的垃圾流量
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupDoOperationDetail {

    private String spamTrafficAdGroupOperateType; // 操作类型
    private Double clickSubtractValue;
    private Double clickMultiplyValue;
    private Double acosSubtractValue;
    private Double acosMultiplyValue;
    private Double changeToValue;
    private Double lagerThanValue;
}
