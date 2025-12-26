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
 * V1产品层面关键词广告组导入流量处理类
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class KeyAdGroupDetailActionV1 {

    /**
     * 处理关键词广告组的导入流量逻辑
     * 
     * @param adGroup 广告组数据（近30天）
     * @param adGroupType 广告组类型
     * @param configuration 配置信息
     * @return 是否执行了bid变更操作（true=执行了变更，false=符合策略未变更）
     */
    public boolean executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 用于追踪是否执行了bid变更或打开操作
        boolean hasBidChanged = false;
        
        // 获取配置项
        int acosKeepOpen = getConfigValue(configuration, "V1产品层面关键词投放入口保持打开的ACOS临界值", 60);
        int acosControlBase = getConfigValue(configuration, "V1产品层面关键词投放入口ACOS控制基准", 35);
        int acosCpcMinus001 = getConfigValue(configuration, "V1产品层面关键词投放入口竞价不低于CPC-0.01的ACOS临界值", 25);
        int acosCpcPlus002 = getConfigValue(configuration, "V1产品层面关键词投放入口竞价不低于CPC+0.02的ACOS临界值", 15);
        
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
            // 广告组最近30天点击≥15
            if (!hasOrders) {
                // 无广告订单，不做处理
                System.out.println(String.format("      广告组近30天点击≥15但无订单，不加流量: %s, 点击=%d", adGroup.getName(), clicks30Days));
                return false;
            }
            // 有广告订单，分析广告订单≥1的投放入口ACoS数据
            hasBidChanged = processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else if (clicks30Days < 15 && clicksYesterday >= 1) {
            // 广告组最近30天点击<15，昨天点击≥1
            if (!hasOrders) {
                // 无广告订单，不做处理
                System.out.println(String.format("      广告组昨天有点击但无订单，不加流量: %s, 昨天点击=%d", adGroup.getName(), clicksYesterday));
                return false;
            }
            // 有广告订单，分析广告订单≥1的投放入口ACoS数据
            hasBidChanged = processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else {
            // 广告组最近30天点击<15，昨天点击<1；分析投放入口的点击和订单数据
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
        
        // 查询投放入口近30天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return false; // 修改返回值
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            // 只处理订单≥1的投放入口
            if (adPlacement.getOrders() < 1) {
                System.out.println(String.format("        投放入口无订单，跳过: %s", adPlacement.getKeyword_text()));
                continue;
            }
            
            Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
            Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
            
            if (placementAcos == null || placementCpc == null) {
                System.out.println(String.format("        投放入口ACOS或CPC为null，跳过: %s, ACOS=%s, CPC=%s", 
                        adPlacement.getKeyword_text(), adPlacement.getAcos(), adPlacement.getCpc()));
                continue;
            }
            
            if (placementAcos > acosControlBase && placementAcos <= acosKeepOpen) {
                // 35%＜ACOS≤60%，保持投放入口打开
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
                // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
                // 策略文档要求"向上取"，原代码使用的是四舍五入
                BigDecimal bd = new BigDecimal(targetBid);
                targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
                new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                hasBidChanged = true; // 记录执行了bid变更
            } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
                // 25%＜ACOS≤35%，投放入口竞价≥CPC-0.01
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                double targetBid = placementCpc - 0.01;
                new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                hasBidChanged = true; // 记录执行了bid变更
            } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
                // 15%＜ACOS≤25%，投放入口竞价≥CPC
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, placementCpc, configuration);
                hasBidChanged = true; // 记录执行了bid变更
            } else if (placementAcos <= acosCpcPlus002) {
                // ACOS≤15%，投放入口竞价≥CPC+0.02
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                double targetBid = placementCpc + 0.02;
                new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                hasBidChanged = true; // 记录执行了bid变更
            } else {
                // ACOS>60%，超出保持打开范围，不做处理
                System.out.println(String.format("        投放入口ACOS>60%%，超出加流量范围，跳过: %s, ACOS=%.2f", 
                        adPlacement.getKeyword_text(), placementAcos));
            }
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
        
        // 查询投放入口近30天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            System.out.println(String.format("      广告组查询不到投放入口，跳过: %s", adGroup.getName()));
            return false; // 修改返回值
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            if (adPlacement.getOrders() <= 0) {
                // 投放入口无广告订单
                if (adPlacement.getClicks() >= 1) {
                    // 投放入口点击≥1，不做处理
                    System.out.println(String.format("        投放入口无订单但有点击，不加流量: %s, 点击=%d", 
                            adPlacement.getKeyword_text(), adPlacement.getClicks()));
                    continue;
                }
                // 投放入口点击=0
                Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                if (currentBid == null) {
                    System.out.println(String.format("        投放入口当前Bid为null，跳过: %s", 
                            adPlacement.getKeyword_text()));
                    continue;
                }
                
                if (currentBid <= 0.3) {
                    // Bid≤0.3，则将投放入口Bid增加0.02
                    new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.02, configuration);
                    hasBidChanged = true; // 记录执行了bid变更
                } else if (currentBid > 0.3 && currentBid <= 0.45) {
                    // 0.3＜Bid≤0.45，则将投放入口Bid增加0.01
                    new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, currentBid + 0.01, configuration);
                    hasBidChanged = true; // 记录执行了bid变更
                } else {
                    // Bid＞0.45，不做处理
                    System.out.println(String.format("        投放入口Bid已达上限，不加流量: %s, currentBid=%.2f", 
                            adPlacement.getKeyword_text(), currentBid));
                }
            } else {
                // 投放入口有广告订单，分析广告订单≥1的投放入口ACoS数据
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
                    adPlacement.getKeyword_text(), adPlacement.getAcos(), adPlacement.getCpc()));
            return false; // 修改返回值
        }
        
        if (placementAcos > acosControlBase && placementAcos <= acosKeepOpen) {
            // 35%＜ACOS≤60%，保持投放入口打开
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
            // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
            BigDecimal bd = new BigDecimal(targetBid);
            targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
            hasBidChanged = true; // 记录执行了bid变更
        } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
            // 25%＜ACOS≤35%，投放入口竞价≥CPC-0.01
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc - 0.01;
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
            hasBidChanged = true; // 记录执行了bid变更
        } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
            // 15%＜ACOS≤25%，投放入口竞价≥CPC
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, placementCpc, configuration);
            hasBidChanged = true; // 记录执行了bid变更
        } else if (placementAcos <= acosCpcPlus002) {
            // ACOS≤15%，投放入口竞价≥CPC+0.02
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc + 0.02;
            new AdPlacementUtils().addBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
            hasBidChanged = true; // 记录执行了bid变更
        } else {
            // ACOS>60%，超出保持打开范围，不做处理
            System.out.println(String.format("        投放入口ACOS>60%%，超出加流量范围，跳过: %s, ACOS=%.2f", 
                    adPlacement.getKeyword_text(), placementAcos));
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
    
    /**
     * 获取配置值，如果不存在则返回默认值
     */
    private int getConfigValue(Configuration configuration, String key, int defaultValue) {
        int value = configuration.getFeatures().getIntValue(key);
        return value > 0 ? value : defaultValue;
    }
}

