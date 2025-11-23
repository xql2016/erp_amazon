package service.functionV5;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.constant.FilePath;
import model.request.autoPlace.AutoPlaceRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.autoPlace.AutoPlace;
import model.response.goodsPlace.GoodsPlace;
import model.response.keyPlace.KeyPlace;
import repository.read.AutoPlaceReadRepository;
import repository.read.GoodsPlaceReadRepository;
import repository.read.KeyPlaceReadRepository;
import service.AbstractAction;
import service.functionV5.spamTrafficFromPlace.*;
import tools.FileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 投放入口控制垃圾流量
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class SpamTrafficFromPlaceAction extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) {
        // 获取配置
        SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration = buildSpamTrafficFromPlaceConfiguration(configuration);
        for(Long hubId : configuration.getHubIdList()) {
            System.out.println(String.format("hub=%s,开始处理", hubId));
            // 执行配置中的操作
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                AutoPlaceRequest autoPlaceRequest = AutoPlaceSpamTrafficFromPlaceService.buildAutoPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<AutoPlace> placeList = new AutoPlaceReadRepository().queryAutoPlaceList(autoPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(AutoPlace autoPlace : placeList) {
                    AutoPlaceSpamTrafficFromPlaceService.doOperation(autoPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                GoodsPlaceRequest goodsPlaceRequest = CategoryPlaceSpamTrafficFromPlaceService.buildGoodsPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(GoodsPlace goodsPlace : placeList) {
                    CategoryPlaceSpamTrafficFromPlaceService.doOperation(goodsPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                KeyPlaceRequest keyPlaceRequest = KeyPlaceSpamTrafficFromPlaceService.buildKeyPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<KeyPlace> placeList = new KeyPlaceReadRepository().queryKeyPlaceList(keyPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(KeyPlace keyPlace : placeList) {
                    KeyPlaceSpamTrafficFromPlaceService.doOperation(keyPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList()) {
                // 处理商品和商品扩展
                List<String> expressionTypes = new ArrayList<>();
                expressionTypes.add("asinSameAs");// 商品
                expressionTypes.add("asinExpandedFrom");// 商品扩展
                for(String expressionType : expressionTypes) {
                    // 1. 获取列表信息
                    GoodsPlaceRequest goodsPlaceRequest = AsinPlaceSpamTrafficFromPlaceService.buildGoodsPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList(), expressionType);
                    List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                    // 2. 根据列表信息做for循环处理
                    for(GoodsPlace goodsPlace : placeList) {
                        AsinPlaceSpamTrafficFromPlaceService.doOperation(goodsPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration, expressionType);
                    }
                }
            }
            System.out.println(String.format("hub=%s,处理完毕", hubId));
        }
    }

    @Override
    protected String getCode() {
        return "function_v5";
    }


    private SpamTrafficFromPlaceConfiguration buildSpamTrafficFromPlaceConfiguration(Configuration configuration) {
        String str = FileUtils.loadFile(FilePath.spamTrafficFromPlaceConfiguration);
        SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration = JSONObject.parseObject(str, SpamTrafficFromPlaceConfiguration.class);
        return assembleSpamTrafficFromPlaceConfiguration(spamTrafficFromPlaceConfiguration, configuration.getFeatures());
    }

    private SpamTrafficFromPlaceConfiguration assembleSpamTrafficFromPlaceConfiguration(SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration, JSONObject spamTrafficCustomConfiguration) {
        Integer nearlyDays = spamTrafficCustomConfiguration.getInteger("V5,投放入口,控制长期垃圾流量的天数");
        Long autoPlaceClicks = spamTrafficCustomConfiguration.getLong("V5,自动点击临界数");
        Long categoryPlaceClicks = spamTrafficCustomConfiguration.getLong("V5,类目点击临界数");
        Long keyPlaceClicks = spamTrafficCustomConfiguration.getLong("V5,关键词点击临界数");
        Long asinPlaceClicks = spamTrafficCustomConfiguration.getLong("V5,ASIN点击临界数");
        Long acosLargerThan = spamTrafficCustomConfiguration.getLong("V5,投放入口,批量控广告ACOS控制基准");
        if(null != nearlyDays) {
            spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
        }
        if(null != autoPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setClicksLargerThan(autoPlaceClicks);
                    }
            );
        }
        if(null != categoryPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setClicksLargerThan(autoPlaceClicks);
                    }
            );
        }
        if(null != keyPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        for(SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle : it.getSpamTrafficFromPlaceDoOperation().getDoOperationSingleList()) {
                            for(SpamTrafficFromPlaceDoOperationAction spamTrafficFromPlaceDoOperationAction : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
                                if("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())) {
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setSmallerThan((double) keyPlaceClicks);
                                        }
                                    }
                                } else if("CLOSE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())){
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setBiggerThan((double) keyPlaceClicks);
                                        }
                                    }
                                }
                            }
                        }
                    }
            );
        }
        if(null != asinPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        for(SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle : it.getSpamTrafficFromPlaceDoOperation().getDoOperationSingleList()) {
                            for(SpamTrafficFromPlaceDoOperationAction spamTrafficFromPlaceDoOperationAction : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
                                if("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())) {
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setSmallerThan((double) asinPlaceClicks);
                                        }
                                    }
                                } else if("CLOSE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())){
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setBiggerThan((double) asinPlaceClicks);
                                        }
                                    }
                                }
                            }
                        }
                    }
            );
        }
        if(null != acosLargerThan) {
            spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setAcosLargerThan(acosLargerThan);
                    }
            );
            spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setAcosLargerThan(acosLargerThan);
                    }
            );
            spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setAcosLargerThan(acosLargerThan);
                    }
            );
            spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setAcosLargerThan(acosLargerThan);
                    }
            );
        }
        return spamTrafficFromPlaceConfiguration;
    }

}
