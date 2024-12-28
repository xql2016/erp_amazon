package service.spamControl;

import model.configuration.Configuration;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceConfiguration;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceDetail;
import model.request.autoPlace.AutoPlaceRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.autoPlace.AutoPlace;
import model.response.goodsPlace.GoodsPlace;
import model.response.keyPlace.KeyPlace;
import repository.read.AutoPlaceReadRepository;
import repository.read.GoodsPlaceReadRepository;
import repository.read.KeyPlaceReadRepository;

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
public class SpamValidFromPlaceAction extends AbstractSpamControlAction {

    @Override
    protected void execute(Configuration configuration) {
        // 获取配置
        SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration = configuration.getSpamValidFromPlaceConfiguration();
        for(Long hubId : configuration.getHubIdList()) {
            System.out.println(String.format("hub=%s,开始处理", hubId));
            // 执行配置中的操作
            List<SpamValidFromPlaceDetail> autoPlaceSpamValidFromPlaceDetailList = spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList();
            // 按照执行顺序进行排序
            autoPlaceSpamValidFromPlaceDetailList = autoPlaceSpamValidFromPlaceDetailList.stream().sorted(Comparator.comparing(SpamValidFromPlaceDetail::getOperateSeq)).collect(Collectors.toList());
            for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : autoPlaceSpamValidFromPlaceDetailList) {
                // 1. 获取列表信息
                AutoPlaceRequest autoPlaceRequest = AutoPlaceSpamValidFromPlaceService.buildAutoPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
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
                GoodsPlaceRequest goodsPlaceRequest = CategoryPlaceSpamValidFromPlaceService.buildGoodsPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
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
                KeyPlaceRequest keyPlaceRequest = KeyPlaceSpamValidFromPlaceService.buildKeyPlaceRequest(spamTrafficFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
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
                    GoodsPlaceRequest goodsPlaceRequest = AsinPlaceSpamValidFromPlaceService.buildGoodsPlaceRequest(spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList(), expressionType);
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
        return "SpamValidFromPlace";
    }



}
