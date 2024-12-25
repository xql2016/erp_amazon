package service.spamControl;

import model.configuration.Configuration;
import model.configuration.spamTrafficFromPlace.*;
import model.request.autoPlace.AutoPlaceRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.autoPlace.AutoPlace;
import model.response.goodsPlace.GoodsPlace;
import model.response.keyPlace.KeyPlace;
import repository.read.AutoPlaceReadRepository;
import repository.read.GoodsPlaceReadRepository;
import repository.read.KeyPlaceReadRepository;

import java.util.List;

/**
 * 投放入口控制垃圾流量
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class SpamTrafficFromPlaceAction extends AbstractSpamControlAction {

    @Override
    protected void execute(Configuration configuration) {
        // 获取配置
        SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration = configuration.getSpamTrafficFromPlaceConfiguration();
        for(Long hubId : configuration.getHubIdList()) {
            System.out.println(String.format("hub=%s,开始处理", hubId));
            // 执行配置中的操作
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                AutoPlaceRequest autoPlaceRequest = AutoPlaceSpamTrafficFromPlaceService.buildAutoPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
                List<AutoPlace> placeList = new AutoPlaceReadRepository().queryAutoPlaceList(autoPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(AutoPlace autoPlace : placeList) {
                    AutoPlaceSpamTrafficFromPlaceService.doOperation(autoPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                GoodsPlaceRequest goodsPlaceRequest = CategoryPlaceSpamTrafficFromPlaceService.buildGoodsPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
                List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(GoodsPlace goodsPlace : placeList) {
                    CategoryPlaceSpamTrafficFromPlaceService.doOperation(goodsPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                KeyPlaceRequest keyPlaceRequest = KeyPlaceSpamTrafficFromPlaceService.buildKeyPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
                List<KeyPlace> placeList = new KeyPlaceReadRepository().queryKeyPlaceList(keyPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(KeyPlace keyPlace : placeList) {
                    KeyPlaceSpamTrafficFromPlaceService.doOperation(keyPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            for(SpamTrafficFromPlaceDetail spamTrafficFromPlaceDetail : spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList()) {
                // 1. 获取列表信息
                GoodsPlaceRequest goodsPlaceRequest = AsinPlaceSpamTrafficFromPlaceService.buildGoodsPlaceRequest(spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceSearchCondition(), hubId, configuration.getHubPortfolioIdList());
                List<GoodsPlace> placeList = new GoodsPlaceReadRepository().queryGoodsPlaceList(goodsPlaceRequest, configuration);
                // 2. 根据列表信息做for循环处理
                for(GoodsPlace goodsPlace : placeList) {
                    AsinPlaceSpamTrafficFromPlaceService.doOperation(goodsPlace, spamTrafficFromPlaceDetail.getSpamTrafficFromPlaceDoOperation(), configuration);
                }
            }
            System.out.println(String.format("hub=%s,处理完毕", hubId));
        }
    }

    @Override
    protected String getCode() {
        return "SpamTrafficFromPlace";
    }



}
