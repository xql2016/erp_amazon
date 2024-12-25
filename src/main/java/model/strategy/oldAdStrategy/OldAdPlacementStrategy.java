package model.strategy.oldAdStrategy;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/10/5
 */
@Data
public class OldAdPlacementStrategy {

    private OldAdPlacementSearchCondition oldAdPlacementSearchCondition;

    private OldAdPlacementStrategyOperation oldAdPlacementStrategyOperation;
}
