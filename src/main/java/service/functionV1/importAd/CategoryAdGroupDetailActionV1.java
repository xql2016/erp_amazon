package service.functionV1.importAd;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import repository.read.AdReadRepository;
import tools.AdPlacementUtils;
import tools.BidUtils;
import tools.DateUtils;
import tools.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * V1产品层面类目广告组导入流量处理类
 * 逻辑与自动广告组相同，需要处理昨天曝光
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class CategoryAdGroupDetailActionV1 {

    /**
     * 处理类目广告组的导入流量逻辑
     */
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 获取配置项
        int acosKeepOpen = getConfigValue(configuration, "V1产品层面类目投放入口保持打开的ACOS临界值", 60);
        int acosControlBase = getConfigValue(configuration, "V1产品层面类目投放入口ACOS控制基准", 35);
        int acosCpcMinus001 = getConfigValue(configuration, "V1产品层面类目投放入口竞价不低于CPC-0.01的ACOS临界值", 25);
        int acosCpcPlus002 = getConfigValue(configuration, "V1产品层面类目投放入口竞价不低于CPC+0.02的ACOS临界值", 15);
        
        // 查询广告组昨天的数据
        AdGroupRequest yesterdayRequest = new AdGroupRequest();
        yesterdayRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        yesterdayRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        yesterdayRequest.setReport_date(DateUtils.buildReportDateString(1, 1)); // 昨天
        AdGroup yesterdayAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(yesterdayRequest, configuration);
        
        int clicks30Days = adGroup.getClicks();
        int clicksYesterday = yesterdayAdGroup.getClicks();
        boolean hasOrders = adGroup.getOrders() != null && adGroup.getOrders() > 0;
        
        if (clicks30Days >= 10) {
            if (!hasOrders) {
                System.out.println(String.format("      广告组近30天点击≥10但无订单，不加流量: %s, 点击=%d", adGroup.getName(), clicks30Days));
                return;
            }
            processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else if (clicks30Days < 10 && clicksYesterday >= 1) {
            if (!hasOrders) {
                System.out.println(String.format("      广告组昨天有点击但无订单，不加流量: %s, 昨天点击=%d", adGroup.getName(), clicksYesterday));
                return;
            }
            processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else {
            processPlacementsByImpressionsAndOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        }
    }
    
    private void processPlacementsWithOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                           int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return;
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() < 1) {
                System.out.println(String.format("        投放入口无订单，跳过: %s", adPlacement.getTargeting_text_zh()));
                continue;
            }
            
            processPlacementWithOrder(adGroup, adGroupType, adPlacement, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        }
    }
    
    private void processPlacementsByImpressionsAndOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                                        int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        // 先查询昨天的投放入口数据，获取曝光
        AdPlacementRequest yesterdayRequest = new AdPlacementRequest();
        yesterdayRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        yesterdayRequest.setReport_date(DateUtils.buildReportDateString(1, 1)); // 昨天
        yesterdayRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> yesterdayPlacementList = new AdReadRepository().queryAdPlacementList(yesterdayRequest, configuration, adGroupType);
        
        // 查询投放入口近30天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return;
        }
        
        // 构建昨天曝光Map
        Map<String, Integer> yesterdayImpressionsMap = new HashMap<>();
        if (yesterdayPlacementList != null) {
            for (AdPlacement placement : yesterdayPlacementList) {
                if (StringUtils.isNotBlank(placement.getAd_group_name())) {
                    String key = getPlacementKey(placement);
                    yesterdayImpressionsMap.put(key, placement.getImpressions());
                }
            }
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() <= 0) {
                String key = getPlacementKey(adPlacement);
                int yesterdayImpressions = yesterdayImpressionsMap.getOrDefault(key, 0);
                
                if (yesterdayImpressions >= 20) {
                    System.out.println(String.format("        投放入口昨天曝光≥20，不加流量: %s, 昨天曝光=%d", 
                            adPlacement.getTargeting_text_zh(), yesterdayImpressions));
                    continue;
                }
                
                if (adPlacement.getClicks() >= 1) {
                    System.out.println(String.format("        投放入口无订单但有点击，不加流量: %s, 点击=%d", 
                            adPlacement.getTargeting_text_zh(), adPlacement.getClicks()));
                    continue;
                }
                
                Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                if (currentBid == null) {
                    System.out.println(String.format("        投放入口当前Bid为null，跳过: %s", 
                            adPlacement.getTargeting_text_zh()));
                    continue;
                }
                
                if (currentBid <= 0.3) {
                    new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.02, configuration);
                } else if (currentBid > 0.3 && currentBid <= 0.4) {
                    new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.01, configuration);
                } else {
                    // Bid＞0.4，不做处理
                    System.out.println(String.format("        投放入口Bid已达上限，不加流量: %s, currentBid=%.2f", 
                            adPlacement.getTargeting_text_zh(), currentBid));
                }
            } else {
                if (adPlacement.getOrders() >= 1) {
                    processPlacementWithOrder(adGroup, adGroupType, adPlacement, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
                }
            }
        }
    }
    
    private void processPlacementWithOrder(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration,
                                         int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
        Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
        
        if (placementAcos == null || placementCpc == null) {
            System.out.println(String.format("        投放入口ACOS或CPC为null，跳过: %s, ACOS=%s, CPC=%s", 
                    adPlacement.getTargeting_text_zh(), adPlacement.getAcos(), adPlacement.getCpc()));
            return;
        }
        
        if (placementAcos > acosControlBase && placementAcos <= acosKeepOpen) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
            // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
            BigDecimal bd = new BigDecimal(targetBid);
            targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc - 0.01;
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, placementCpc, configuration);
        } else if (placementAcos <= acosCpcPlus002) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc + 0.02;
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else {
            // ACOS>60%，超出保持打开范围，不做处理
            System.out.println(String.format("        投放入口ACOS>60%%，超出加流量范围，跳过: %s, ACOS=%.2f", 
                    adPlacement.getTargeting_text_zh(), placementAcos));
        }
    }
    
    /**
     * 获取投放入口的唯一标识（用于匹配昨天和近30天的数据）
     * 注意：ad_group_id可能为null，keyword_id和target_id为0时表示未设置
     */
    private String getPlacementKey(AdPlacement placement) {
        String adGroupId = placement.getAd_group_id() != null ? placement.getAd_group_id() : "";
        String targetId = placement.getTarget_id() != 0 ? String.valueOf(placement.getTarget_id()) : "";
        String keywordId = placement.getKeyword_id() != 0 ? String.valueOf(placement.getKeyword_id()) : "";
        return adGroupId + "_" + targetId + "_" + keywordId;
    }
    
    private int getConfigValue(Configuration configuration, String key, int defaultValue) {
        int value = configuration.getFeatures().getIntValue(key);
        return value > 0 ? value : defaultValue;
    }
}

