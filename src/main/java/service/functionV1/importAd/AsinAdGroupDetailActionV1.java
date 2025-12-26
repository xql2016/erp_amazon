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
import java.util.List;

/**
 * V1产品层面ASIN广告组导入流量处理类
 * 逻辑与关键词广告组相同
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class AsinAdGroupDetailActionV1 {

    /**
     * 处理ASIN广告组的导入流量逻辑
     * 
     * @return 是否执行了bid变更操作（true=执行了变更，false=符合策略未变更）
     */
    public boolean executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        boolean hasBidChanged = false; // 追踪是否执行了bid变更
        
        // 获取配置项
        int acosKeepOpen = getConfigValue(configuration, "V1产品层面ASIN投放入口保持打开的ACOS临界值", 60);
        int acosControlBase = getConfigValue(configuration, "V1产品层面ASIN投放入口ACOS控制基准", 35);
        int acosCpcMinus001 = getConfigValue(configuration, "V1产品层面ASIN投放入口竞价不低于CPC-0.01的ACOS临界值", 25);
        int acosCpcPlus002 = getConfigValue(configuration, "V1产品层面ASIN投放入口竞价不低于CPC+0.02的ACOS临界值", 15);
        
        // 查询广告组昨天的数据
        AdGroupRequest yesterdayRequest = new AdGroupRequest();
        yesterdayRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        yesterdayRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        yesterdayRequest.setReport_date(DateUtils.buildReportDateString(1, 1)); // 昨天
        AdGroup yesterdayAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(yesterdayRequest, configuration);
        
        int clicks30Days = adGroup.getClicks();
        int clicksYesterday = yesterdayAdGroup.getClicks();
        boolean hasOrders = adGroup.getOrders() != null && adGroup.getOrders() > 0;
        
        if (clicks30Days >= 15) {
            if (!hasOrders) {
                System.out.println(String.format("      广告组近30天点击≥15但无订单，不加流量: %s, 点击=%d", adGroup.getName(), clicks30Days));
                return false;
            }
            hasBidChanged = processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else if (clicks30Days < 15 && clicksYesterday >= 1) {
            if (!hasOrders) {
                System.out.println(String.format("      广告组昨天有点击但无订单，不加流量: %s, 昨天点击=%d", adGroup.getName(), clicksYesterday));
                return false;
            }
            hasBidChanged = processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else {
            hasBidChanged = processPlacementsByClicksAndOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
    
    /**
     * 处理有订单的投放入口
     * 
     * @return 是否执行了bid变更操作
     */
    private boolean processPlacementsWithOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                           int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        boolean hasBidChanged = false; // 追踪是否执行了bid变更
        
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return false;
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() < 1) {
                System.out.println(String.format("        投放入口无订单，跳过: %s", adPlacement.getTargeting_text_zh()));
                continue;
            }
            
            boolean changed = processPlacementWithOrder(adGroup, adGroupType, adPlacement, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
            hasBidChanged = hasBidChanged || changed; // 合并结果
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
    
    /**
     * 处理点击<15且昨天点击<1的情况，分析投放入口的点击和订单数据
     * 
     * @return 是否执行了bid变更操作
     */
    private boolean processPlacementsByClicksAndOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                                   int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        boolean hasBidChanged = false; // 追踪是否执行了bid变更
        
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return false;
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() <= 0) {
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
                    // Bid≤0.3，则将投放入口Bid增加0.02
                    hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.02, configuration);
                } else if (currentBid > 0.3 && currentBid <= 0.45) {
                    // 0.3＜Bid≤0.45，则将投放入口Bid增加0.01
                    hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.01, configuration);
                } else {
                    // Bid＞0.45，不做处理
                    System.out.println(String.format("        投放入口Bid已达上限，不加流量: %s, currentBid=%.2f", 
                            adPlacement.getTargeting_text_zh(), currentBid));
                }
            } else {
                if (adPlacement.getOrders() >= 1) {
                    boolean changed = processPlacementWithOrder(adGroup, adGroupType, adPlacement, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
                    hasBidChanged = hasBidChanged || changed; // 合并结果
                }
            }
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
    
    /**
     * 处理单个有订单的投放入口
     * 
     * @return 是否执行了bid变更操作
     */
    private boolean processPlacementWithOrder(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration,
                                         int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        boolean hasBidChanged = false; // 追踪是否执行了bid变更
        
        Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
        Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
        
        if (placementAcos == null || placementCpc == null) {
            System.out.println(String.format("        投放入口ACOS或CPC为null，跳过: %s, ACOS=%s, CPC=%s", 
                    adPlacement.getTargeting_text_zh(), adPlacement.getAcos(), adPlacement.getCpc()));
            return false;
        }
        
        if (placementAcos > acosControlBase && placementAcos <= acosKeepOpen) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
            // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
            BigDecimal bd = new BigDecimal(targetBid);
            targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
            hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc - 0.01;
            hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, placementCpc, configuration);
        } else if (placementAcos <= acosCpcPlus002) {
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc + 0.02;
            hasBidChanged = new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
        } else {
            // ACOS>60%，超出保持打开范围，不做处理
            System.out.println(String.format("        投放入口ACOS>60%%，超出加流量范围，跳过: %s, ACOS=%.2f", 
                    adPlacement.getTargeting_text_zh(), placementAcos));
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
    
    private int getConfigValue(Configuration configuration, String key, int defaultValue) {
        int value = configuration.getFeatures().getIntValue(key);
        return value > 0 ? value : defaultValue;
    }
}
