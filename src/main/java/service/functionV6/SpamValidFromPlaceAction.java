package service.functionV6;

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
import service.functionV6.spamValidFromPlace.SpamValidFromPlaceConfiguration;
import service.functionV6.spamValidFromPlace.SpamValidFromPlaceDetail;
import tools.FileUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投放入口控制垃圾流量
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class SpamValidFromPlaceAction extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) {
        // 获取配置
        SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration = buildSpamValidFromPlaceConfiguration(configuration);
        for(Long hubId : configuration.getHubIdList()) {
            System.out.println(String.format("hub=%s,开始处理", hubId));
            // 执行配置中的操作
            List<SpamValidFromPlaceDetail> autoPlaceSpamValidFromPlaceDetailList = spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList();
            // 按照执行顺序进行排序
            autoPlaceSpamValidFromPlaceDetailList = autoPlaceSpamValidFromPlaceDetailList.stream().sorted(Comparator.comparing(SpamValidFromPlaceDetail::getOperateSeq)).collect(Collectors.toList());
            for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : autoPlaceSpamValidFromPlaceDetailList) {
                // 1. 获取列表信息
                AutoPlaceRequest autoPlaceRequest = AutoPlaceSpamValidFromPlaceService.buildAutoPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<AutoPlace> placeList = new AutoPlaceReadRepository().queryAutoPlaceList(autoPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(AutoPlace autoPlace : placeList) {
                    AutoPlaceSpamValidFromPlaceService.doOperation(autoPlace, spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation(), configuration);
                }
            }
            List<SpamValidFromPlaceDetail> categoryPlaceSpamValidFromPlaceDetailList = spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList();
            // 按照执行顺序进行排序
            categoryPlaceSpamValidFromPlaceDetailList = categoryPlaceSpamValidFromPlaceDetailList.stream().sorted(Comparator.comparing(SpamValidFromPlaceDetail::getOperateSeq)).collect(Collectors.toList());
            for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : categoryPlaceSpamValidFromPlaceDetailList) {
                // 1. 获取列表信息
                GoodsPlaceRequest goodsPlaceRequest = CategoryPlaceSpamValidFromPlaceService.buildGoodsPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(GoodsPlace goodsPlace : placeList) {
                    CategoryPlaceSpamValidFromPlaceService.doOperation(goodsPlace, spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation(), configuration);
                }
            }
            List<SpamValidFromPlaceDetail> keyPlaceSpamValidFromPlaceDetailList = spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList();
            // 按照执行顺序进行排序
            keyPlaceSpamValidFromPlaceDetailList = keyPlaceSpamValidFromPlaceDetailList.stream().sorted(Comparator.comparing(SpamValidFromPlaceDetail::getOperateSeq)).collect(Collectors.toList());
            for(SpamValidFromPlaceDetail spamTrafficFromPlaceDetail : keyPlaceSpamValidFromPlaceDetailList) {
                // 1. 获取列表信息
                KeyPlaceRequest keyPlaceRequest = KeyPlaceSpamValidFromPlaceService.buildKeyPlaceRequest(spamTrafficFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList());
                List<KeyPlace> placeList = new KeyPlaceReadRepository().queryKeyPlaceList(keyPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(KeyPlace keyPlace : placeList) {
                    KeyPlaceSpamValidFromPlaceService.doOperation(keyPlace, spamTrafficFromPlaceDetail.getSpamValidFromPlaceDoOperation(), configuration);
                }
            }
            List<SpamValidFromPlaceDetail> asinPlaceSpamValidFromPlaceDetailList = spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList();
            // 按照执行顺序进行排序
            asinPlaceSpamValidFromPlaceDetailList = asinPlaceSpamValidFromPlaceDetailList.stream().sorted(Comparator.comparing(SpamValidFromPlaceDetail::getOperateSeq)).collect(Collectors.toList());
            for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : asinPlaceSpamValidFromPlaceDetailList) {
                // 处理商品和商品扩展
                List<String> expressionTypes = new ArrayList<>();
                expressionTypes.add("asinSameAs");// 商品
                expressionTypes.add("asinExpandedFrom");// 商品扩展
                for(String expressionType : expressionTypes) {
                    // 1. 获取列表信息
                    GoodsPlaceRequest goodsPlaceRequest = AsinPlaceSpamValidFromPlaceService.buildGoodsPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getPortfolioIdList(), expressionType);
                    List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                    // 2. 根据列表信息做for循环处理
                    for(GoodsPlace goodsPlace : placeList) {
                        AsinPlaceSpamValidFromPlaceService.doOperation(goodsPlace, spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation(), configuration, expressionType);
                    }
                }
            }
            System.out.println(String.format("hub=%s,处理完毕", hubId));
        }
    }

    @Override
    protected String getCode() {
        return "function_v6";
    }


    private SpamValidFromPlaceConfiguration buildSpamValidFromPlaceConfiguration(Configuration configuration) {
        String str = FileUtils.loadFile(FilePath.spamValidFromPlaceConfiguration);
        SpamValidFromPlaceConfiguration spamTrafficFromPlaceConfiguration = JSONObject.parseObject(str, SpamValidFromPlaceConfiguration.class);
        return assembleSpamValidFromPlaceConfiguration(spamTrafficFromPlaceConfiguration, configuration.getFeatures());
    }

    private SpamValidFromPlaceConfiguration assembleSpamValidFromPlaceConfiguration(SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration, JSONObject spamValidFromPlaceCustomConfiguration) {
        Integer nearlyDays = spamValidFromPlaceCustomConfiguration.getInteger("投放入口,放大长期有效流量的天数");
        Long autoPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("自动投放入口打开ACOS临界值");
        Long autoPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("自动投放竞价不低于CPC-0.01的ACOS临界值");
        Double autoPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("自动投放ACOS＜20% 竞价增加值");
        Long categoryPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("类目投放ACOS临界值");
        Long categoryPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("类目投放竞价不低于CPC-0.01的ACOS临界值");
        Double categoryPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("类目投放ACOS＜20% 竞价增加值");
        Long keyPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("关键词投放ACOS临界值");
        Long keyPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("关键词投放竞价不低于CPC-0.01的ACOS临界值");
        Double keyPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("关键词投放ACOS＜20% 竞价增加值");
        Long asinPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("ASIN投放ACOS临界值");
        Long asinPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("ASIN投放竞价不低于CPC-0.01的ACOS临界值");
        Double asinPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("ASIN投放ACOS＜20% 竞价增加值");
        if(null != nearlyDays) {
            spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList()) {
            if(null != autoPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoPlaceAcosOpen);
            }
            if(null != autoPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoPlaceAcosSub);
            }
            if(null != autoPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(autoPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList()) {
            if(null != asinPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinPlaceAcosOpen);
            }
            if(null != asinPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinPlaceAcosSub);
            }
            if(null != asinPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(asinPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList()) {
            if(null != categoryPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categoryPlaceAcosOpen);
            }
            if(null != categoryPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categoryPlaceAcosSub);
            }
            if(null != categoryPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(categoryPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList()) {
            if(null != keyPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keyPlaceAcosOpen);
            }
            if(null != keyPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keyPlaceAcosSub);
            }
            if(null != keyPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(keyPlaceAcosAdd);
            }
        }
        return spamValidFromPlaceConfiguration;
    }

}
