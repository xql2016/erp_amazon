package model.strategy.newAdControl;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/21
 */
@Data
public class NewAdControlStrategy {

    private NewAdStrategySearchCondition newAdStrategySearchCondition;

    private NewAdStrategyOperation newAdStrategyOperation;
}
