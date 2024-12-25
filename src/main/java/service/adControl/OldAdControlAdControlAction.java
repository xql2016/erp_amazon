package service.adControl;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.configuration.OldAdConfiguration;
import model.constant.ErrorMessage;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.enums.CompareValueType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.adPlacement.CompareValue;
import model.enums.adPlacement.OperateType;
import model.request.*;
import model.response.AdGroup;
import model.response.AdPlacement;
import model.strategy.oldAdStrategy.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdReadRepository;
import repository.write.AdWriteRepository;
import tools.AdUtils;
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
public class OldAdControlAdControlAction extends AbstractionAdControlAction {

    @Override
    protected String getCode() {
        return "OldAdControl";
    }

    // 近30天的sku订单
    @Override
    protected AdGroupRequest buildAdGroupRequest(Configuration configuration) {
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(29));
        adGroupRequest.setProfile_ids(configuration.getHubIdList());
        if(CollectionUtils.isNotEmpty(configuration.getAdGroupNameList())) {
            adGroupRequest.setSearch_type("campaign_name");
            adGroupRequest.setName(configuration.getAdGroupNameList());
        }
        adGroupRequest.setSku(configuration.getOldAdConfiguration().getSkuList());
        return adGroupRequest;
    }

    @Override
    protected boolean needHandle(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        return !"paused".equalsIgnoreCase(adGroup.getState());
    }


    @Override
    protected void executeAdGroup(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        List<OldAdControlStrategy> oldAdControlStrategyList = configuration.getOldAdControlStrategyList();
        OldAdControlStrategy oldAdControlStrategy = findOldAdControlStrategy(oldAdControlStrategyList, adGroup, adGroupType, configuration);
        if(null == oldAdControlStrategy || null == oldAdControlStrategy.getOldAdStrategySearchCondition()) {
            System.out.println(String.format("    未找到操作策略, 存在配置或者代码问题, country=%s, adGroupName=%s", adGroup.getStore_country(), adGroup.getName()));
            return;
        }
        System.out.println(String.format("    近30天订单数=%s, country=%s, adGroupName=%s", adGroup.getAd_units(), adGroup.getStore_country(), adGroup.getName()));
        // 查询出投放列表
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(oldAdControlStrategy.getOldAdStrategySearchCondition().getAdPlacementSearchFromDays(), oldAdControlStrategy.getOldAdStrategySearchCondition().getAdPlacementSearchToDays()));
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        // 对投放列表进行处理
        for(AdPlacement adPlacement : adPlacementList) {
            executeAdPlacement(adPlacement, adGroup, adGroupType, configuration, oldAdControlStrategy.getOldAdStrategySearchCondition().getOldAdPlacementStrategyList(), oldAdControlStrategy.getOldAdStrategySearchCondition().getAdPlacementSearchFromDays(), oldAdControlStrategy.getOldAdStrategySearchCondition().getAdPlacementSearchToDays());
        }
    }

    private void executeAdPlacement(AdPlacement adPlacement, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration, List<OldAdPlacementStrategy> oldAdPlacementStrategyList, int fromDays, int toDays) {
        // 第一行统计不做处理
        if(StringUtils.isBlank(adPlacement.getAd_group_name())) {
            return;
        }
        String adPlacementName = null;
        switch (adGroupType) {
            case KEY_AD_GROUP:
                adPlacementName = adPlacement.getKeyword_text();
                break;
            case AUTO_AD_GROUP:
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                adPlacementName = adPlacement.getTargeting_text_zh();
                break;
        }
        if(StringUtils.isBlank(adPlacementName)) {
            return;
        }

        OldAdPlacementStrategy oldAdPlacementStrategy = findOldAdPlacementStrategy(oldAdPlacementStrategyList, adPlacement, adGroup, adGroupType, configuration);
        if(null == oldAdPlacementStrategy || null == oldAdPlacementStrategy.getOldAdPlacementStrategyOperation()) {
            System.out.println(String.format("    未找到操作策略, 存在配置或者代码问题, country=%s, adGroupName=%s, adPlacement=%s", adGroup.getStore_country(), adGroup.getName(), adPlacementName));
            return;
        }
        OldAdPlacementStrategyOperation oldAdPlacementStrategyOperation = oldAdPlacementStrategy.getOldAdPlacementStrategyOperation();
        if(!Boolean.TRUE.equals(oldAdPlacementStrategyOperation.getDoOperate())) {
            System.out.println(String.format("    不做操作, country=%s, adGroupName=%s, placementName=%s, 近%s天的acos =%s, order=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, fromDays + 1, adPlacement.getAcos(), adPlacement.getOrders()));
            return;
        }
        if(Boolean.TRUE.equals(oldAdPlacementStrategyOperation.getDoOpen())) {
            // 判断未打开则打开
            doOpenAdPlacement(adPlacement, adPlacementName, adGroup, adGroupType, configuration);
        }
        OperateType operateType = oldAdPlacementStrategyOperation.getAdOperateType();
        switch (operateType) {
            case ADD_BID:
                String nowBid = adPlacement.getBid();
                String nowCpc = adPlacement.getCpc();
                double changeToBid = Double.parseDouble(nowCpc) + oldAdPlacementStrategyOperation.getChangeBidValue();
                changeToBid = Math.min(oldAdPlacementStrategyOperation.getMaxValue(), changeToBid);
                BigDecimal bd = new BigDecimal(changeToBid);
                changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(StringUtils.isNotBlank(nowBid) && Double.parseDouble(nowBid) >= changeToBid) {
                    System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s，近%s天的acos =%s, order=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, fromDays + 1, adPlacement.getAcos(), adPlacement.getOrders(), adPlacement.getCpc(), nowBid, changeToBid));
                    return;
                }
                if(!configuration.isDoSimulation()) {
                    AdWriteRepository adWriteRepository = new AdWriteRepository();
                    AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
                    adPlacementChangeBidRequest.setBid(changeToBid);
                    adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
                    adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
                    boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType, 5);// 只尝试5次
                    System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s，近%s天的acos =%s, order=%s, cpc=%s, from %s to %s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, fromDays + 1, adPlacement.getAcos(), adPlacement.getOrders(), adPlacement.getCpc(), nowBid, changeToBid));
                } else {
                    System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s，近%s天的acos =%s, order=%s, cpc=%s, from %s to %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, fromDays + 1, adPlacement.getAcos(), adPlacement.getOrders(), adPlacement.getCpc(), nowBid, changeToBid));
                }
                return;
        }
    }

    private void doOpenAdPlacement(AdPlacement adPlacement, String adPlacementName, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 如果打开则不用处理
        if(!"paused".equalsIgnoreCase(adPlacement.getState())) {
            return;
        }
        // 暂停的就进行打开
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, adGroupType);
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 打开入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, order=%s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getOrders()));
        } else {
            System.out.println(String.format("    操作-simulation, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, order=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getOrders()));
        }
        return;
    }

    private OldAdPlacementStrategy findOldAdPlacementStrategy(List<OldAdPlacementStrategy> oldAdPlacementStrategyList, AdPlacement adPlacement, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        if(CollectionUtils.isEmpty(oldAdPlacementStrategyList)) {
            return null;
        }
        List<OldAdPlacementStrategy> tempList = oldAdPlacementStrategyList.stream().filter(it -> null != it.getOldAdPlacementSearchCondition() && AdUtils.hasOrder(adPlacement.getAcos()) == it.getOldAdPlacementSearchCondition().isHasOrder() && adGroupType.equals(it.getOldAdPlacementSearchCondition().getAdGroupType())).collect(Collectors.toList());
        for(OldAdPlacementStrategy oldAdPlacementStrategy : tempList) {
            OldAdPlacementSearchCondition oldAdPlacementSearchCondition = oldAdPlacementStrategy.getOldAdPlacementSearchCondition();
            if(!oldAdPlacementSearchCondition.isNeedCompare()) {
                return oldAdPlacementStrategy;
            }
            // 如果未通过校验则continue
            if(!compareValuePass(adPlacement, oldAdPlacementSearchCondition.getFirstCompareValue(), oldAdPlacementSearchCondition.getFirstCompareValueType(), oldAdPlacementSearchCondition.getFirstEqualsValue(), oldAdPlacementSearchCondition.getFirstBiggerThanValue(), oldAdPlacementSearchCondition.getFirstSmallerThanOrEqualToValue())) {
                continue;
            }
            if(!oldAdPlacementSearchCondition.isNeedCompareSecond()) {
                // 如果不需要第二个校验则返回
                return oldAdPlacementStrategy;
            } else {
                // 如果需要第二个校验则判断
                if(compareValuePass(adPlacement, oldAdPlacementSearchCondition.getSecondCompareValue(), oldAdPlacementSearchCondition.getSecondCompareValueType(), oldAdPlacementSearchCondition.getSecondEqualsValue(), oldAdPlacementSearchCondition.getSecondBiggerThanValue(), oldAdPlacementSearchCondition.getSecondSmallerThanOrEqualToValue())) {
                    return oldAdPlacementStrategy;
                }
            }
        }
        return null;
    }

    private boolean compareValuePass(AdPlacement adPlacement, CompareValue compareValue, CompareValueType compareValueType, Double equalsValue, Double biggerThanValue, Double smallerThanOrEqualToValue) {
        Double compareValueVal = null;
        switch (compareValue) {
            case ACOS:
                compareValueVal = Double.parseDouble(adPlacement.getAcos());
                break;
            case CLICK:
                compareValueVal = (double) adPlacement.getClicks();
                break;
            case ORDER:
                compareValueVal = (double) adPlacement.getOrders();
                break;
        }
        if(null == compareValueVal) {
            return false;
        }
        switch (compareValueType) {
            case EQUALS_VALUE:
                if(compareValueVal.equals(equalsValue)) {
                    return true;
                }
                break;
            case NUMERICAL_RANGE:
                if(compareValueVal > biggerThanValue && compareValueVal <= smallerThanOrEqualToValue) {
                    return true;
                }
                break;
        }
        return false;
    }



    private OldAdControlStrategy findOldAdControlStrategy(List<OldAdControlStrategy> oldAdControlStrategyList, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        if(CollectionUtils.isEmpty(oldAdControlStrategyList)) {
            return null;
        }
        List<OldAdControlStrategy> tempOldAdControlStrategyList = oldAdControlStrategyList.stream().filter(it -> it.getOldAdStrategySearchCondition().getAdGroupType().equals(adGroupType)).collect(Collectors.toList());
        for(OldAdControlStrategy oldAdControlStrategy : tempOldAdControlStrategyList) {
            OldAdStrategySearchCondition oldAdStrategySearchCondition = oldAdControlStrategy.getOldAdStrategySearchCondition();
            boolean passFirstValueCheck = passValueCheck(oldAdStrategySearchCondition.getAdGroupCompareValue(), oldAdStrategySearchCondition.getAdGroupCompareValueType(), oldAdStrategySearchCondition.getAdGroupEqualsValue(),
                    oldAdStrategySearchCondition.getAdGroupBiggerThanValue(), oldAdStrategySearchCondition.getAdGroupSmallerThanOrEqualToValue(), adGroup);
            if(passFirstValueCheck) {
                return oldAdControlStrategy;
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
            case ORDER:
                if(StringUtils.isBlank(adGroup.getAd_units())) {
                    compareVal = 0.0;
                } else {
                    compareVal = Double.parseDouble(adGroup.getAd_units());
                }
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


    @Override
    protected void loadSpecialConfiguration(Configuration configuration) throws Exception {
        // 记载skuList
        List<List<String>> result = ExcelUtils.readExcel(FilePath.oldSkuExcel);
        if(CollectionUtils.isEmpty(result) || result.size() <= 1) {
            throw new Exception(ErrorMessage.EXCEL_EMPTY);
        }
        OldAdConfiguration oldAdConfiguration = new OldAdConfiguration();
        List<String> skuList = new ArrayList<>();
        for(int i = 1; i < result.size(); i ++) {
            skuList.add(result.get(i).get(0));
        }
        oldAdConfiguration.setSkuList(skuList);
        configuration.setOldAdConfiguration(oldAdConfiguration);
        System.out.println(String.format("加载SkuList = %s", JSONObject.toJSONString(skuList)));
    }

}
