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
import tools.HubUtils;
import tools.NumberUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * V1产品层面关键词广告组控制流量处理类
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class KeyAdGroupDetailActionV1 {

    /**
     * 处理关键词广告组的控制流量逻辑
     * 
     * @param adGroup 广告组数据（近30天）
     * @param adGroupType 广告组类型
     * @param configuration 配置信息
     */
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 获取配置项：V1产品层面关键词广告组长期ACOS控制基准上限（默认35）
        String configKey = "V1产品层面关键词广告组长期ACOS控制基准上限";
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
        // 原代码错误：满足高点击无转化时直接返回，导致投放入口未处理
        // 修复后：记录标记，在投放入口处理时使用不同的Bid计算公式
        boolean isHighClickNoConversion = condition1 || condition2;
        
        // 查询投放入口近30天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        
        if (adPlacementList == null || adPlacementList.isEmpty()) {
            // 投放入口列表为空，跳过该广告组
            return;
        }
        
        for (AdPlacement adPlacement : adPlacementList) {
            // 第一行统计不做处理
            if (StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            
            // 投放入口有广告订单
            if (adPlacement.getOrders() > 0) {
                // 分析ACoS数据
                Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
                if (placementAcos != null && placementAcos > acosControlBase) {
                    // ACoS > 35%，将竞价调低为【35%*CPC/ACoS】-0.01
                    Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                    if (placementCpc != null && placementCpc > 0) {
                        double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
                        // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
                        // 原代码使用HALF_UP（四舍五入），策略文档要求"向上取"
                        // 例如：0.234 → 0.24（而不是0.23）
                        BigDecimal bd = new BigDecimal(targetBid);
                        targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
                        
                        // 【优化】只有当前Bid为null或者当前Bid > 目标Bid时才需要降低竞价
                        // 如果当前Bid <= 目标Bid，说明已经比目标更低，不需要变更
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                        }
                    }
                }
            } else {
                // 投放入口没有广告订单，分析投放入口点击数
                int placementClicks = adPlacement.getClicks();
                Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                
                if (placementClicks >= 10) {
                    // 投放入口点击≥10，关闭该入口
                    new AdPlacementUtils().close(adGroup, adGroupType, adPlacement, configuration);
                } else if (placementClicks >= 4) {
                    // 【修复问题1】10 > 投放入口点击数≥4
                    // 根据是否为"高点击无转化"情况使用不同公式：
                    // - 情况A（高点击无转化）：CPC + 0.02 - 0.01 × 点击数
                    // - 情况B（非高点击无转化）：CPC + 0.04 - 0.01 × 点击数
                    if (placementCpc != null) {
                        double offset = isHighClickNoConversion ? 0.02 : 0.04;
                        double targetBid = placementCpc + offset - 0.01 * placementClicks;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        // 【优化】当前Bid为null或者大于目标Bid时才降低竞价
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                        }
                    }
                } else if (placementClicks > 0) {
                    // 【修复问题1】4 > 投放入口点击数 > 0
                    // 根据是否为"高点击无转化"情况使用不同公式：
                    // - 情况A（高点击无转化）：CPC - 0.02
                    // - 情况B（非高点击无转化）：CPC
                    if (placementCpc != null) {
                        double targetBid = isHighClickNoConversion ? (placementCpc - 0.02) : placementCpc;
                        Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                        // 【优化】当前Bid为null或者大于目标Bid时才降低竞价
                        if (currentBid == null || currentBid > targetBid) {
                            new AdPlacementUtils().subtractBid(adGroup, adGroupType, adPlacement, targetBid, configuration);
                        }
                    }
                }
                // 投放入口点击数=0，不做处理
            }
        }
    }
}

