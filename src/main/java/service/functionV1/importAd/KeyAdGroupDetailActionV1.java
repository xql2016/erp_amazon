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
import tools.AdPlacementLogUtils;
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
     */
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
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
                return;
            }
            // 有广告订单，分析广告订单≥1的投放入口ACoS数据
            processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else if (clicks30Days < 15 && clicksYesterday >= 1) {
            // 广告组最近30天点击<15，昨天点击≥1
            if (!hasOrders) {
                // 无广告订单，不做处理
                return;
            }
            // 有广告订单，分析广告订单≥1的投放入口ACoS数据
            processPlacementsWithOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        } else {
            // 广告组最近30天点击<15，昨天点击<1；分析投放入口的点击和订单数据
            processPlacementsByClicksAndOrders(adGroup, adGroupType, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
        }
    }
    
    /**
     * 处理有订单的投放入口
     */
    private void processPlacementsWithOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                           int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
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
            
            // 只处理订单≥1的投放入口
            if (adPlacement.getOrders() < 1) {
                continue;
            }
            
            Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
            Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
            
            if (placementAcos == null || placementCpc == null) {
                continue;
            }
            
            // 检查昨天和今天是否有变更日志
            if (new AdPlacementLogUtils().hasBidOperateLog(adGroup, adGroupType, adPlacement, configuration, 1)) {
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
                new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
            } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
                // 25%＜ACOS≤35%，投放入口竞价≥CPC-0.01
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                double targetBid = placementCpc - 0.01;
                new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
            } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
                // 15%＜ACOS≤25%，投放入口竞价≥CPC
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, placementCpc, configuration, 1);
            } else if (placementAcos <= acosCpcPlus002) {
                // ACOS≤15%，投放入口竞价≥CPC+0.02
                new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                double targetBid = placementCpc + 0.02;
                new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
            }
        }
    }
    
    /**
     * 处理点击<15且昨天点击<1的情况，分析投放入口的点击和订单数据
     */
    private void processPlacementsByClicksAndOrders(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration,
                                                   int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
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
            
            if (adPlacement.getOrders() <= 0) {
                // 投放入口无广告订单
                if (adPlacement.getClicks() >= 1) {
                    // 投放入口点击≥1，不做处理
                    continue;
                }
                // 投放入口点击=0
                Double currentBid = BidUtils.getAdPlacementBid(adPlacement);
                if (currentBid == null) {
                    continue;
                }
                
                // 检查昨天和今天是否有变更日志
                if (new AdPlacementLogUtils().hasBidOperateLog(adGroup, adGroupType, adPlacement, configuration, 1)) {
                    continue;
                }
                
                if (currentBid <= 0.3) {
                    // Bid≤0.3，则将投放入口Bid增加0.02
                    new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, currentBid + 0.02, configuration, 1);
                } else if (currentBid > 0.3 && currentBid <= 0.45) {
                    // 0.3＜Bid≤0.45，则将投放入口Bid增加0.01
                    new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, currentBid + 0.01, configuration, 1);
                }
                // Bid＞0.45，不做处理
            } else {
                // 投放入口有广告订单，分析广告订单≥1的投放入口ACoS数据
                if (adPlacement.getOrders() >= 1) {
                    processPlacementWithOrder(adGroup, adGroupType, adPlacement, configuration, acosKeepOpen, acosControlBase, acosCpcMinus001, acosCpcPlus002);
                }
            }
        }
    }
    
    /**
     * 处理单个有订单的投放入口
     */
    private void processPlacementWithOrder(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration,
                                         int acosKeepOpen, int acosControlBase, int acosCpcMinus001, int acosCpcPlus002) {
        Double placementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
        Double placementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
        
        if (placementAcos == null || placementCpc == null) {
            return;
        }
        
        // 检查昨天和今天是否有变更日志
        if (new AdPlacementLogUtils().hasBidOperateLog(adGroup, adGroupType, adPlacement, configuration, 1)) {
            return;
        }
        
        if (placementAcos > acosControlBase && placementAcos <= acosKeepOpen) {
            // 35%＜ACOS≤60%，保持投放入口打开
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = (acosControlBase * placementCpc / placementAcos) - 0.01;
            // 【修复问题4】向上取整，保留两位小数（使用UP而不是HALF_UP）
            BigDecimal bd = new BigDecimal(targetBid);
            targetBid = bd.setScale(2, RoundingMode.UP).doubleValue();
            new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
        } else if (placementAcos > acosCpcMinus001 && placementAcos <= acosControlBase) {
            // 25%＜ACOS≤35%，投放入口竞价≥CPC-0.01
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc - 0.01;
            new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
        } else if (placementAcos > acosCpcPlus002 && placementAcos <= acosCpcMinus001) {
            // 15%＜ACOS≤25%，投放入口竞价≥CPC
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, placementCpc, configuration, 1);
        } else if (placementAcos <= acosCpcPlus002) {
            // ACOS≤15%，投放入口竞价≥CPC+0.02
            new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
            double targetBid = placementCpc + 0.02;
            new AdPlacementUtils().addBidWithLog(adGroup, adGroupType, adPlacement, targetBid, configuration, 1);
        }
    }
    
    /**
     * 获取配置值，如果不存在则返回默认值
     */
    private int getConfigValue(Configuration configuration, String key, int defaultValue) {
        int value = configuration.getFeatures().getIntValue(key);
        return value > 0 ? value : defaultValue;
    }
}

