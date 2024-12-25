package tools;

import lombok.extern.slf4j.Slf4j;
import model.enums.AdGroupType;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/2
 */
//@Slf4j
public class AdUtils {

    public static AdGroupType getAdGroupType(String adGroupName) {
        if(StringUtils.isBlank(adGroupName)) {
            return null;
        }
        if(adGroupName.contains("自动") || adGroupName.contains("Auto") || adGroupName.contains("auto")){
            return AdGroupType.AUTO_AD_GROUP;
        }
        if(adGroupName.contains("关键词") || adGroupName.contains("keyword") || adGroupName.contains("Keyword")
                ||  adGroupName.contains("keywords") || adGroupName.contains("Keywords")){
            return AdGroupType.KEY_AD_GROUP;
        }
        if(adGroupName.contains("ASIN") || adGroupName.contains("Asin") || adGroupName.contains("asin")){
            return AdGroupType.ASIN_AD_GROUP;
        }
        if(adGroupName.contains("CATEGORY") || adGroupName.contains("类目")){
            return AdGroupType.CATEGORY_AD_GROUP;
        }
        System.out.println(String.format("not find AdGroupType, adGroupName=%s", adGroupName));
        return null;
    }

    public static boolean hasOrder(String acos) {
        if(null == acos || "99999999".equals(acos)) {
            return false;
        }
        double acosLongValue = Double.parseDouble(acos);
        return acosLongValue != 0;
    }
}
