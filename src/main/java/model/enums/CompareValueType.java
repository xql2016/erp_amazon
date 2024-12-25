package model.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum CompareValueType {

    NUMERICAL_RANGE("NUMERICAL_RANGE"),
    EQUALS_VALUE("EQUALS_VALUE"),
    ;

    private String value;

    CompareValueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static CompareValueType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (CompareValueType compareValueType : CompareValueType.values()) {
            if (compareValueType.getValue().equals(value)) {
                return compareValueType;
            }
        }
        return null;
    }
}
