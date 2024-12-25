package service.adControl;

import model.configuration.Configuration;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.response.AdGroup;
import org.apache.commons.collections4.CollectionUtils;
import tools.DateUtils;
import tools.ExcelUtils;
import tools.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/28
 */
public class ExcelExportAdControlAction extends AbstractionAdControlAction {

    @Override
    protected String getCode() {
        return "ExcelExport";
    }

    // 近3天最多的广告组
    @Override
    protected AdGroupRequest buildAdGroupRequest(Configuration configuration) {
        List<Double> spends = new ArrayList<>();
        spends.add(0.5);
        spends.add(100000000.0);
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(2));
        adGroupRequest.setProfile_ids(configuration.getHubIdList());
        if(CollectionUtils.isNotEmpty(configuration.getAdGroupNameList())) {
            adGroupRequest.setSearch_type("campaign_name");
            adGroupRequest.setName(configuration.getAdGroupNameList());
        }
        adGroupRequest.setSpends(spends);
        return adGroupRequest;
    }

    @Override
    protected boolean needHandle(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        return true;
    }

    @Override
    protected void executeAdGroup(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {

    }

    @Override
    protected void loadSpecialConfiguration(Configuration configuration) throws Exception {

    }

    @Override
    protected void doOperateAdGroupList(List<AdGroup> adGroupList, Configuration configuration) {
        List<List<String>> adGroupExportList = generateAdGroupExportList(adGroupList);
        List<String> headerList = generateHeaderList();
        ExcelUtils.writeExcel(FilePath.exportExcel, headerList, adGroupExportList);
    }

    private List<String> generateHeaderList() {
        List<String> result = new ArrayList<>();
        result.add("仓库id");
        result.add("广告组id-campaign_id");
        result.add("广告组id-ad_group_id");
        result.add("名称");
        result.add("ads_type");
        result.add("曝光量");
        result.add("点击");
        result.add("点击百分比");
        result.add("ctr");
        result.add("cpc");
        result.add("花费");
        result.add("花费百分比");
        result.add("销售额");
        result.add("销售额百分比");
        result.add("直接销售额");
        result.add("acos");
        result.add("广告订单");
        result.add("直接成交订单");
        result.add("cpa");
        result.add("cvr");
        result.add("广告单笔价");
        result.add("状态");
        result.add("国家信息");
        result.add("default_bid");
        return result;
    }

    private List<List<String>> generateAdGroupExportList(List<AdGroup> adGroupList) {
        List<List<String>> result = new ArrayList<>();
        for(AdGroup adGroup : adGroupList) {
            List<String> list = new ArrayList<>();
            list.add(adGroup.getProfile_id());
            list.add(adGroup.getCampaign_id());
            list.add(NumberUtils.toString(adGroup.getAd_group_id()));
            list.add(adGroup.getName());
            list.add(adGroup.getAds_type());
            list.add(NumberUtils.toString(adGroup.getImpressions()));
            list.add(NumberUtils.toString(adGroup.getClicks()));
            list.add(adGroup.getClicks_percent());
            list.add(adGroup.getCtr());
            list.add(adGroup.getCpc());
            list.add(adGroup.getSpends());
            list.add(adGroup.getSpends_percent());
            list.add(adGroup.getSales());
            list.add(adGroup.getSales_percent());
            list.add(adGroup.getDirect_sales());
            list.add(adGroup.getAcos());
            list.add(adGroup.getAd_units());
            list.add(NumberUtils.toString(adGroup.getDirect_orders()));
            list.add(adGroup.getCpa());
            list.add(adGroup.getCvr());
            list.add(adGroup.getUnit_price());
            list.add(adGroup.getState());
            list.add(adGroup.getStore_country());
            list.add(adGroup.getDefault_bid());
            result.add(list);
        }
        return result;
    }
}
