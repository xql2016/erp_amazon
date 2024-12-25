package model.enums.adPlacement;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum OperateType {

    SUBTRACT_BID("SUBTRACT_BID"),
    CLOSE("CLOSE"),
    ADD_BID("ADD_BID"),
    ;

    private String value;

    OperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static OperateType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (OperateType operateType : OperateType.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
