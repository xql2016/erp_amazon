package service.adControl;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.configuration.NewAdConfiguration;
import model.constant.ErrorMessage;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.enums.CompareValueType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.adGroup.AdGroupOperateType;
import model.request.AdGroupChangeBidRequest;
import model.request.AdGroupRequest;
import model.response.AdGroup;
import model.strategy.newAdControl.NewAdControlStrategy;
import model.strategy.newAdControl.NewAdStrategyOperation;
import model.strategy.newAdControl.NewAdStrategySearchCondition;
import org.apache.commons.collections4.CollectionUtils;
import repository.read.AdGroupReadRepository;
import repository.write.AdWriteRepository;
import tools.DateUtils;
import tools.ExcelUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/2
 */
//@Slf4j
public class NewAdControlAdControlAction extends AbstractionAdControlAction {

    @Override
    protected String getCode() {
        return "NewAdControl";
    }

    // 昨天的sku订单
    @Override
    protected AdGroupRequest buildAdGroupRequest(Configuration configuration) {
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(1, 1));
        adGroupRequest.setProfile_ids(configuration.getHubIdList());
        if(CollectionUtils.isNotEmpty(configuration.getAdGroupNameList())) {
            adGroupRequest.setSearch_type("campaign_name");
            adGroupRequest.setName(configuration.getAdGroupNameList());
        }
        adGroupRequest.setSku(configuration.getNewAdConfiguration().getSkuList());
        return adGroupRequest;
    }


    @Override
    protected boolean needHandle(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        return !"paused".equalsIgnoreCase(adGroup.getState()) && !adGroup.getName().contains("EXACT");
    }


    @Override
    protected void executeAdGroup(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        List<NewAdControlStrategy> newAdControlStrategyList = configuration.getNewAdControlStrategyList();
        NewAdControlStrategy newAdControlStrategy = findNewAdControlStrategy(newAdControlStrategyList, adGroup, adGroupType, configuration);
        if(null == newAdControlStrategy || null == newAdControlStrategy.getNewAdStrategyOperation()) {
            System.out.println(String.format("    未找到操作策略, 存在配置或者代码问题, country=%s, adGroupName=%s", adGroup.getStore_country(), adGroup.getName()));
            return;
        }
        NewAdStrategyOperation newAdStrategyOperation = newAdControlStrategy.getNewAdStrategyOperation();
        AdGroup thirtyDaysAdGroupDetail = findTempAdGroup(29, 0, adGroup, configuration);
        if(!Boolean.TRUE.equals(newAdStrategyOperation.getDoOperate())) {
            System.out.println(String.format("    不做操作, country=%s, adGroupName=%s, 昨天click=%s, 昨天曝光=%s, 30天click=%s, 30天曝光=%s", adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), thirtyDaysAdGroupDetail.getClicks(), thirtyDaysAdGroupDetail.getImpressions()));
            return;
        }
        AdGroupOperateType adGroupOperateType = newAdStrategyOperation.getAdGroupOperateType();
        AdGroup yesterdayAdGroupDetail = findTempAdGroup(1, 1, adGroup, configuration);
        switch (adGroupOperateType) {
            case ADD_VALUE:
                Double nowBid = Double.parseDouble(yesterdayAdGroupDetail.getDefault_bid());
                Double changeAdGroupValue = newAdStrategyOperation.getChangeAdGroupValue();
                double changeToBid = nowBid + changeAdGroupValue;
                switch (adGroupType) {
                    case KEY_AD_GROUP:
                    case ASIN_AD_GROUP:
                        changeToBid = Math.min(0.6, changeToBid);
                        break;
                    case AUTO_AD_GROUP:
                    case CATEGORY_AD_GROUP:
                        changeToBid = Math.min(0.4, changeToBid);
                        break;
                }

                BigDecimal bd = new BigDecimal(changeToBid);
                changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(nowBid >= changeToBid) {
                    System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, 昨天click=%s, 昨天曝光=%s, 30天click=%s, 30天曝光=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), thirtyDaysAdGroupDetail.getClicks(), thirtyDaysAdGroupDetail.getImpressions(), nowBid, changeToBid));
                    return;
                }
                if(!configuration.isDoSimulation()) {
                    AdWriteRepository adWriteRepository = new AdWriteRepository();
                    AdGroupChangeBidRequest adGroupChangeBidRequest = new AdGroupChangeBidRequest();
                    adGroupChangeBidRequest.setChangeToBid(changeToBid);
                    adGroupChangeBidRequest.setAdGroupId(yesterdayAdGroupDetail.getAd_group_id());
                    adGroupChangeBidRequest.setProfileId(Long.parseLong(yesterdayAdGroupDetail.getProfile_id()));
                    boolean operateResult = adWriteRepository.doAdGroupOperateChangeBid(adGroupChangeBidRequest, configuration, adGroupType);
                    System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, 昨天click=%s, 昨天曝光=%s, 30天click=%s, 30天曝光=%s, nowBid %s changeToBid %s", operateResult, adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), thirtyDaysAdGroupDetail.getClicks(), thirtyDaysAdGroupDetail.getImpressions(), nowBid, changeToBid));
                } else {
                    System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, 昨天click=%s, 昨天曝光=%s, 30天click=%s, 30天曝光=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), thirtyDaysAdGroupDetail.getClicks(), thirtyDaysAdGroupDetail.getImpressions(), nowBid, changeToBid));
                }
        }
    }

    @Override
    protected void loadSpecialConfiguration(Configuration configuration) throws Exception {
        // 记载skuList
        List<List<String>> result = ExcelUtils.readExcel(FilePath.skuExcel);
        if(CollectionUtils.isEmpty(result) || result.size() <= 1) {
            throw new Exception(ErrorMessage.EXCEL_EMPTY);
        }
        NewAdConfiguration newAdConfiguration = new NewAdConfiguration();
        List<String> skuList = new ArrayList<>();
        for(int i = 1; i < result.size(); i ++) {
            skuList.add(result.get(i).get(0));
        }
        newAdConfiguration.setSkuList(skuList);
        configuration.setNewAdConfiguration(newAdConfiguration);
        System.out.println(String.format("加载SkuList = %s", JSONObject.toJSONString(skuList)));
    }

    protected NewAdControlStrategy findNewAdControlStrategy(List<NewAdControlStrategy> newAdControlStrategyList, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        if(CollectionUtils.isEmpty(newAdControlStrategyList)) {
            return null;
        }
        List<NewAdControlStrategy> tempNewAdControlStrategyList = newAdControlStrategyList.stream().filter(it -> it.getNewAdStrategySearchCondition().getAdGroupType().equals(adGroupType)).collect(Collectors.toList());
        for(NewAdControlStrategy newAdControlStrategy : tempNewAdControlStrategyList) {
            NewAdStrategySearchCondition strategySearchCondition = newAdControlStrategy.getNewAdStrategySearchCondition();
            boolean passFirstValueCheck = passValueCheck(strategySearchCondition.getFirstCompareValue(), strategySearchCondition.getFirstCompareValueType(), strategySearchCondition.getFirstEqualsValue(),
                    strategySearchCondition.getFirstBiggerThanValue(), strategySearchCondition.getFirstSmallerThanOrEqualToValue(), adGroup);
            if(passFirstValueCheck) {
                if(null == strategySearchCondition.getSecondCompareValue()) {
                    return newAdControlStrategy;
                }

                AdGroup tempAdGroup = findTempAdGroup(strategySearchCondition.getSecondFromSearchDay(), strategySearchCondition.getSecondToSearchDay(), adGroup, configuration);
                boolean passValueCheck = passValueCheck(strategySearchCondition.getSecondCompareValue(), strategySearchCondition.getSecondCompareValueType(), strategySearchCondition.getSecondEqualsValue(),
                        strategySearchCondition.getSecondBiggerThanValue(), strategySearchCondition.getSecondSmallerThanOrEqualToValue(), tempAdGroup);
                if(passValueCheck) {
                    return newAdControlStrategy;
                }
            }
        }
        return null;
    }

    private boolean passValueCheck(AdGroupCompareValue compareValue, CompareValueType compareValueType, Double equalsValue, Double biggerThanValue, Double smallerThanOrEqualToValue, AdGroup adGroup) {
        Double compareVal = null;
        switch (compareValue) {
            case CLICK:
                compareVal = (double) adGroup.getClicks();
                break;
            case EXPOSURE:
                compareVal = (double) adGroup.getImpressions();
                break;
        }
        if(null == compareVal) {
            return false;
        }
        boolean passValueCheck = false;
        switch (compareValueType) {
            case EQUALS_VALUE:
                if(compareVal.equals(equalsValue)) {
                    passValueCheck = true;
                }
                break;
            case NUMERICAL_RANGE:
                if(compareVal > biggerThanValue && compareVal <= smallerThanOrEqualToValue) {
                    passValueCheck = true;
                }
                break;
        }
        return passValueCheck;
    }

    private AdGroup findTempAdGroup(int fromSearchDay, int toSearchDay, AdGroup originalAdGroup, Configuration configuration) {

        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(fromSearchDay, toSearchDay));
        adGroupRequest.setProfile_id(Long.parseLong(originalAdGroup.getProfile_id()));
        adGroupRequest.setCampaign_id(Long.parseLong(originalAdGroup.getCampaign_id()));

        AdGroup adGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(adGroupRequest, configuration);
        return adGroup;
    }
}
