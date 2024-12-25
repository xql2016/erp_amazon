package model.strategy.oldAdStrategy;

import lombok.Data;
import model.enums.adPlacement.OperateType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class OldAdPlacementStrategyOperation {

    private Boolean doOperate;

    private OperateType adOperateType;

    private Double changeBidValue;

    private Boolean doOpen;

    private Double maxValue;
}
