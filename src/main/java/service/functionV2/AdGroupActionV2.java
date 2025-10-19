package service.functionV2;

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

import java.util.ArrayList;
import java.util.List;

public class AdGroupActionV2 extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) throws Exception {
        // 1. 查询对应配置中昨天的情况,点击=0不处理,点击>=1的到各个广告组处理
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
        List<Long> clicks = new ArrayList<>();
        clicks.add(1L);
        clicks.add(999999999L);
        adGroupRequest.setClicks(clicks); // 点击数>=1
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
        // 1. 根据不同广告组获取要看的天数x
        String key = null;
        switch (adGroupType) {
            case KEY_AD_GROUP:
                key = "V2关键词广告组层面控制垃圾流量看的长期天数";
                break;
            case ASIN_AD_GROUP:
                key = "V2ASIN广告组层面控制垃圾流量看的长期天数";
                break;
            case AUTO_AD_GROUP:
                key = "V2自动广告组层面控制垃圾流量看的长期天数";
                break;
            case CATEGORY_AD_GROUP:
                key = "V2类目广告组层面控制垃圾流量看的长期天数";
                break;
        }
        int configDays = configuration.getFeatures().getInteger(key);
        int realDays = configDays - 1;// 配置近60天实际是今天之前59天+今天
        // 2. 查看广告组近x天的的数据
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(realDays));
        adGroupRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adGroupRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        AdGroup newAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(adGroupRequest, configuration);
        executeAdGroupDetail(newAdGroup, adGroupType, configuration, realDays);
    }

    // 1. 如果广告组近x天的广告订单为0
    //    1.1 如果总点击数<x不做处理
    //    1.2 如果总点击数>=x则看投放入口,改投放入口bid
    // 2. 如果广告组近x天的广告订单不为0
    //    2.1 如果总acos<=x不做处理
    //    2.2 如果总acos>=x则看投放入口,改投放入口bid
    protected void executeAdGroupDetail(AdGroup newAdGroup, AdGroupType adGroupType, Configuration configuration, int realDays) {
        switch (adGroupType) {
            case AUTO_AD_GROUP:
                new AutoAdGroupDetailAction().executeAdGroupDetail(newAdGroup, adGroupType, configuration, realDays);
                return;
            case CATEGORY_AD_GROUP:
                new CategoryAdGroupDetailAction().executeAdGroupDetail(newAdGroup, adGroupType, configuration, realDays);
                return;
            case KEY_AD_GROUP:
                new KeyAdGroupDetailAction().executeAdGroupDetail(newAdGroup, adGroupType, configuration, realDays);
                return;
            case ASIN_AD_GROUP:
                new AsinAdGroupDetailAction().executeAdGroupDetail(newAdGroup, adGroupType, configuration, realDays);
                return;
        }
    }

    @Override
    protected String getCode() {
        return "function_v2";
    }
}
