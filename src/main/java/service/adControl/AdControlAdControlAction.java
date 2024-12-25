package service.adControl;

import model.enums.AdGroupType;
import model.enums.adPlacement.OperateType;
import model.request.AdGroupRequest;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.AdPlacementRequest;
import model.configuration.Configuration;
import model.response.AdGroup;
import model.response.AdPlacement;
import model.strategy.adControl.AdControlStrategy;
import model.strategy.adControl.StrategyOperation;
import model.strategy.adControl.StrategySearchCondition;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdReadRepository;
import repository.write.AdWriteRepository;
import tools.AdUtils;
import tools.DateUtils;

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
public class AdControlAdControlAction extends AbstractionAdControlAction {

    @Override
    protected String getCode() {
        return "AdControl";
    }

    // 选择近3天花费超过0.5的广告组
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
        if(StringUtils.isBlank(adGroup.getSpends())) {
            return false;
        }
        // 暂停的不做处理
        if("paused".equalsIgnoreCase(adGroup.getState())) {
            return false;
        }
        double spendValue = Double.parseDouble(adGroup.getSpends());
        return spendValue > 0.5;
    }

    // 最近30天
    @Override
    protected void executeAdGroup(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 查询出投放列表
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29));
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        // 对投放列表进行处理
        for(AdPlacement adPlacement : adPlacementList) {
            executeAdPlacement(adPlacement, adGroup, adGroupType, configuration);
        }
    }

    @Override
    protected void loadSpecialConfiguration(Configuration configuration) {

    }

    protected void executeAdPlacement(AdPlacement adPlacement, AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
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
        // 暂停状态不做处理
        if("paused".equalsIgnoreCase(adPlacement.getState())) {
            return;
        }
        List<AdControlStrategy> adControlStrategyList = configuration.getAdControlStrategyList();
        AdControlStrategy adControlStrategy = findAdControlStrategy(adControlStrategyList, adPlacement, adGroupType, adPlacement.getAcos());
        if(null == adControlStrategy || null == adControlStrategy.getStrategyOperation()) {
            System.out.println(String.format("    未找到操作策略, 存在配置或者代码问题, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s%n", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks()));
            return;
        }
        StrategyOperation strategyOperation = adControlStrategy.getStrategyOperation();
        if(!strategyOperation.isDoOperate()) {
            System.out.println(String.format("    不做操作, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks()));
            return;
        }
        OperateType operateType = strategyOperation.getOperateType();
        switch (operateType) {
            case SUBTRACT_BID:
                String nowBid = adPlacement.getBid();
                String nowCpc = adPlacement.getCpc();
                double changeToBid = Double.parseDouble(nowCpc) - strategyOperation.getChangeBidValue();
                changeToBid = Math.max(0.02, changeToBid);
                BigDecimal bd = new BigDecimal(changeToBid);
                changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(StringUtils.isNotBlank(nowBid) && Double.parseDouble(nowBid) <= changeToBid) {
                    System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
                    return;
                }
                if(!configuration.isDoSimulation()) {
                    AdWriteRepository adWriteRepository = new AdWriteRepository();
                    AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
                    adPlacementChangeBidRequest.setBid(changeToBid);
                    adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
                    adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
                    boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType);
                    System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
                } else {
                    System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
                }
                return;
            case CLOSE:
                if(!configuration.isDoSimulation()) {
                    AdWriteRepository adWriteRepository = new AdWriteRepository();
                    AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
                    adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
                    adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
                    boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, adGroupType);
                    System.out.println(String.format("    操作-真实操作, 操作结果=%s, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks()));
                } else {
                    System.out.println(String.format("    操作-simulation, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), adPlacementName, adPlacement.getAcos(), adPlacement.getClicks()));
                }
                return;
        }
    }

    protected AdControlStrategy findAdControlStrategy(List<AdControlStrategy> adControlStrategyList, AdPlacement adPlacement, AdGroupType adGroupType, String acos) {
        if(CollectionUtils.isEmpty(adControlStrategyList)) {
            return null;
        }
        List<AdControlStrategy> tempAdControlStrategyList = adControlStrategyList.stream().filter(it -> null != it.getStrategyOperation() && AdUtils.hasOrder(acos) == it.getStrategySearchCondition().isHasOrder() && adGroupType.equals(it.getStrategySearchCondition().getAdGroupType())).collect(Collectors.toList());
        for(AdControlStrategy adControlStrategy : tempAdControlStrategyList) {
            StrategySearchCondition strategySearchCondition = adControlStrategy.getStrategySearchCondition();
            Double compareValue = null;
            switch (strategySearchCondition.getCompareValue()) {
                case ACOS:
                    compareValue = Double.parseDouble(adPlacement.getAcos());
                    break;
                case CLICK:
                    compareValue = (double) adPlacement.getClicks();
                    break;
            }
            if(null == compareValue) {
                continue;
            }
            switch (strategySearchCondition.getCompareValueType()) {
                case EQUALS_VALUE:
                    if(compareValue == strategySearchCondition.getEqualsValue()) {
                        return adControlStrategy;
                    }
                    break;
                case NUMERICAL_RANGE:
                    if(compareValue > strategySearchCondition.getBiggerThanValue() && compareValue <= strategySearchCondition.getSmallerThanOrEqualToValue()) {
                        return adControlStrategy;
                    }
                    break;
            }

        }
        return null;
    }
}
