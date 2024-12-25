package model.strategy.adControl;

import lombok.Data;
import model.enums.AdGroupType;
import model.enums.adPlacement.CompareValue;
import model.enums.CompareValueType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class StrategySearchCondition {

    private AdGroupType adGroupType;

    private boolean hasOrder;

    private CompareValue compareValue;

    private CompareValueType compareValueType;

    private double equalsValue;

    private double biggerThanValue;// >

    private double smallerThanOrEqualToValue;// <=
}
