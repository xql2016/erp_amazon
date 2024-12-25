package model.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/7/23
 */
public enum AdGroupType {
    KEY_AD_GROUP("KEY_AD_GROUP", "关键词广告组"),
    ASIN_AD_GROUP("ASIN_AD_GROUP", "ASIN广告组"),
    AUTO_AD_GROUP("AUTO_AD_GROUP", "自动广告组"),
    CATEGORY_AD_GROUP("CATEGORY_AD_GROUP", "CATEGORY广告组"),
    ;

    private String value;

    private String desc;

    AdGroupType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return this.value;
    }

    public String getDesc() {
        return this.desc;
    }

    public static AdGroupType getByValue(String value) {
        if(StringUtils.isBlank(value)) {
            return null;
        }
        for (AdGroupType adGroupType : AdGroupType.values()) {
            if (adGroupType.getValue().equals(value)) {
                return adGroupType;
            }
        }
        return null;
    }


}
