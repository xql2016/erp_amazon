package model.enums.adGroup;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum AdGroupOperateType {

    ADD_VALUE("ADD_VALUE"),
    ;

    private String value;

    AdGroupOperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static AdGroupOperateType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AdGroupOperateType operateType : AdGroupOperateType.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
