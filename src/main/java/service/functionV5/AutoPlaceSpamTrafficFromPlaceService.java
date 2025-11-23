package service.functionV5;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.autoPlace.AutoPlaceRequest;
import model.response.autoPlace.AutoPlace;
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
public class AutoPlaceSpamTrafficFromPlaceService {

    public static AutoPlaceRequest buildAutoPlaceRequest(SpamTrafficFromPlaceSearchCondition spamTrafficFromPlaceSearchCondition, Long hubId, List<Long> portfolioIdList) {
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

        AutoPlaceRequest autoPlaceRequest = new AutoPlaceRequest();
        if(CollectionUtils.isNotEmpty(clicks)) {
            autoPlaceRequest.setClicks(clicks);
        }
        if(CollectionUtils.isNotEmpty(orders)) {
            autoPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            autoPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(portfolioIdList)) {
            autoPlaceRequest.setPortfolio_id(portfolioIdList); // 新品老品
        }
        autoPlaceRequest.setProfile_id(hubId);
        autoPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamTrafficFromPlaceSearchCondition.getDaysLargerThan(), spamTrafficFromPlaceSearchCondition.getDaysSmallerThan()));
        return autoPlaceRequest;
    }

    // 找到符合条件的策略然后进行操作
    public static void doOperation(AutoPlace autoPlace, SpamTrafficFromPlaceDoOperation spamTrafficFromPlaceDoOperation, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationSingle doOperationSingle : spamTrafficFromPlaceDoOperation.getDoOperationSingleList()) {
            if(matchCondition(autoPlace, doOperationSingle)) {
                doOperation(autoPlace, doOperationSingle, configuration);
                return;
            }
        }
        System.out.println(String.format("自动投放无处理, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", autoPlace.getAd_group_name(),autoPlace.getBid(),autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
    }

    private static void doOperation(AutoPlace autoPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationAction action : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
            double bidNow;
            if(null == autoPlace.getBid()) {
                bidNow = 0.0;
            } else {
                bidNow = autoPlace.getBid();
            }
            if(bidNow == 0.0) {
                bidNow = autoPlace.getReal_bid();
            }
            switch (AutoPlaceOperateType.getByValue(action.getAutoPlaceOperateType())) {
                case NO_OPERATE:
                    System.out.println(String.format("自动投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                    break;
                case CLOSE:
                    doClose(configuration, autoPlace, bidNow);
                    break;
                case CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeTo = Math.max(autoPlace.getCpc() - action.getClickMultiplyValue() * (autoPlace.getClicks() - action.getClickSubtractValue()), action.getLagerThanValue());
                    BigDecimal bd = new BigDecimal(bidChangeTo);
                    bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeTo) {
                        System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeTo, autoPlace, bidNow);
                    }
                    break;
                case CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToB = Math.max(autoPlace.getCpc() - action.getAcosMultiplyValue() * ((int)Math.ceil(Double.parseDouble(autoPlace.getAcos())/10) - action.getAcosSubtractValue()), action.getLagerThanValue());
                    BigDecimal bdB = new BigDecimal(bidChangeToB);
                    bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToB) {
                        System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToB, autoPlace, bidNow);
                    }
                    break;
                case BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToC = Math.max(action.getChangeToValue(), action.getLagerThanValue());
                    BigDecimal bdC = new BigDecimal(bidChangeToC);
                    bidChangeToC = bdC.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToC) {
                        System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToC,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToC, autoPlace, bidNow);
                    }
                    break;
                case CPC_MULTIPLY_VALUE_DIVIDE_ACOS:
                    double acos = null == autoPlace.getAcos() || "99999999".equalsIgnoreCase(autoPlace.getAcos()) ? 0 : Double.parseDouble(autoPlace.getAcos());
                    double bidChangeToD = Math.max(autoPlace.getCpc() * action.getCpcMultiplyValue() / acos, action.getLagerThanValue());
                    BigDecimal bdD = new BigDecimal(bidChangeToD);
                    bidChangeToD = bdD.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToD) {
                        System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToD,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToD, autoPlace, bidNow);
                    }
                    break;
            }
        }
    }

    private static Boolean matchCondition(AutoPlace autoPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle) {
        for(SpamTrafficFromPlaceDoOperationCompare compare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
            switch (AutoPlaceCompareValue.getByValue(compare.getAutoPlaceCompareValue())) {
                case CLICK:
                    double clicks = null == autoPlace.getClicks() ? 0 : autoPlace.getClicks();
                    if(!CompareUtils.compareAutoPlaceCompareValue(clicks, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
                case ACOS:
                    double acos = null == autoPlace.getAcos() || "99999999".equalsIgnoreCase(autoPlace.getAcos()) ? 0 : Double.parseDouble(autoPlace.getAcos());
                    if(!CompareUtils.compareAutoPlaceCompareValue(acos, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private static void doChangeBid(Configuration configuration, double bidChangeTo, AutoPlace autoPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(bidChangeTo);
            adPlacementChangeBidRequest.setTargetId(Long.parseLong(autoPlace.getTarget_id()));
            adPlacementChangeBidRequest.setProfileId(autoPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.AUTO_AD_GROUP);
            System.out.println(String.format("自动投放操作,bid变更,真实操作结果=%s,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,bidNow,bidChangeTo,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        } else {
            System.out.println(String.format("自动投放操作,bid变更,仿真,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        }
    }

    private static void doClose(Configuration configuration, AutoPlace autoPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(Long.parseLong(autoPlace.getTarget_id()));
            adPlacementPauseRequest.setProfileId(autoPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.AUTO_AD_GROUP);
            System.out.println(String.format("自动投放操作,关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        } else {
            System.out.println(String.format("自动投放操作,关闭,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        }
    }

}
