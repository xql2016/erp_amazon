package model.strategy.adControl;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class AdControlStrategy {

    private StrategySearchCondition strategySearchCondition;

    private StrategyOperation strategyOperation;
}
