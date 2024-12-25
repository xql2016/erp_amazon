package model.enums.autoPlace;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AutoPlaceCompareValue {

    ACOS("ACOS"),
    CLICK("CLICK"),
    ;

    private String value;

    AutoPlaceCompareValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AutoPlaceCompareValue getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AutoPlaceCompareValue compareValue : AutoPlaceCompareValue.values()) {
            if (compareValue.getValue().equals(value)) {
                return compareValue;
            }
        }
        return null;
    }
}
