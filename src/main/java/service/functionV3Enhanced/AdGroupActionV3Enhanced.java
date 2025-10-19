package service.functionV3Enhanced;


import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.response.AdGroup;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import service.AbstractAction;
import tools.AdUtils;
import tools.DateUtils;

import java.util.List;

public class AdGroupActionV3Enhanced extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) throws Exception {
        // 捞出所有配置条件下的广告组合
        AdGroupReadRepository adGroupReadRepository = new AdGroupReadRepository();
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        if(CollectionUtils.isNotEmpty(configuration.getCountryList())) {
            adGroupRequest.setCountries(configuration.getCountryList()); // 国家参数
        }
        if(CollectionUtils.isNotEmpty(configuration.getHubIdList())) {
            adGroupRequest.setProfile_ids(configuration.getHubIdList()); // hub参数
        }
        if(CollectionUtils.isNotEmpty(configuration.getPortfolioIdList())) {
            adGroupRequest.setPortfolio_id(configuration.getPortfolioIdList()); // 广告组参数
        }
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(2)); // 近3天数据
        List<AdGroup> adGroupList = adGroupReadRepository.queryAdGroupList(adGroupRequest, configuration);
        if(CollectionUtils.isEmpty(adGroupList)) {
            System.out.println("adGroupList.size=0");
            return;
        }
        System.out.println(String.format("adGroupList.size=%s", adGroupList.size()));
        // 对每个广告组判断和处理
        for(AdGroup adGroup : adGroupList) {
            // 第一行的广告组不处理
            if(StringUtils.isBlank(adGroup.getName())) {
                continue;
            }
            AdGroupType adGroupType = AdUtils.getAdGroupType(adGroup.getName());
            if(null == adGroupType) {
                System.out.println(String.format("not find adGroupType, adGroup.name=%s", adGroup.getName()));
                continue;
            }
            if(!"enabled".equalsIgnoreCase(adGroup.getState())) {
                System.out.println(String.format("广告组未启用, adGroup.name=%s", adGroup.getName()));
                continue;
            }
            // 处理每个广告组
            System.out.println(String.format("handle adGroup=%s, adGroupType=%s", adGroup.getName(), adGroupType.getDesc()));
            executeAdGroupBase(adGroup, adGroupType, configuration);
        }
    }

    protected void executeAdGroupBase(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        executeAdGroupDetail(adGroup, adGroupType, configuration);
    }

    protected void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        switch (adGroupType) {
            case AUTO_AD_GROUP:
                new AutoAdGroupDetailAction().executeAdGroupDetail(adGroup, adGroupType, configuration);
                return;
            case CATEGORY_AD_GROUP:
                new CategoryAdGroupDetailAction().executeAdGroupDetail(adGroup, adGroupType, configuration);
                return;
            case KEY_AD_GROUP:
                new KeyAdGroupDetailAction().executeAdGroupDetail(adGroup, adGroupType, configuration);
                return;
            case ASIN_AD_GROUP:
                new AsinAdGroupDetailAction().executeAdGroupDetail(adGroup, adGroupType, configuration);
                return;
        }
    }

    @Override
    protected String getCode() {
        return "function_v3_enhanced";
    }
}
