package model.enums.autoPlace;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AutoPlaceOperateValue {

    BID("BID"), // 竞价
    ;

    private String value;

    AutoPlaceOperateValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AutoPlaceOperateValue getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AutoPlaceOperateValue operateType : AutoPlaceOperateValue.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
