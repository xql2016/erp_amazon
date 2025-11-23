package service.functionV6;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.response.goodsPlace.GoodsPlace;
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
public class CategoryPlaceSpamValidFromPlaceService {

    public static GoodsPlaceRequest buildGoodsPlaceRequest(SpamValidFromPlaceSearchCondition spamValidFromPlaceSearchCondition, Long hubId, List<Long> portfolioIdList) {
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
        List<String> expression_types = new ArrayList<>(); // asinSameAs=商品,asinCategorySameAs=类目
        expression_types.add("asinCategorySameAs");

        GoodsPlaceRequest goodsPlaceRequest = new GoodsPlaceRequest();
        if(CollectionUtils.isNotEmpty(orders)) {
            goodsPlaceRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            goodsPlaceRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(cpcs)) {
            goodsPlaceRequest.setCpc(cpcs);
        }
        if(CollectionUtils.isNotEmpty(portfolioIdList)) {
            goodsPlaceRequest.setPortfolio_id(portfolioIdList);
        }
        goodsPlaceRequest.setProfile_id(hubId);
        goodsPlaceRequest.setReport_date(DateUtils.buildReportDateString(spamValidFromPlaceSearchCondition.getDaysLargerThan(), spamValidFromPlaceSearchCondition.getDaysSmallerThan()));
        goodsPlaceRequest.setExpression_types(expression_types);
        return goodsPlaceRequest;
    }

    // 找到符合条件的策略然后进行操作
    public static void doOperation(GoodsPlace goodsPlace, SpamValidFromPlaceDoOperation spamValidFromPlaceDoOperation, Configuration configuration) {
        SpamValidFromPlaceDoOperationAction action = spamValidFromPlaceDoOperation.getSpamValidFromPlaceDoOperationAction();
        double bidNow;
        if(null == goodsPlace.getBid()) {
            bidNow = 0.0;
        } else {
            bidNow = goodsPlace.getBid();
        }
        if(bidNow == 0.0) {
            bidNow = goodsPlace.getReal_bid();
        }
        switch (SpamValidOperateType.getByValue(action.getSpamValidOperateType())) {
            case NO_OPERATE:
                System.out.println(String.format("类目投放操作,不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                break;
            case OPEN:
                doOpen(configuration, goodsPlace, bidNow);
                break;
            case MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeTo = Math.max(goodsPlace.getCpc() - action.getCpcSubtractValue(), action.getLagerThanValue());
                BigDecimal bd = new BigDecimal(bidChangeTo);
                bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeTo) {
                    System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeTo, goodsPlace, bidNow);
                }
                break;
            case MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeToB = Math.max(goodsPlace.getCpc() + action.getCpcAddValue(), action.getLagerThanValue());
                BigDecimal bdB = new BigDecimal(bidChangeToB);
                bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeToB) {
                    System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToB, goodsPlace, bidNow);
                }
                break;
            case CPC_MULTIPLY_VALUE_DIVIDE_ACOS_SUB_VALUE:
                double acos = null == goodsPlace.getAcos() || "99999999".equalsIgnoreCase(goodsPlace.getAcos()) ? 0 : Double.parseDouble(goodsPlace.getAcos());
                double bidChangeToC = Math.max((goodsPlace.getCpc() * action.getCpcMultiplyValue()/ acos) - action.getSubValue(), action.getLagerThanValue());
                BigDecimal bdC = new BigDecimal(bidChangeToC);
                bidChangeToC = bdC.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow >= bidChangeToC) {
                    System.out.println(String.format("类目投放操作,bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToC,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToC, goodsPlace, bidNow);
                }
                break;
        }
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

    private static void doOpen(Configuration configuration, GoodsPlace goodsPlace, double bidNow) {
        if("enabled".equalsIgnoreCase(goodsPlace.getState())) {
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(goodsPlace.getTarget_id());
            adPlacementPauseRequest.setProfileId(goodsPlace.getProfile_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, AdGroupType.CATEGORY_AD_GROUP);
            System.out.println(String.format("类目投放操作,打开,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        } else {
            System.out.println(String.format("类目投放操作,打开,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", goodsPlace.getAd_group_name(),bidNow,goodsPlace.getCpc(),goodsPlace.getAcos(),goodsPlace.getClicks(),goodsPlace.getOrders()));
        }
    }

}
