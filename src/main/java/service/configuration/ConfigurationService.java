package service.configuration;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.configuration.userConfiguration.InputConfigurationCustom;
import model.configuration.userConfiguration.InputConfigurationDetail;
import model.configuration.userConfiguration.InputConfigurationToken;
import model.constant.FilePath;
import model.response.AdGroup;
import model.response.AdGroupPageResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import tools.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationService {

    public Configuration loadLocalConfiguration() {
        // 读取用户原生的配置
        InputConfigurationToken inputConfigurationToken = loadInputConfigurationToken();
        InputConfigurationDetail inputConfigurationDetail = loadInputConfigurationDetail();
        InputConfigurationCustom inputConfigurationCustom = loadInputConfigurationCustom();
        return assembleConfiguration(inputConfigurationToken, inputConfigurationDetail, inputConfigurationCustom);
    }

    private InputConfigurationToken loadInputConfigurationToken() {
        String str = FileUtils.loadFile(FilePath.inputConfigurationToken);
        return JSONObject.parseObject(str, InputConfigurationToken.class);
    }

    private InputConfigurationDetail loadInputConfigurationDetail() {
        String str = FileUtils.loadFile(FilePath.inputConfigurationDetail);
        return JSONObject.parseObject(str, InputConfigurationDetail.class);
    }

    private InputConfigurationCustom loadInputConfigurationCustom() {
        String str = FileUtils.loadFile(FilePath.inputConfigurationCustom);
        InputConfigurationCustom inputConfigurationCustom = new InputConfigurationCustom();
        JSONObject features = JSONObject.parseObject(str);
        
        // 加载V1产品层面配置，合并到features中
        try {
            String v1ConfigStr = FileUtils.loadFile(FilePath.v1ConfigurationCustom);
            if (StringUtils.isNotBlank(v1ConfigStr)) {
                JSONObject v1Features = JSONObject.parseObject(v1ConfigStr);
                if (v1Features != null) {
                    // 合并V1配置到features中
                    for (String key : v1Features.keySet()) {
                        features.put(key, v1Features.get(key));
                    }
                }
            }
        } catch (Exception e) {
            // V1配置文件不存在或格式错误时，使用默认值，不阻塞流程
            System.out.println(String.format("加载V1配置文件失败，将使用默认值: %s", e.getMessage()));
        }
        
        inputConfigurationCustom.setFeatures(features);
        return inputConfigurationCustom;
    }

    private Configuration assembleConfiguration(InputConfigurationToken inputConfigurationToken, InputConfigurationDetail inputConfigurationDetail, InputConfigurationCustom inputConfigurationCustom) {
        if(null == inputConfigurationToken || null == inputConfigurationDetail || null == inputConfigurationCustom) {
            return null;
        }
        if(StringUtils.isBlank(inputConfigurationToken.getCookie()) || StringUtils.isBlank(inputConfigurationToken.getToken())) {
            return null;
        }
        if(CollectionUtils.isEmpty(inputConfigurationDetail.getHubIdList())) {
            return null;
        }
        if(CollectionUtils.isEmpty(inputConfigurationDetail.getExecuteActionCodeList())) {
            return null;
        }
        List<Long> portfolioIdList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(inputConfigurationDetail.getAdGroupNameList())) {
            AdGroupPageResult adGroupPageResult = new AdGroupReadRepository().queryAdGroupSimpleInfoList(inputConfigurationToken.getCookie(), inputConfigurationToken.getToken(), inputConfigurationDetail.getHubIdList());
            portfolioIdList = adGroupPageResult.getData().stream().filter(it -> inputConfigurationDetail.getAdGroupNameList().contains(it.getName())).map(AdGroup::getPortfolio_id).collect(Collectors.toList());
        }
        Configuration configuration = new Configuration();
        configuration.setToken(inputConfigurationToken.getToken());
        configuration.setCookie(inputConfigurationToken.getCookie());
        configuration.setDoSimulation(inputConfigurationToken.isDoSimulation());
        configuration.setExecuteActionCodeList(inputConfigurationDetail.getExecuteActionCodeList());
        configuration.setCountryList(inputConfigurationDetail.getCountryList());
        configuration.setHubIdList(inputConfigurationDetail.getHubIdList());
        configuration.setPortfolioIdList(portfolioIdList);
        configuration.setFeatures(inputConfigurationCustom.getFeatures());
        return configuration;
    }
}
