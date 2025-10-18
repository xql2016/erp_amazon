package model.configuration;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/30
 */
@Data
public class Configuration {

    /**
     * cookie
     */
    private String cookie;

    /**
     * token
     */
    private String token;

    /**
     * doSimulation
     */
    private boolean doSimulation;

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
     * 广告组合ID列表
     */
    private List<Long> portfolioIdList;

    /**
     * 用户自定义的一些策略参数
     */
    private JSONObject features;

}
