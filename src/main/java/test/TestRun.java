package test;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import repository.read.AdReadRepository;
import service.configuration.ConfigurationService;
import tools.AdGroupLogUtils;
import tools.AdPlacementLogUtils;
import tools.AdUtils;
import tools.DateUtils;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class TestRun {

//    public static void main(String[] args) throws Exception {
//        runAdPlacementLog();
//    }

    private static void runAdPlacementLog() {
        Configuration configuration = new ConfigurationService().loadLocalConfiguration();
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
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(1, 1)); // 昨天
        List<AdGroup> adGroupList = adGroupReadRepository.queryAdGroupList(adGroupRequest, configuration);
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
            if(!adGroupType.equals(AdGroupType.KEY_AD_GROUP)) {
                continue;
            }
            if(!adGroup.getName().equalsIgnoreCase("ZY202405-LWB006-手动-关键词-BROAD-关键词-刘文斌")) {
                continue;
            }
            AdGroupRequest request = new AdGroupRequest();
            request.setReport_date(DateUtils.buildReportDateString(3));
            request.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
            request.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
            AdGroup newAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(request, configuration);
            // 3. 查看投放入口近60天数据
            AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
            adPlacementRequest.setProfile_id(Long.parseLong(newAdGroup.getProfile_id()));
            adPlacementRequest.setReport_date(DateUtils.buildReportDateString(59));
            adPlacementRequest.setCampaign_id(Long.parseLong(newAdGroup.getCampaign_id()));
            List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
            for(AdPlacement adPlacement : adPlacementList) {
                // 第一行统计不做处理
                if(StringUtils.isBlank(adPlacement.getAd_group_name())) {
                    continue;
                }
                new AdPlacementLogUtils().hasBidOperateLog(newAdGroup, adGroupType, adPlacement, configuration, 30);
            }
        }
    }

    private static void runAdGroupLog() {
        Configuration configuration = new ConfigurationService().loadLocalConfiguration();
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
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(1, 1)); // 昨天
        List<AdGroup> adGroupList = adGroupReadRepository.queryAdGroupList(adGroupRequest, configuration);
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
            AdGroupRequest request = new AdGroupRequest();
            request.setReport_date(DateUtils.buildReportDateString(3));
            request.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
            request.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
            AdGroup newAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(request, configuration);
            new AdGroupLogUtils().hasBidOperateLog(newAdGroup, adGroupType, configuration, 1);
        }
    }
}
