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
        Integer nearlyDays = spamValidFromPlaceCustomConfiguration.getInteger("V6,投放入口层面放大长期有效流量的控制天数");
        Long autoSeq1AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口保持打开的ACOS临界值");
        Long autoSeq2AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口保持打开的ACOS临界值");
        Long autoSeq2AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口ACOS控制基准");
        Long autoSeq3AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口ACOS控制基准");
        Long autoSeq3AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long autoSeq4AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long autoSeq4AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口竞价不低于CPC+0.02的ACOS临界值");
        Long autoSeq5AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,自动投放入口竞价不低于CPC+0.02的ACOS临界值");

        Long categorySeq1AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口保持打开的ACOS临界值");
        Long categorySeq2AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口保持打开的ACOS临界值");
        Long categorySeq2AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口ACOS控制基准");
        Long categorySeq3AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口ACOS控制基准");
        Long categorySeq3AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long categorySeq4AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long categorySeq4AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口竞价不低于CPC+0.02的ACOS临界值");
        Long categorySeq5AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,类目投放入口竞价不低于CPC+0.02的ACOS临界值");

        Long keySeq1AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口保持打开的ACOS临界值");
        Long keySeq2AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口保持打开的ACOS临界值");
        Long keySeq2AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口ACOS控制基准");
        Long keySeq3AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口ACOS控制基准");
        Long keySeq3AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long keySeq4AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long keySeq4AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口竞价不低于CPC+0.02的ACOS临界值");
        Long keySeq5AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,关键词投放入口竞价不低于CPC+0.02的ACOS临界值");

        Long asinSeq1AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口保持打开的ACOS临界值");
        Long asinSeq2AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口保持打开的ACOS临界值");
        Long asinSeq2AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口ACOS控制基准");
        Long asinSeq3AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口ACOS控制基准");
        Long asinSeq3AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long asinSeq4AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口竞价不低于CPC-0.01的ACOS临界值");
        Long asinSeq4AcosLargerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口竞价不低于CPC+0.02的ACOS临界值");
        Long asinSeq5AcosSmallerThan = spamValidFromPlaceCustomConfiguration.getLong("V6,ASIN投放入口竞价不低于CPC+0.02的ACOS临界值");

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
            if(1 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != autoSeq1AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoSeq1AcosSmallerThan);
                }
            }
            if(2 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != autoSeq2AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoSeq2AcosSmallerThan);
                }
                if(null != autoSeq2AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(autoSeq2AcosLargerThan);
                }
            }
            if(3 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != autoSeq3AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoSeq3AcosSmallerThan);
                }
                if(null != autoSeq3AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(autoSeq3AcosLargerThan);
                }
            }
            if(4 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != autoSeq4AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoSeq4AcosSmallerThan);
                }
                if(null != autoSeq4AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(autoSeq4AcosLargerThan);
                }
            }
            if(5 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != autoSeq5AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoSeq5AcosSmallerThan);
                }
            }
        }

        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList()) {
            if(1 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != categorySeq1AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categorySeq1AcosSmallerThan);
                }
            }
            if(2 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != categorySeq2AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categorySeq2AcosSmallerThan);
                }
                if(null != categorySeq2AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(categorySeq2AcosLargerThan);
                }
            }
            if(3 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != categorySeq3AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categorySeq3AcosSmallerThan);
                }
                if(null != categorySeq3AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(categorySeq3AcosLargerThan);
                }
            }
            if(4 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != categorySeq4AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categorySeq4AcosSmallerThan);
                }
                if(null != categorySeq4AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(categorySeq4AcosLargerThan);
                }
            }
            if(5 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != categorySeq5AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categorySeq5AcosSmallerThan);
                }
            }
        }

        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList()) {
            if(1 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != keySeq1AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keySeq1AcosSmallerThan);
                }
            }
            if(2 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != keySeq2AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keySeq2AcosSmallerThan);
                }
                if(null != keySeq2AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(keySeq2AcosLargerThan);
                }
            }
            if(3 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != keySeq3AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keySeq3AcosSmallerThan);
                }
                if(null != keySeq3AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(keySeq3AcosLargerThan);
                }
            }
            if(4 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != keySeq4AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keySeq4AcosSmallerThan);
                }
                if(null != keySeq4AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(keySeq4AcosLargerThan);
                }
            }
            if(5 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != keySeq5AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keySeq5AcosSmallerThan);
                }
            }
        }

        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList()) {
            if(1 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != asinSeq1AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinSeq1AcosSmallerThan);
                }
            }
            if(2 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != asinSeq2AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinSeq2AcosSmallerThan);
                }
                if(null != asinSeq2AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(asinSeq2AcosLargerThan);
                }
            }
            if(3 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != asinSeq3AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinSeq3AcosSmallerThan);
                }
                if(null != asinSeq3AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(asinSeq3AcosLargerThan);
                }
            }
            if(4 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != asinSeq4AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinSeq4AcosSmallerThan);
                }
                if(null != asinSeq4AcosLargerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosLargerThan(asinSeq4AcosLargerThan);
                }
            }
            if(5 == spamValidFromPlaceDetail.getOperateSeq()) {
                if(null != asinSeq5AcosSmallerThan) {
                    spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinSeq5AcosSmallerThan);
                }
            }
        }

        return spamValidFromPlaceConfiguration;
    }

}
