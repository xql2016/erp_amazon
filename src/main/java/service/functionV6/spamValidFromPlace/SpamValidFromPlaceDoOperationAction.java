package service.functionV6.spamValidFromPlace;

import lombok.Data;

/**
 * 扩展投放入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamValidFromPlaceDoOperationAction {

    private String spamValidOperateType; // 操作类型
    private Double cpcSubtractValue;
    private Double cpcAddValue;
    private Double lagerThanValue;
    private Double cpcMultiplyValue;
    private Double subValue;
}
