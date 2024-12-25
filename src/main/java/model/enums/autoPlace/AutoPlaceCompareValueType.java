package model.enums.autoPlace;

import org.apache.commons.lang3.StringUtils;

/**
 *  todo later整个package改名称
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AutoPlaceCompareValueType {

    BIGGER_OR_EQUAL_TO_SMALLER("BIGGER_OR_EQUAL_TO_SMALLER"),
    BIGGER_OR_EQUAL_TO_SMALLER_OR_EQUAL("BIGGER_OR_EQUAL_TO_SMALLER_OR_EQUAL"),
    BIGGER_TO_SMALLER_OR_EQUAL("BIGGER_TO_SMALLER_OR_EQUAL"),
    BIGGER_TO_SMALLER("BIGGER_TO_SMALLER"),
    EQUALS_VALUE("EQUALS_VALUE"),
    ;

    private String value;

    AutoPlaceCompareValueType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AutoPlaceCompareValueType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AutoPlaceCompareValueType compareValueType : AutoPlaceCompareValueType.values()) {
            if (compareValueType.getValue().equals(value)) {
                return compareValueType;
            }
        }
        return null;
    }
}
