package service.functionV1.control;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdReadRepository;
import tools.AdPlacementUtils;
import tools.BidUtils;
import tools.DateUtils;
import tools.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * V1产品层面自动广告组控制流量处理类
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class AutoAdGroupDetailActionV1 {

    /**
     * 处理自动广告组的控制流量逻辑
     */
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 获取配置项：V1产品层面自动广告组长期ACOS控制基准上限（默认35）
        String configKey = "V1产品层面自动广告组长期ACOS控制基准上限";
        int acosControlBase = configuration.getFeatures().getIntValue(configKey);
        if (acosControlBase <= 0) {
            acosControlBase = 35; // 默认值
        }
        
        // 判断条件：!(点击数 >= 20 && 花费>5欧 && 无订单) && !(点击数 >= 20 && cpa >= 5)
        Double spends = NumberUtils.parseDouble(adGroup.getSpends());
        boolean hasOrders = adGroup.getOrders() != null && adGroup.getOrders() > 0;
        Double cpa = NumberUtils.parseDouble(adGroup.getCpa());
        
        boolean condition1 = adGroup.getClicks() >= 20 && spends != null && spends > 5 && !hasOrders;
        boolean condition2 = adGroup.getClicks() >= 20 && cpa != null && cpa >= 5;
        boolean shouldProcess = !condition1 && !condition2;
        
        if (!shouldProcess) {
            return;
        }
        
        // 查询投放入口近30天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            return;
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() > 0) {
                // 投放入口有广告订单，分析ACoS数据
                Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
                if (placementAcos != null && placementAcos > acosControlBase) {
                    Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                    if (placementCpc != null && placementCpc > 0) {
                        double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
                        BigDecimal bd = new BigDecimal(targetBid);
                        targetBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
                        new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                    }
                }
            } else {
                // 投放入口没有广告订单，分析投放入口点击数
                int placementClicks = adPlacement.getClicks();
                Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                
                if (placementClicks >= 10) {
                    new AdPlacementUtils().close(adGroup, adGroupType, adPlacement, configuration);
                } else if (placementClicks >= 4) {
                    if (placementCpc != null) {
                        double targetBid = placementCpc + 0.04 - 0.01 * placementClicks;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        if (currentBid != null && currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                        }
                    }
                } else if (placementClicks > 0) {
                    if (placementCpc != null) {
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        if (currentBid != null && currentBid > placementCpc) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, placementCpc, configuration);
                        }
                    }
                }
            }
        }
    }
}

