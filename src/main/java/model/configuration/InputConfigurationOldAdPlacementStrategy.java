package model.configuration;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/10/6
 */
@Data
public class InputConfigurationOldAdPlacementStrategy {

    private String adGroupType;

    private boolean hasOrder;

    private boolean needCompare;

    private boolean needCompareSecond;

    private String firstCompareValue;

    private String firstCompareValueType;

    private Double firstEqualsValue;

    private Double firstBiggerThanValue;

    private Double firstSmallerThanOrEqualToValue;

    private String secondCompareValue;

    private String secondCompareValueType;

    private Double secondEqualsValue;

    private Double secondBiggerThanValue;

    private Double secondSmallerThanOrEqualToValue;

    private Boolean doOperate;

    private String adOperateType;

    private Double changeBidValue;

    private Boolean doOpen;

    private Double maxValue;

}
