package model.configuration;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationNewAdStrategy {

    private String adGroupType;

    private String firstCompareValue;

    private String firstCompareValueType;

    private Double firstEqualsValue;

    private Double firstBiggerThanValue;// >

    private Double firstSmallerThanOrEqualToValue;// <=

    private Integer secondFromSearchDay;

    private Integer secondToSearchDay;

    private String secondCompareValue;

    private String secondCompareValueType;

    private Double secondEqualsValue;

    private Double secondBiggerThanValue;// >

    private Double secondSmallerThanOrEqualToValue;// <=

    private boolean doOperate;

    private String operateType;

    private Double changeAdGroupValue;


}
