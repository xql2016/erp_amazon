package model.strategy.newAdControl;

import lombok.Data;
import model.enums.AdGroupType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.CompareValueType;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class NewAdStrategySearchCondition {

    private AdGroupType adGroupType;

    private AdGroupCompareValue firstCompareValue;

    private CompareValueType firstCompareValueType;

    private Double firstEqualsValue;

    private Double firstBiggerThanValue;// >

    private Double firstSmallerThanOrEqualToValue;// <=

    private Integer secondFromSearchDay;

    private Integer secondToSearchDay;

    private AdGroupCompareValue secondCompareValue;

    private CompareValueType secondCompareValueType;

    private Double secondEqualsValue;

    private Double secondBiggerThanValue;// >

    private Double secondSmallerThanOrEqualToValue;// <=
}
