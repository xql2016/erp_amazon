package service.functionV6;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.autoPlace.AutoPlaceRequest;
import model.response.autoPlace.AutoPlace;
import org.apache.commons.collections4.CollectionUtils;
import repository.write.AdWriteRepository;
import service.functionV6.spamValidFromPlace.SpamValidFromPlaceDoOperation;
import service.functionV6.spamValidFromPlace.SpamValidFromPlaceDoOperationAction;
import service.functionV6.spamValidFromPlace.SpamValidFromPlaceSearchCondition;
import service.functionV6.spamValidFromPlace.SpamValidOperateType;
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
public class AutoPlaceSpamValidFromPlaceService {

    public static AutoPlaceRequest buildAutoPlaceRequest(SpamValidFromPlaceSearchCondition spamValidFromPlaceSearchCondition, Long hubId, List<Long> portfolioIdList) {
        List<Double> cpcs = new ArrayList<>();
        if(null != spamValidFromPlaceSearchCondition.getCpcLargerThan() || null != spamValidFromPlaceSearchCondition.getCpcSmallerThan()) {
            cpcs.add(spamValidFromPlaceSearchCondition.getCpcLargerThan());
            cpcs.add(spamValidFromPlaceSearchCondition.getCpcSmallerThan());
        }
        List<Long> orders = new ArrayList<>();
        if(null != spamValidFromPlaceSearchCondition.getOrdersLargerThan() || null != spamValidFromPlaceSearchCondition.getOrdersSmallerThan()) {
            orders.add(spamValidFromPlaceSearchCondition.getOrdersLargerThan());
            orders.add(spamValidFromPlaceSearchCondition.getOrdersSmallerThan());
        }
        List<Long> acos = new ArrayList<>();
        if(null != spamValidFromPlaceSearchCondition.getAcosLargerThan() || null != spamValidFromPlaceSearchCondition.getAcosSmallerThan()) {
            acos.add(spamValidFromPlaceSearchCondition.getAcosLargerThan());
            acos.add(spamValidFromPlaceSearchCondition.getAcosSmallerThan());
        }

        AutoPlaceRequest autoPlaceRequest = new AutoPlaceRequest();
        if(CollectionUtils.isNotEmpty(orders)) {
            autoPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            autoPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(cpcs)) {
            autoPlaceRequest.setCpc(cpcs);
        }
        if(CollectionUtils.isNotEmpty(portfolioIdList)) {
            autoPlaceRequest.setPortfolio_id(portfolioIdList);
        }
        autoPlaceRequest.setProfile_id(hubId);
        autoPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamValidFromPlaceSearchCondition.getDaysLargerThan(), spamValidFromPlaceSearchCondition.getDaysSmallerThan()));
        return autoPlaceRequest;
    }

    // 找到符合条件的策略然后进行操作
    public static void doOperation(AutoPlace autoPlace, SpamValidFromPlaceDoOperation spamValidFromPlaceDoOperation, Configuration configuration) {
        SpamValidFromPlaceDoOperationAction action = spamValidFromPlaceDoOperation.getSpamValidFromPlaceDoOperationAction();
        double bidNow;
        if(null == autoPlace.getBid()) {
            bidNow = 0.0;
        } else {
            bidNow = autoPlace.getBid();
        }
        if(bidNow == 0.0) {
            bidNow = autoPlace.getReal_bid();
        }
        switch (SpamValidOperateType.getByValue(action.getSpamValidOperateType())) {
            case NO_OPERATE:
                System.out.println(String.format("自动投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                break;
            case OPEN:
                doOpen(configuration, autoPlace, bidNow);
                break;
            case MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeTo = Math.max(autoPlace.getCpc() - action.getCpcSubtractValue(), action.getLagerThanValue());
                BigDecimal bd = new BigDecimal(bidChangeTo);
                bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeTo) {
                    System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeTo, autoPlace, bidNow);
                }
                break;
            case MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeToB = Math.max(autoPlace.getCpc() + action.getCpcAddValue(), action.getLagerThanValue());
                BigDecimal bdB = new BigDecimal(bidChangeToB);
                bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeToB) {
                    System.out.println(String.format("自动投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToB, autoPlace, bidNow);
                }
                break;
        }
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

    private static void doOpen(Configuration configuration, AutoPlace autoPlace, double bidNow) {
        if("enabled".equalsIgnoreCase(autoPlace.getState())) {
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(Long.parseLong(autoPlace.getTarget_id()));
            adPlacementPauseRequest.setProfileId(autoPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, AdGroupType.AUTO_AD_GROUP);
            System.out.println(String.format("自动投放操作,打开,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        } else {
            System.out.println(String.format("自动投放操作,打开,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", autoPlace.getAd_group_name(),bidNow,autoPlace.getCpc(),autoPlace.getAcos(),autoPlace.getClicks(),autoPlace.getOrders()));
        }
    }

}
