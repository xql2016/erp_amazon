package model.enums.adPlacement;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum CompareValue {

    ACOS("ACOS"),
    CLICK("CLICK"),
    ORDER("ORDER"),
    ;

    private String value;

    CompareValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static CompareValue getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (CompareValue compareValue : CompareValue.values()) {
            if (compareValue.getValue().equals(value)) {
                return compareValue;
            }
        }
        return null;
    }
}
