package model.strategy.adControl;

import lombok.Data;
import model.enums.adPlacement.OperateType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class StrategyOperation {

    private boolean doOperate;

    private OperateType operateType;

    private double changeBidValue;
}
