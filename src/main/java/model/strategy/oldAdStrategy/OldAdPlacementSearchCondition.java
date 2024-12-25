package model.strategy.oldAdStrategy;

import lombok.Data;
import model.enums.AdGroupType;
import model.enums.CompareValueType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.adPlacement.CompareValue;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class OldAdPlacementSearchCondition {

    private AdGroupType adGroupType;

    private boolean hasOrder;

    private boolean needCompare;

    private boolean needCompareSecond;

    private CompareValue firstCompareValue;

    private CompareValueType firstCompareValueType;

    private Double firstEqualsValue;

    private Double firstBiggerThanValue;// >

    private Double firstSmallerThanOrEqualToValue;// <=

    private CompareValue secondCompareValue;

    private CompareValueType secondCompareValueType;

    private Double secondEqualsValue;

    private Double secondBiggerThanValue;// >

    private Double secondSmallerThanOrEqualToValue;// <=

}
