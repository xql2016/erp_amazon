package service.functionV6.spamValidFromPlace;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public enum SpamValidOperateType {
    MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE("MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE"),
    MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE("MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE"),
    NO_OPERATE("NO_OPERATE"), //不操作
    OPEN("OPEN"), //打开
    ;

    private String value;

    SpamValidOperateType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static SpamValidOperateType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (SpamValidOperateType operateType : SpamValidOperateType.values()) {
            if (operateType.getValue().equals(value)) {
                return operateType;
            }
        }
        return null;
    }
}
