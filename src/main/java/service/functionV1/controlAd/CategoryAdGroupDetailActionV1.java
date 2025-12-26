package service.functionV1.controlAd;

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
 * V1产品层面类目广告组控制流量处理类
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class CategoryAdGroupDetailActionV1 {

    /**
     * 处理类目广告组的控制流量逻辑
     * 
     * @return 是否执行了bid变更操作（true=执行了变更，false=符合策略未变更）
     */
    public boolean executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        boolean hasBidChanged = false; // 追踪是否执行了bid变更
        
        // 获取配置项：V1产品层面类目广告组长期ACOS控制基准上限（默认35）
        String configKey = "V1产品层面类目广告组长期ACOS控制基准上限";
        int acosControlBase = configuration.getFeatures().getIntValue(configKey);
        if (acosControlBase <= 0) {
            acosControlBase = 35; // 默认值
        }
        
        // 【修复问题2】判断广告组是否满足"高点击无转化"条件
        // 条件1：点击数 >= 20 && 花费>5欧 && 无订单
        // 条件2：点击数 >= 20 && CPA > 5（注意：策略文档要求是>而不是>=）
        Double spends = NumberUtils.parseDouble(adGroup.getSpends());
        boolean hasOrders = adGroup.getOrders() != null && adGroup.getOrders() > 0;
        Double cpa = NumberUtils.parseDouble(adGroup.getCpa());
        
        boolean condition1 = adGroup.getClicks() >= 20 && spends != null && spends > 5 && !hasOrders;
        boolean condition2 = adGroup.getClicks() >= 20 && cpa != null && cpa > 5; // 修改：>= 改为 >
        
        // 【修复问题1】标记是否为"高点击无转化"情况，但继续处理投放入口
        boolean isHighClickNoConversion = condition1 || condition2;
        
        // 查询投放入口近30天数据
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
            
            if (adPlacement.getOrders() > 0) {
                // 投放入口有广告订单，分析ACoS数据
                Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
                if (placementAcos != null && placementAcos > acosControlBase) {
                    Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                    if (placementCpc != null && placementCpc > 0) {
                        double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
                        // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
                        BigDecimal bd = new BigDecimal(targetBid);
                        targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
                        
                        // 【优化】只有当前Bid为null或者当前Bid > 目标Bid时才需要降低竞价
                        // 如果当前Bid <= 目标Bid，说明已经比目标更低，不需要变更
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                            hasBidChanged = true; // 记录执行了bid变更
                        } else {
                            System.out.println(String.format("        投放入口Bid已达标，跳过: %s, currentBid=%.2f, targetBid=%.2f", 
                                    adPlacement.getTargeting_text_zh(), currentBid, targetBid));
                        }
                    } else {
                        System.out.println(String.format("        投放入口CPC异常(null或≤0)，跳过降Bid: %s, ACOS=%.2f", 
                                adPlacement.getTargeting_text_zh(), placementAcos));
                    }
                } else {
                    // 【场景3】有订单但ACOS≤35%，符合策略不处理
                    if (placementAcos != null) {
                        System.out.println(String.format("        投放入口ACOS达标，无需降Bid: %s, ACOS=%.2f, 基准=%d%%", 
                                adPlacement.getTargeting_text_zh(), placementAcos, acosControlBase));
                    }
                }
            } else {
                // 【重要】类目广告组投放入口没有广告订单的处理逻辑与关键词/ASIN完全不同！
                // 策略文档V1_strategy.md 第110-130行定义了类目广告组的特殊规则
                // 类目广告组策略与自动广告组完全相同
                int placementClicks = adPlacement.getClicks();
                Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                
                if (placementClicks >= 20) {
                    // 【类目广告组特殊规则】点击≥20，将Bid改为0.02（而不是关闭投放入口）
                    // 这与关键词/ASIN的"点击≥10关闭"完全不同
                    new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, 0.02, configuration);
                    hasBidChanged = true; // 记录执行了bid变更
                    
                } else if (placementClicks >= 10) {
                    // 【类目广告组特殊规则】10≤点击<20区间处理
                    // 情况A（高点击无转化）：CPC + 0.07 - 0.01 × 点击数
                    // 情况B（非高点击无转化）：CPC + 0.08 - 0.01 × 点击数
                    if (placementCpc != null) {
                        double offset = isHighClickNoConversion ? 0.07 : 0.08;
                        double targetBid = placementCpc + offset - 0.01 * placementClicks;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        // 【优化】当前Bid为null或者大于目标Bid时才降低竞价
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                            hasBidChanged = true; // 记录执行了bid变更
                        } else {
                            System.out.println(String.format("        投放入口Bid已达标，跳过: %s, currentBid=%.2f, targetBid=%.2f", 
                                    adPlacement.getTargeting_text_zh(), currentBid, targetBid));
                        }
                    } else {
                        System.out.println(String.format("        投放入口CPC为null，无法计算目标Bid: %s, 点击=%d", 
                                adPlacement.getTargeting_text_zh(), placementClicks));
                    }
                    
                } else if (isHighClickNoConversion && placementClicks > 0) {
                    // 【类目广告组特殊规则】高点击无转化，0<点击<10
                    // 统一使用：CPC - 0.02
                    // 注意：这与关键词/ASIN的分段处理（4-10和1-4）完全不同
                    if (placementCpc != null) {
                        double targetBid = placementCpc - 0.02;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        // 【优化】当前Bid为null或者大于目标Bid时才降低竞价
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                            hasBidChanged = true; // 记录执行了bid变更
                        } else {
                            System.out.println(String.format("        投放入口Bid已达标，跳过: %s, currentBid=%.2f, targetBid=%.2f", 
                                    adPlacement.getTargeting_text_zh(), currentBid, targetBid));
                        }
                    } else {
                        System.out.println(String.format("        投放入口CPC为null，无法计算目标Bid: %s, 点击=%d", 
                                adPlacement.getTargeting_text_zh(), placementClicks));
                    }
                    
                } else if (!isHighClickNoConversion && placementClicks >= 4) {
                    // 【类目广告组特殊规则】非高点击无转化，4≤点击<10
                    // 使用：CPC - 0.01
                    // 注意：关键词/ASIN用的是 CPC + 0.04 - 0.01 × 点击数
                    if (placementCpc != null) {
                        double targetBid = placementCpc - 0.01;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        // 【优化】当前Bid为null或者大于目标Bid时才降低竞价
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                            hasBidChanged = true; // 记录执行了bid变更
                        } else {
                            System.out.println(String.format("        投放入口Bid已达标，跳过: %s, currentBid=%.2f, targetBid=%.2f", 
                                    adPlacement.getTargeting_text_zh(), currentBid, targetBid));
                        }
                    } else {
                        System.out.println(String.format("        投放入口CPC为null，无法计算目标Bid: %s, 点击=%d", 
                                adPlacement.getTargeting_text_zh(), placementClicks));
                    }
                } else {
                    // 【场景4】无订单且点击=0，符合策略不处理
                    System.out.println(String.format("        投放入口无点击数据，跳过: %s", 
                            adPlacement.getTargeting_text_zh()));
                }
                // 【类目广告组特殊规则】非高点击无转化，0<点击<4：不做处理
                // 注意：关键词/ASIN在此情况下会降Bid至CPC
                if (!isHighClickNoConversion && placementClicks > 0 && placementClicks < 4) {
                    // 【场景5】无订单非高点击无转化且0<点击<4，符合策略不处理
                    System.out.println(String.format("        投放入口点击数太少(非高点击无转化)，跳过: %s, 点击=%d", 
                            adPlacement.getTargeting_text_zh(), placementClicks));
                }
            }
        }
        return hasBidChanged; // 返回是否执行了bid变更
    }
}

