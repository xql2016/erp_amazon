package service.functionV5;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.keyPlace.KeyPlace;
import org.apache.commons.collections4.CollectionUtils;
import repository.write.AdWriteRepository;
import service.functionV5.spamTrafficFromPlace.*;
import tools.CompareUtils;
import tools.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class KeyPlaceSpamTrafficFromPlaceService {

    public static KeyPlaceRequest buildKeyPlaceRequest(SpamTrafficFromPlaceSearchCondition spamTrafficFromPlaceSearchCondition, Long hubId, List<Long> portfolioIdList) {
        List<Long> clicks = new ArrayList<>();
        if(null != spamTrafficFromPlaceSearchCondition.getClicksLargerThan() || null != spamTrafficFromPlaceSearchCondition.getClicksSmallerThan()) {
            clicks.add(spamTrafficFromPlaceSearchCondition.getClicksLargerThan());
            clicks.add(spamTrafficFromPlaceSearchCondition.getClicksSmallerThan());
        }
        List<Long> orders = new ArrayList<>();
        if(null != spamTrafficFromPlaceSearchCondition.getOrdersLargerThan() || null != spamTrafficFromPlaceSearchCondition.getOrdersSmallerThan()) {
            orders.add(spamTrafficFromPlaceSearchCondition.getOrdersLargerThan());
            orders.add(spamTrafficFromPlaceSearchCondition.getOrdersSmallerThan());
        }
        List<Long> acos = new ArrayList<>();
        if(null != spamTrafficFromPlaceSearchCondition.getAcosLargerThan() || null != spamTrafficFromPlaceSearchCondition.getAcosSmallerThan()) {
            acos.add(spamTrafficFromPlaceSearchCondition.getAcosLargerThan());
            acos.add(spamTrafficFromPlaceSearchCondition.getAcosSmallerThan());
        }

        KeyPlaceRequest keyPlaceRequest = new KeyPlaceRequest();
        if(CollectionUtils.isNotEmpty(clicks)) {
            keyPlaceRequest.setClicks(clicks);
        }
        if(CollectionUtils.isNotEmpty(orders)) {
            keyPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            keyPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(portfolioIdList)) {
            keyPlaceRequest.setPortfolio_id(portfolioIdList);
        }
        keyPlaceRequest.setProfile_id(hubId);
        keyPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamTrafficFromPlaceSearchCondition.getDaysLargerThan(), spamTrafficFromPlaceSearchCondition.getDaysSmallerThan()));
        return keyPlaceRequest;
    }

    // 找到符合条件的策略然后进行操作
    public static void doOperation(KeyPlace keyPlace, SpamTrafficFromPlaceDoOperation spamTrafficFromPlaceDoOperation, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationSingle doOperationSingle : spamTrafficFromPlaceDoOperation.getDoOperationSingleList()) {
            if(matchCondition(keyPlace, doOperationSingle)) {
                doOperation(keyPlace, doOperationSingle, configuration);
                return;
            }
        }
        System.out.println(String.format("关键词投放无处理, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", keyPlace.getAd_group_name(),keyPlace.getBid(),keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
    }

    private static void doOperation(KeyPlace keyPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationAction action : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
            double bidNow;
            if(null == keyPlace.getBid()) {
                bidNow = 0.0;
            } else {
                bidNow = keyPlace.getBid();
            }
            if(bidNow == 0.0) {
                bidNow = keyPlace.getReal_bid();
            }
            switch (AutoPlaceOperateType.getByValue(action.getAutoPlaceOperateType())) {
                case NO_OPERATE:
                    System.out.println(String.format("关键词投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                    break;
                case CLOSE:
                    doClose(configuration, keyPlace, bidNow);
                    break;
                case CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeTo = Math.max(keyPlace.getCpc() - action.getClickMultiplyValue() * (keyPlace.getClicks() - action.getClickSubtractValue()), action.getLagerThanValue());
                    BigDecimal bd = new BigDecimal(bidChangeTo);
                    bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeTo) {
                        System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeTo, keyPlace, bidNow);
                    }
                    break;
                case CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToB = Math.max(keyPlace.getCpc() - action.getAcosMultiplyValue() * ((int)Math.ceil(Double.parseDouble(keyPlace.getAcos())/10) - action.getAcosSubtractValue()), action.getLagerThanValue());
                    BigDecimal bdB = new BigDecimal(bidChangeToB);
                    bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToB) {
                        System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToB, keyPlace, bidNow);
                    }
                    break;
                case BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToC = Math.max(action.getChangeToValue(), action.getLagerThanValue());
                    BigDecimal bdC = new BigDecimal(bidChangeToC);
                    bidChangeToC = bdC.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToC) {
                        System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToC,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToC, keyPlace, bidNow);
                    }
                    break;
                case CPC_MULTIPLY_VALUE_DIVIDE_ACOS:
                    double acos = null == keyPlace.getAcos() || "99999999".equalsIgnoreCase(keyPlace.getAcos()) ? 0 : Double.parseDouble(keyPlace.getAcos());
                    double bidChangeToD = Math.max(keyPlace.getCpc() * action.getCpcMultiplyValue() / acos, action.getLagerThanValue());
                    BigDecimal bdD = new BigDecimal(bidChangeToD);
                    bidChangeToD = bdD.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToD) {
                        System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToD,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToD, keyPlace, bidNow);
                    }
                    break;
            }
        }
    }

    private static Boolean matchCondition(KeyPlace keyPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle) {
        for(SpamTrafficFromPlaceDoOperationCompare compare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
            switch (AutoPlaceCompareValue.getByValue(compare.getAutoPlaceCompareValue())) {
                case CLICK:
                    double clicks = null == keyPlace.getClicks() ? 0 : keyPlace.getClicks();
                    if(!CompareUtils.compareAutoPlaceCompareValue(clicks, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
                case ACOS:
                    double acos = null == keyPlace.getAcos() || "99999999".equalsIgnoreCase(keyPlace.getAcos()) ? 0 : Double.parseDouble(keyPlace.getAcos());
                    if(!CompareUtils.compareAutoPlaceCompareValue(acos, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private static void doChangeBid(Configuration configuration, double bidChangeTo, KeyPlace keyPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(bidChangeTo);
            adPlacementChangeBidRequest.setProfileId(keyPlace.getProfile_id());
            adPlacementChangeBidRequest.setKeywordId(keyPlace.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.KEY_AD_GROUP);
            System.out.println(String.format("关键词投放操作,真实操作结果=%s,bid变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,bidNow,bidChangeTo,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        } else {
            System.out.println(String.format("关键词投放操作,仿真,bid变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        }
    }

    private static void doClose(Configuration configuration, KeyPlace keyPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setProfileId(keyPlace.getProfile_id());
            adPlacementPauseRequest.setKeywordId(keyPlace.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.KEY_AD_GROUP);
            System.out.println(String.format("关键词投放操作,关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        } else {
            System.out.println(String.format("关键词投放操作,关闭,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        }
    }

}
