package model.configuration;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationOldAdStrategy {

    private String adGroupType;

    private String adGroupCompareValue;

    private String adGroupCompareValueType;

    private Double adGroupEqualsValue;

    private Double adGroupBiggerThanValue;// >

    private Double adGroupSmallerThanOrEqualToValue;// <=

    private Integer adPlacementSearchFromDays;

    private Integer adPlacementSearchToDays;

    private List<InputConfigurationOldAdPlacementStrategy> oldAdPlacementStrategyList;


}
