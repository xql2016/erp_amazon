package model.enums.adGroup;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AdGroupCompareValue {

    EXPOSURE("EXPOSURE"),//广告组曝光
    CLICK("CLICK"),//广告组点击
    ORDER("ORDER"),//广告组订单
    ;

    private String value;

    AdGroupCompareValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AdGroupCompareValue getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AdGroupCompareValue compareValue : AdGroupCompareValue.values()) {
            if (compareValue.getValue().equals(value)) {
                return compareValue;
            }
        }
        return null;
    }
}
