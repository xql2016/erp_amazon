package service.spamControl;

import model.configuration.Configuration;
import model.configuration.HubPortfolioId;
import model.configuration.spamTrafficFromPlace.*;
import model.enums.AdGroupType;
import model.enums.autoPlace.AutoPlaceCompareValue;
import model.enums.autoPlace.AutoPlaceOperateType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.response.goodsPlace.GoodsPlace;
import org.apache.commons.collections4.CollectionUtils;
import repository.write.AdWriteRepository;
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
public class CategoryPlaceSpamTrafficFromPlaceService {

    public static GoodsPlaceRequest buildGoodsPlaceRequest(SpamTrafficFromPlaceSearchCondition spamTrafficFromPlaceSearchCondition, Long hubId, List<HubPortfolioId> hubPortfolioIdList) {
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
        List<String> expression_types = new ArrayList<>(); // asinSameAs=商品,asinCategorySameAs=类目
        expression_types.add("asinCategorySameAs");

        GoodsPlaceRequest goodsPlaceRequest = new GoodsPlaceRequest();
        if(CollectionUtils.isNotEmpty(clicks)) {
            goodsPlaceRequest.setClicks(clicks);
        }
        if(CollectionUtils.isNotEmpty(orders)) {
            goodsPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            goodsPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(hubPortfolioIdList)) {
            HubPortfolioId hubPortfolioId = hubPortfolioIdList.stream().filter(it -> null != hubId && null != it.getHubId() && hubId.equals(it.getHubId())).findFirst().orElse(null);
            if(null != hubPortfolioId && CollectionUtils.isNotEmpty(hubPortfolioId.getPortfolioIdList())) {
                goodsPlaceRequest.setPortfolio_id(hubPortfolioId.getPortfolioIdList());
            }
        }
        goodsPlaceRequest.setProfile_id(hubId);
        goodsPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamTrafficFromPlaceSearchCondition.getDaysLargerThan(), spamTrafficFromPlaceSearchCondition.getDaysSmallerThan()));
        goodsPlaceRequest.setExpression_types(expression_types);
        return goodsPlaceRequest;
    }

    // 找到符合条件的策略然后进行操作
    public static void doOperation(GoodsPlace goodsPlace, SpamTrafficFromPlaceDoOperation spamTrafficFromPlaceDoOperation, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationSingle doOperationSingle : spamTrafficFromPlaceDoOperation.getDoOperationSingleList()) {
            if(matchCondition(goodsPlace, doOperationSingle)) {
                doOperation(goodsPlace, doOperationSingle, configuration);
                return;
            }
        }
        System.out.println(String.format("类目投放无处理, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", goodsPlace.getAd_group_name(),goodsPlace.getBid(),goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
    }

    private static void doOperation(GoodsPlace goodsPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle, Configuration configuration) {
        for(SpamTrafficFromPlaceDoOperationAction action : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
            double bidNow;
            if(null == goodsPlace.getBid()) {
                bidNow = 0.0;
            } else {
                bidNow = goodsPlace.getBid();
            }
            if(bidNow == 0.0) {
                bidNow = goodsPlace.getReal_bid();
            }
            switch (AutoPlaceOperateType.getByValue(action.getAutoPlaceOperateType())) {
                case NO_OPERATE:
                    System.out.println(String.format("类目投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                    break;
                case CLOSE:
                    doClose(configuration, goodsPlace, bidNow);
                    break;
                case CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeTo = Math.max(goodsPlace.getCpc() - action.getClickMultiplyValue() * (goodsPlace.getClicks() - action.getClickSubtractValue()), action.getLagerThanValue());
                    BigDecimal bd = new BigDecimal(bidChangeTo);
                    bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeTo) {
                        System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeTo, goodsPlace, bidNow);
                    }
                    break;
                case CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToB = Math.max(goodsPlace.getCpc() - action.getAcosMultiplyValue() * ((int)Math.ceil(Double.parseDouble(goodsPlace.getAcos())/10) - action.getAcosSubtractValue()), action.getLagerThanValue());
                    BigDecimal bdB = new BigDecimal(bidChangeToB);
                    bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToB) {
                        System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToB, goodsPlace, bidNow);
                    }
                    break;
                case BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE:
                    double bidChangeToC = Math.max(action.getChangeToValue(), action.getLagerThanValue());
                    BigDecimal bdC = new BigDecimal(bidChangeToC);
                    bidChangeToC = bdC.setScale(2, RoundingMode.HALF_UP).doubleValue();
                    if(bidNow <= bidChangeToC) {
                        System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToC,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                    }else {
                        doChangeBid(configuration, bidChangeToC, goodsPlace, bidNow);
                    }
                    break;
            }
        }
    }

    private static Boolean matchCondition(GoodsPlace goodsPlace, SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle) {
        for(SpamTrafficFromPlaceDoOperationCompare compare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
            switch (AutoPlaceCompareValue.getByValue(compare.getAutoPlaceCompareValue())) {
                case CLICK:
                    double clicks = null == goodsPlace.getClicks() ? 0 : goodsPlace.getClicks();
                    if(!CompareUtils.compareAutoPlaceCompareValue(clicks, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
                case ACOS:
                    double acos = null == goodsPlace.getAcos() || "99999999".equalsIgnoreCase(goodsPlace.getAcos()) ? 0 : Double.parseDouble(goodsPlace.getAcos());
                    if(!CompareUtils.compareAutoPlaceCompareValue(acos, compare.getAutoPlaceCompareValueType(), compare.getBiggerThan(), compare.getSmallerThan(), compare.getEqualTo())) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private static void doChangeBid(Configuration configuration, double bidChangeTo, GoodsPlace goodsPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(bidChangeTo);
            adPlacementChangeBidRequest.setTargetId(goodsPlace.getTarget_id());
            adPlacementChangeBidRequest.setProfileId(goodsPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.CATEGORY_AD_GROUP);
            System.out.println(String.format("类目投放操作,bid变更,真实操作结果=%s,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,bidNow,bidChangeTo,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        } else {
            System.out.println(String.format("类目投放操作,bid变更,仿真,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        }
    }

    private static void doClose(Configuration configuration, GoodsPlace goodsPlace, double bidNow) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(goodsPlace.getTarget_id());
            adPlacementPauseRequest.setProfileId(goodsPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.CATEGORY_AD_GROUP);
            System.out.println(String.format("类目投放操作,关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        } else {
            System.out.println(String.format("类目投放操作,关闭,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        }
    }

}
