package model.strategy.newAdControl;

import lombok.Data;
import model.enums.adGroup.AdGroupOperateType;
import model.enums.adPlacement.OperateType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class NewAdStrategyOperation {

    private Boolean doOperate;

    private AdGroupOperateType adGroupOperateType;

    private Double changeAdGroupValue;
}
