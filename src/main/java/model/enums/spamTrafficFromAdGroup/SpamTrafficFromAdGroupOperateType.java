package model.enums.spamTrafficFromAdGroup;

import model.enums.autoPlace.AutoPlaceOperateType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/1/7
 */
public enum SpamTrafficFromAdGroupOperateType {

    CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE("CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE"),
    // max(CPC-0.01 * (点击-9），0.02)
    CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE"),
    BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE("BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE"),
    NO_OPERATE("NO_OPERATE"), //不操作
    CLOSE("CLOSE"), //关闭入口
    ;

    private String value;

    SpamTrafficFromAdGroupOperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static SpamTrafficFromAdGroupOperateType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (SpamTrafficFromAdGroupOperateType operateType : SpamTrafficFromAdGroupOperateType.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
