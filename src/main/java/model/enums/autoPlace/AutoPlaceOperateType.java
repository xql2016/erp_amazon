package model.enums.autoPlace;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AutoPlaceOperateType {

    // max(CPC-0.01 * (Math.ceil(acos/10)-4），0.02)
    CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE("CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE"),
    // max(CPC-0.01 * (点击-4），0.02)
    CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE"),
    BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE("BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE"),
    NO_OPERATE("NO_OPERATE"), //不操作
    CLOSE("CLOSE"), //关闭
    ;

    private String value;

    AutoPlaceOperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AutoPlaceOperateType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AutoPlaceOperateType operateType : AutoPlaceOperateType.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
