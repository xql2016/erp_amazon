package model.configuration;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationStrategy {

    private String adGroupType;

    private boolean hasOrder;

    private String compareValue;

    private String compareValueType;

    private double equalsValue;

    private double biggerThanValue;// >

    private double smallerThanOrEqualToValue;// <=


    private boolean doOperate;

    private String operateType;

    private double changeBidValue;


}
