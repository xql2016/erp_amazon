package model.configuration.userConfiguration;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationDetail {

    /**
     * 执行的操作
     */
    private List<String> executeActionCodeList;

    /**
     * 店铺id
     */
    private List<Long> hubIdList;

    /**
     * 国家列表
     */
    private List<String> countryList;

    /**
     * 广告组列表
     */
    private List<String> adGroupNameList;
}
