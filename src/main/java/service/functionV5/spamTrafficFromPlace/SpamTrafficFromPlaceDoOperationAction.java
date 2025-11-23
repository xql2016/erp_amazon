package service.functionV5.spamTrafficFromPlace;

import lombok.Data;

/**
 * 扩展投放入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromPlaceDoOperationAction {

    private String autoPlaceOperateType; // 操作类型
    private Double clickSubtractValue;
    private Double clickMultiplyValue;
    private Double acosSubtractValue;
    private Double acosMultiplyValue;
    private Double changeToValue;
    private Double lagerThanValue;
    private Double cpcMultiplyValue;
}
