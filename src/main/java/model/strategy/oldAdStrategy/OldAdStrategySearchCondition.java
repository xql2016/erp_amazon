package model.strategy.oldAdStrategy;

import lombok.Data;
import model.enums.AdGroupType;
import model.enums.CompareValueType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.adPlacement.CompareValue;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class OldAdStrategySearchCondition {

    private AdGroupType adGroupType;

    private AdGroupCompareValue adGroupCompareValue;

    private CompareValueType adGroupCompareValueType;

    private Double adGroupEqualsValue;

    private Double adGroupBiggerThanValue;// >

    private Double adGroupSmallerThanOrEqualToValue;// <=

    private int adPlacementSearchFromDays;

    private int adPlacementSearchToDays;

    private List<OldAdPlacementStrategy> oldAdPlacementStrategyList;

}
