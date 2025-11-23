package service.functionV7;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import repository.read.AdReadRepository;
import repository.write.AdWriteRepository;
import service.AbstractAction;
import service.functionV7.spamTrafficFromAdGroup.*;
import tools.AdUtils;
import tools.DateUtils;
import tools.FileUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 广告入口控制垃圾流量
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class SpamTrafficFromAdGroupAction extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) {
        // 获取配置
        SpamTrafficFromAdGroupConfiguration spamTrafficFromAdGroupConfiguration = buildSpamTrafficFromAdGroupConfiguration(configuration);
        for(Long hubId : configuration.getHubIdList()) {
            System.out.println(String.format("hub=%s,开始处理", hubId));
            // 执行配置中的操作
            for(SpamTrafficFromAdGroupDetail spamTrafficFromAdGroupDetail : spamTrafficFromAdGroupConfiguration.getSpamTrafficFromAdGroupDetailList()) {
                // 1. 构建adGroup查询条件
                AdGroupRequest adGroupRequest = this.buildAdGroupRequest(spamTrafficFromAdGroupDetail.getSpamTrafficFromAdGroupSearchCondition(), hubId, configuration.getPortfolioIdList());
                StringBuilder sb = new StringBuilder();
                if(CollectionUtils.isNotEmpty(adGroupRequest.getSpends())) {
                    sb.append("花费处于").append(adGroupRequest.getSpends());
                }
                if(CollectionUtils.isNotEmpty(adGroupRequest.getAcos())) {
                    sb.append("acos处于").append(adGroupRequest.getAcos());
                }
                if(CollectionUtils.isNotEmpty(adGroupRequest.getOrders())) {
                    sb.append("订单处于").append(adGroupRequest.getOrders());
                }
                System.out.println("处理" + sb + "开始");
                // 2. 查询广告组列表
                List<AdGroup> adGroupList = new AdGroupReadRepository().queryAdGroupList(adGroupRequest, configuration);
                // 3. 根据广告组信息做for循环处理
                for(AdGroup adGroup : adGroupList) {
                    if(StringUtils.isBlank(adGroup.getName())) {
                        continue;
                    }
                    AdGroupType adGroupType = findAdGroupType(adGroup);
                    if(null == adGroupType) {
                        System.out.println(String.format("not find adGroupType, adGroup.name=%s", adGroup.getName()));
                        continue;
                    }
                    if(needHandle(adGroup, adGroupType, configuration)) {
                        // 4. 处理每个广告组
                        System.out.println(String.format("handle adGroup=%s, adGroupType=%s, 查询条件=%s", adGroup.getName(), adGroupType.getDesc(), sb));
                        SpamTrafficFromAdGroupDoOperation spamTrafficFromAdGroupDoOperation = null;
                        // 5. 构建投放入口的查询条件
                        switch (adGroupType) {
                            case KEY_AD_GROUP:
                                spamTrafficFromAdGroupDoOperation = spamTrafficFromAdGroupDetail.getKeySpamTrafficFromAdGroupDoOperation();
                                break;
                            case CATEGORY_AD_GROUP:
                                spamTrafficFromAdGroupDoOperation = spamTrafficFromAdGroupDetail.getCategorySpamTrafficFromAdGroupDoOperation();
                                break;
                            case AUTO_AD_GROUP:
                                spamTrafficFromAdGroupDoOperation = spamTrafficFromAdGroupDetail.getAutoSpamTrafficFromAdGroupDoOperation();
                                break;
                            case ASIN_AD_GROUP:
                                spamTrafficFromAdGroupDoOperation = spamTrafficFromAdGroupDetail.getAsinSpamTrafficFromAdGroupDoOperation();
                                break;
                        }
                        // 6. 查询出投放列表
                        for(SpamTrafficFromAdGroupDoOperationAction spamTrafficFromAdGroupDoOperationAction : spamTrafficFromAdGroupDoOperation.getDoOperationActionList()) {
                            // 7. 根据投放数据进行处理
                            AdPlacementRequest adPlacementRequest = buildAdPlacementRequest(spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch(), adGroup);
                            List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
                            for(AdPlacement adPlacement : adPlacementList) {
                                // 第一行统计不做处理
                                if(StringUtils.isBlank(adPlacement.getAd_group_name())) {
                                    continue;
                                }
                                doOperation(adPlacement, adGroup, adGroupType, hubId, spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationDetail(), configuration);
                            }
                        }

                    }
                }
                System.out.println("处理" + sb + "结束");
            }
            System.out.println(String.format("hub=%s,处理完毕", hubId));
        }
    }

    @Override
    protected String getCode() {
        return "function_v7";
    }

    private void doOperation(AdPlacement adPlacement, AdGroup adGroup, AdGroupType adGroupType, Long hubId, SpamTrafficFromAdGroupDoOperationDetail action, Configuration configuration) {
        SpamTrafficFromAdGroupOperateType spamTrafficFromAdGroupOperateType = SpamTrafficFromAdGroupOperateType.getByValue(action.getSpamTrafficAdGroupOperateType());
        Double bidNow;
        if(StringUtils.isNotBlank(adPlacement.getBid())) {
            bidNow = Double.parseDouble(adPlacement.getBid());
        } else {
            bidNow = adPlacement.getReal_bid();
        }
        double cpc = Double.parseDouble(adPlacement.getCpc());
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
        switch (spamTrafficFromAdGroupOperateType) {
            case NO_OPERATE:
                System.out.println(String.format("  不操作, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,",adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
            case CLOSE:
                doClose(configuration, adPlacement, adGroupType, hubId, bidNow, adPlacementName);
                return;
            case CPC_SUBTRACT_ACOS_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeToB = Math.max(cpc - action.getAcosMultiplyValue() * ((int)Math.ceil(Double.parseDouble(adPlacement.getAcos())/10) - action.getAcosSubtractValue()), action.getLagerThanValue());
                BigDecimal bdB = new BigDecimal(bidChangeToB);
                bidChangeToB = bdB.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow <= bidChangeToB) {
                    System.out.println(String.format("  bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToB,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToB, adPlacement, adGroupType, hubId, bidNow, adPlacementName);
                }
                break;
            case CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeTo = Math.max(cpc - action.getClickMultiplyValue() * (adPlacement.getClicks() - action.getClickSubtractValue()), action.getLagerThanValue());
                BigDecimal bd = new BigDecimal(bidChangeTo);
                bidChangeTo = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow <= bidChangeTo) {
                    System.out.println(String.format("  bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeTo,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeTo, adPlacement, adGroupType, hubId, bidNow, adPlacementName);
                }
                return;
            case BID_CHANGE_TO_VALUE_AND_LAGER_THAN_VALUE:
                double bidChangeToC = Math.max(action.getChangeToValue(), action.getLagerThanValue());
                BigDecimal bdC = new BigDecimal(bidChangeToC);
                bidChangeToC = bdC.setScale(2, RoundingMode.HALF_UP).doubleValue();
                if(bidNow <= bidChangeToC) {
                    System.out.println(String.format("  bid无变更,变更前=%s,变更后=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", bidNow,bidChangeToC,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                }else {
                    doChangeBid(configuration, bidChangeToC, adPlacement, adGroupType, hubId, bidNow, adPlacementName);
                }
                return;
        }
    }

    private void doClose(Configuration configuration, AdPlacement adPlacement, AdGroupType adGroupType, Long hubId, Double bidNow, String adPlacementName) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            boolean operateResult = false;
            switch (adGroupType) {
                case AUTO_AD_GROUP:
                    adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementPauseRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.AUTO_AD_GROUP);
                    System.out.println(String.format("  关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case ASIN_AD_GROUP:
                    adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementPauseRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.ASIN_AD_GROUP);
                    System.out.println(String.format("  关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case CATEGORY_AD_GROUP:
                    adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementPauseRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.CATEGORY_AD_GROUP);
                    System.out.println(String.format("  关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case KEY_AD_GROUP:
                    adPlacementPauseRequest.setProfileId(hubId);
                    adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
                    operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.KEY_AD_GROUP);
                    System.out.println(String.format("  关闭,真实操作结果=%s, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
            }
        } else {
            System.out.println(String.format("  关闭,仿真, 名称=%s,bid=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,",adPlacementName,bidNow,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
        }
    }

    private void doChangeBid(Configuration configuration, Double bidChangeTo, AdPlacement adPlacement, AdGroupType adGroupType, Long hubId, Double bidNow, String adPlacementName) {
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            boolean operateResult = false;
            switch (adGroupType) {
                case AUTO_AD_GROUP:
                    adPlacementChangeBidRequest.setBid(bidChangeTo);
                    adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementChangeBidRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.AUTO_AD_GROUP);
                    System.out.println(String.format("  变更bid,真实操作结果=%s, 名称=%s,bid=%s,bid变更后=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,bidChangeTo,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case ASIN_AD_GROUP:
                    adPlacementChangeBidRequest.setBid(bidChangeTo);
                    adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementChangeBidRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.ASIN_AD_GROUP);
                    System.out.println(String.format("  变更bid,真实操作结果=%s, 名称=%s,bid=%s,bid变更后=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,bidChangeTo,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case CATEGORY_AD_GROUP:
                    adPlacementChangeBidRequest.setBid(bidChangeTo);
                    adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
                    adPlacementChangeBidRequest.setProfileId(hubId);
                    operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.CATEGORY_AD_GROUP);
                    System.out.println(String.format("  变更bid,真实操作结果=%s, 名称=%s,bid=%s,bid变更后=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,bidChangeTo,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
                case KEY_AD_GROUP:
                    adPlacementChangeBidRequest.setBid(bidChangeTo);
                    adPlacementChangeBidRequest.setProfileId(hubId);
                    adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
                    operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.KEY_AD_GROUP);
                    System.out.println(String.format("  变更bid,真实操作结果=%s, 名称=%s,bid=%s,bid变更后=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", operateResult,adPlacementName,bidNow,bidChangeTo,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
                    return;
            }
        } else {
            System.out.println(String.format("  变更bid,仿真, 名称=%s,bid=%s,bid变更后=%s,cpc=%s,acos=%s,点击数=%s,订单数=%s,", adPlacementName,bidNow,bidChangeTo,adPlacement.getCpc(),adPlacement.getAcos(),adPlacement.getClicks(),adPlacement.getOrders()));
        }
    }

    private AdPlacementRequest buildAdPlacementRequest(SpamTrafficFromAdGroupDoOperationSearch search, AdGroup adGroup) {
        List<Long> acos = new ArrayList<>();
        if(null != search.getAcosLargerThan() || null != search.getAcosSmallerThan()) {
            acos.add(search.getAcosLargerThan());
            acos.add(search.getAcosSmallerThan());
        }
        List<Long> clicks = new ArrayList<>();
        if(null != search.getClicksLargerThan() || null != search.getClicksSmallerThan()) {
            clicks.add(search.getClicksLargerThan());
            clicks.add(search.getClicksSmallerThan());
        }
        List<Long> orders = new ArrayList<>();
        if(null != search.getOrdersLargerThan() || null != search.getOrdersSmallerThan()) {
            orders.add(search.getOrdersLargerThan());
            orders.add(search.getOrdersSmallerThan());
        }

        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(search.getDaysLargerThan(), search.getDaysSmallerThan()));
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        adPlacementRequest.setClicks(clicks);
        adPlacementRequest.setAcos(acos);
        adPlacementRequest.setOrders(orders);
        return adPlacementRequest;
    }

    private AdGroupRequest buildAdGroupRequest(SpamTrafficFromAdGroupSearchCondition spamTrafficFromAdGroupSearchCondition, Long hubId, List<Long> portfolioIdList) {
        List<Double> spends = new ArrayList<>();
        if(null != spamTrafficFromAdGroupSearchCondition.getSpendLargerThan() || null != spamTrafficFromAdGroupSearchCondition.getSpendSmallerThan()) {
            spends.add(spamTrafficFromAdGroupSearchCondition.getSpendLargerThan());
            spends.add(spamTrafficFromAdGroupSearchCondition.getSpendSmallerThan());
        }
        List<Long> orders = new ArrayList<>();
        if(null != spamTrafficFromAdGroupSearchCondition.getOrdersLargerThan() || null != spamTrafficFromAdGroupSearchCondition.getOrdersSmallerThan()) {
            orders.add(spamTrafficFromAdGroupSearchCondition.getOrdersLargerThan());
            orders.add(spamTrafficFromAdGroupSearchCondition.getOrdersSmallerThan());
        }
        List<Long> acos = new ArrayList<>();
        if(null != spamTrafficFromAdGroupSearchCondition.getAcosLargerThan() || null != spamTrafficFromAdGroupSearchCondition.getAcosSmallerThan()) {
            acos.add(spamTrafficFromAdGroupSearchCondition.getAcosLargerThan());
            acos.add(spamTrafficFromAdGroupSearchCondition.getAcosSmallerThan());
        }

        AdGroupRequest adGroupRequest = new AdGroupRequest();
        if(CollectionUtils.isNotEmpty(spends)) {
            adGroupRequest.setSpends(spends);
        }
        if(CollectionUtils.isNotEmpty(orders)) {
            adGroupRequest.setOrders(orders);
        }
        if(CollectionUtils.isNotEmpty(acos)) {
            adGroupRequest.setAcos(acos);
        }
        if(CollectionUtils.isNotEmpty(portfolioIdList)) {
            adGroupRequest.setPortfolio_id(portfolioIdList);
        }
        adGroupRequest.setProfile_id(hubId);
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(spamTrafficFromAdGroupSearchCondition.getDaysLargerThan(), spamTrafficFromAdGroupSearchCondition.getDaysSmallerThan()));
        return adGroupRequest;
    }

    private AdGroupType findAdGroupType(AdGroup adGroup) {
        if(StringUtils.isBlank(adGroup.getName())) {
            return null;
        }
        return AdUtils.getAdGroupType(adGroup.getName());
    }

    private boolean needHandle(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        return !"paused".equalsIgnoreCase(adGroup.getState());
    }


    private SpamTrafficFromAdGroupConfiguration buildSpamTrafficFromAdGroupConfiguration(Configuration configuration) {
        try {
            String str = FileUtils.loadFile(FilePath.spamTrafficFromAdGroupConfiguration);
            SpamTrafficFromAdGroupConfiguration spamTrafficFromAdGroupConfiguration =  JSONObject.parseObject(str, SpamTrafficFromAdGroupConfiguration.class);
            return assembleSpamTrafficFromAdGroupConfiguration(spamTrafficFromAdGroupConfiguration, configuration.getFeatures());
        }catch (Exception e) {
            System.out.println("loadSpamTrafficFromAdGroupConfiguration error");
            return null;
        }
    }

    private SpamTrafficFromAdGroupConfiguration assembleSpamTrafficFromAdGroupConfiguration(SpamTrafficFromAdGroupConfiguration spamTrafficFromAdGroupConfiguration, JSONObject spamTrafficFromAdGroupCustomConfiguration) {
        Integer nearlyDays = null == spamTrafficFromAdGroupCustomConfiguration ? null : spamTrafficFromAdGroupCustomConfiguration.getInteger("V7,广告活动入口,控制长期垃圾流量的天数");
        Double spendsLimit = null == spamTrafficFromAdGroupCustomConfiguration ? null : spamTrafficFromAdGroupCustomConfiguration.getDouble("V7,广告活动花费临界数");

        for(SpamTrafficFromAdGroupDetail spamTrafficFromAdGroupDetail : spamTrafficFromAdGroupConfiguration.getSpamTrafficFromAdGroupDetailList()) {
            if(null != nearlyDays) {
                spamTrafficFromAdGroupDetail.getSpamTrafficFromAdGroupSearchCondition().setDaysLargerThan(nearlyDays - 1);
                spamTrafficFromAdGroupDetail.getSpamTrafficFromAdGroupSearchCondition().setDaysSmallerThan(0);

                for(SpamTrafficFromAdGroupDoOperationAction spamTrafficFromAdGroupDoOperationAction : spamTrafficFromAdGroupDetail.getAutoSpamTrafficFromAdGroupDoOperation().getDoOperationActionList()) {
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysLargerThan(nearlyDays - 1);
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysSmallerThan(0);
                }
                for(SpamTrafficFromAdGroupDoOperationAction spamTrafficFromAdGroupDoOperationAction : spamTrafficFromAdGroupDetail.getAsinSpamTrafficFromAdGroupDoOperation().getDoOperationActionList()) {
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysLargerThan(nearlyDays - 1);
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysSmallerThan(0);
                }
                for(SpamTrafficFromAdGroupDoOperationAction spamTrafficFromAdGroupDoOperationAction : spamTrafficFromAdGroupDetail.getKeySpamTrafficFromAdGroupDoOperation().getDoOperationActionList()) {
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysLargerThan(nearlyDays - 1);
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysSmallerThan(0);
                }
                for(SpamTrafficFromAdGroupDoOperationAction spamTrafficFromAdGroupDoOperationAction : spamTrafficFromAdGroupDetail.getCategorySpamTrafficFromAdGroupDoOperation().getDoOperationActionList()) {
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysLargerThan(nearlyDays - 1);
                    spamTrafficFromAdGroupDoOperationAction.getSpamTrafficFromAdGroupDoOperationSearch().setDaysSmallerThan(0);
                }
            }
            if(null != spendsLimit && null != spamTrafficFromAdGroupDetail.getSpamTrafficFromAdGroupSearchCondition().getSpendLargerThan()) {
                spamTrafficFromAdGroupDetail.getSpamTrafficFromAdGroupSearchCondition().setSpendLargerThan(spendsLimit);
            }
        }
        return spamTrafficFromAdGroupConfiguration;
    }

}
