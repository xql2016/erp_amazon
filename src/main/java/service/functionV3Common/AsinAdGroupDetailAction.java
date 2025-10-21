package service.functionV3Common;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import repository.read.AdReadRepository;
import tools.*;

import java.util.List;

public class AsinAdGroupDetailAction {

    // 昨天广告组点击=0，点进去看前天广告数据
    //     前天广告组点击数≤1，分析60天投放入口的点击和订单数据
    //     前天广告组点击数＞1，不做处理
    // 昨天广告组点击＞0，不做处理
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        // 1. 查看广告组昨天的的数据
        AdGroupRequest d1AdGroupRequest = new AdGroupRequest();
        d1AdGroupRequest.setReport_date(DateUtils.buildReportDateString(1,1));
        d1AdGroupRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        d1AdGroupRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        AdGroup d1AdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(d1AdGroupRequest, configuration);
        if(d1AdGroup.getClicks() > 0) {
            // 打日志,不做处理
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组昨天点击>0,无需变更, country=%s, adGroupName=%s, 昨天曝光=%s, 昨天点击=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adGroup.getName(), d1AdGroup.getImpressions(), d1AdGroup.getClicks()));
            return;
        }
        // 2. 查看广告组前天的数据
        AdGroupRequest d2AdGroupRequest = new AdGroupRequest();
        d2AdGroupRequest.setReport_date(DateUtils.buildReportDateString(2,2));
        d2AdGroupRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        d2AdGroupRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        AdGroup d2AdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(d2AdGroupRequest, configuration);
        if(d2AdGroup.getClicks() > 1) {
            // 打日志,不做处理
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组昨天点击=0且前天点击>1,无需变更, country=%s, adGroupName=%s, 昨天曝光=%s, 昨天点击=%s, 前天曝光=%s, 前天点击=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adGroup.getName(), d1AdGroup.getImpressions(), d1AdGroup.getClicks(), d2AdGroup.getImpressions(), d2AdGroup.getClicks()));
            return;
        }
        // 3. 查看投放入口近60天数据
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(59));
        adPlacementRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        for(AdPlacement adPlacement : adPlacementList) {
            // 第一行统计不做处理
            if(StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            if(adPlacement.getOrders() <= 0) {
                // 投放入口无广告订单
                if(adPlacement.getClicks() >= 1) {
                    // 不做处理,打日志
                    System.out.println(String.format("    店铺id=%s,名称=%s,前天广告组点击为0且60天投放入口无广告订单且点击大于等于1当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
                    return;
                } else {
                    // 投放入口Bid≤0.3，则将投放入口Bid增加0.02
                    // 投放入口Bid＞0.3，不做处理
                    Double adPlacementBid = BidUtils.getAdPlacementBid(adPlacement);
                    if(null == adPlacementBid || adPlacementBid > 0.3) {
                        // 不做处理,打日志
                        System.out.println(String.format("    店铺id=%s,名称=%s,前天广告组点击为0且60天投放入口无广告订单且bid大于0.3当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
                        return;
                    } else {
                        // 昨天和今天有变更日志则不处理
                        new AdPlacementUtils().addBidWithLog(d2AdGroup, adGroupType, adPlacement,adPlacementBid + 0.02, configuration, 1);
                    }
                }
            } else {
                // 投放入口有广告订单
                Double adPlacementAcos = NumberUtils.parseDouble(adPlacement.getAcos());
                Double adPlacementCpc = NumberUtils.parseDouble(adPlacement.getCpc());
                if(adPlacementAcos > 40) {
                    // 不做处理,打日志
                    System.out.println(String.format("    店铺id=%s,名称=%s,前天广告组点击为0且60天投放入口有广告订单且acos大于40当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
                    return;
                } else if(adPlacementAcos > 30) {
                    // 投放入口打开
                    new AdPlacementUtils().open(adGroup, adGroupType, adPlacement, configuration);
                } else if(adPlacementAcos > 15) {
                    new AdPlacementUtils().addBidWithLog(d2AdGroup, adGroupType, adPlacement,adPlacementCpc - 0.01, configuration, 1);
                } else {
                    new AdPlacementUtils().addBidWithLog(d2AdGroup, adGroupType, adPlacement,adPlacementCpc, configuration, 1);
                }
            }
        }
    }
}
