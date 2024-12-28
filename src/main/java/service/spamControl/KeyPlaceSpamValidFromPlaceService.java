package service.spamControl;

import model.configuration.Configuration;
import model.configuration.HubPortfolioId;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceDoOperation;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceDoOperationAction;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceSearchCondition;
import model.enums.AdGroupType;
import model.enums.spamValid.SpamValidOperateType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.keyPlace.KeyPlace;
import org.apache.commons.collections4.CollectionUtils;
import repository.write.AdWriteRepository;
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
public class KeyPlaceSpamValidFromPlaceService {

    public static KeyPlaceRequest buildKeyPlaceRequest(SpamValidFromPlaceSearchCondition spamValidFromPlaceSearchCondition, Long hubId, List<HubPortfolioId> hubPortfolioIdList) {
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

        KeyPlaceRequest keyPlaceRequest = new KeyPlaceRequest();
        if(CollectionUtils.isNotEmpty(orders)) {
            keyPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            keyPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(cpcs)) {
            keyPlaceRequest.setCpc(cpcs);
        }
        if(CollectionUtils.isNotEmpty(hubPortfolioIdList)) {
            HubPortfolioId hubPortfolioId = hubPortfolioIdList.stream().filter(it -> null != hubId && null != it.getHubId() && hubId.equals(it.getHubId())).findFirst().orElse(null);
            if(null != hubPortfolioId && CollectionUtils.isNotEmpty(hubPortfolioId.getPortfolioIdList())) {
                keyPlaceRequest.setPortfolio_id(hubPortfolioId.getPortfolioIdList());
            }
        }
        keyPlaceRequest.setProfile_id(hubId);
        keyPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamValidFromPlaceSearchCondition.getDaysLargerThan(), spamValidFromPlaceSearchCondition.getDaysSmallerThan()));
        return keyPlaceRequest;
    }


    // 找到符合条件的策略然后进行操作
    public static void doOperation(KeyPlace keyPlace, SpamValidFromPlaceDoOperation spamValidFromPlaceDoOperation, Configuration configuration) {
        SpamValidFromPlaceDoOperationAction action = spamValidFromPlaceDoOperation.getSpamValidFromPlaceDoOperationAction();
        double bidNow;
        if(null == keyPlace.getBid()) {
            bidNow = 0.0;
        } else {
            bidNow = keyPlace.getBid();
        }
        if(bidNow == 0.0) {
            bidNow = keyPlace.getReal_bid();
        }
        switch (SpamValidOperateType.getByValue(action.getSpamValidOperateType())) {
            case NO_OPERATE:
                System.out.println(String.format("关键词投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                break;
            case OPEN:
                doOpen(configuration, keyPlace, bidNow);
                break;
            case MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeTo = Math.max(keyPlace.getCpc() - action.getCpcSubtractValue(), action.getLagerThanValue());
                BigDecimal bd = new BigDecimal(bidChangeTo);
                bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeTo) {
                    System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeTo, keyPlace, bidNow);
                }
                break;
            case MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeToB = Math.max(keyPlace.getCpc() + action.getCpcAddValue(), action.getLagerThanValue());
                BigDecimal bdB = new BigDecimal(bidChangeToB);
                bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeToB) {
                    System.out.println(String.format("关键词投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToB, keyPlace, bidNow);
                }
                break;
        }
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

    private static void doOpen(Configuration configuration, KeyPlace keyPlace, double bidNow) {
        if("enabled".equalsIgnoreCase(keyPlace.getState())) {
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setProfileId(keyPlace.getProfile_id());
            adPlacementPauseRequest.setKeywordId(keyPlace.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, AdGroupType.KEY_AD_GROUP);
            System.out.println(String.format("关键词投放操作,打开,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        } else {
            System.out.println(String.format("关键词投放操作,打开,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", keyPlace.getAd_group_name(),bidNow,keyPlace.getCpc(),keyPlace.getAcos(),keyPlace.getClicks(),keyPlace.getOrders()));
        }
    }

}
