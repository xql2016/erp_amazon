package service.functionV3Enhanced;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.response.AdGroup;
import repository.read.AdGroupReadRepository;
import tools.AdGroupUtils;
import tools.DateUtils;
import tools.HubUtils;
import tools.NumberUtils;

public class CategoryAdGroupDetailAction {

    // 近3天曝光≤20，点进去看最近60天广告数据
    // 近60天总点击数＜10
    //      广告组竞价≤0.3，广告组竞价调高0.02
    //      广告组竞价＞0.3，不做处理
    // 近60天总点击数≥10，不做处理
    // 近3天曝光>20，不做处理
    public void executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        if(adGroup.getImpressions() > 20) {
            // 打日志,不做处理
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组近3天曝光大于20,无需变更, country=%s, adGroupName=%s, 近3天曝光=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adGroup.getName(), adGroup.getImpressions()));
            return;
        }
        // 2. 查看广告组近60天的的数据
        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setReport_date(DateUtils.buildReportDateString(59));
        adGroupRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adGroupRequest.setCampaign_id(Long.parseLong(adGroup.getCampaign_id()));
        AdGroup newAdGroup = new AdGroupReadRepository().queryAdGroupListInAdGroupPage(adGroupRequest, configuration);
        // 3. 近60天的总点击数>=10,不处理
        if(newAdGroup.getClicks() >= 10) {
            // 打日志,不做处理
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组近3天曝光小于等于20且近60天点击大于等于10,无需变更, country=%s, adGroupName=%s, 近3天曝光=%s, 近60天点击=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adGroup.getName(), adGroup.getImpressions(), newAdGroup.getClicks()));
            return;
        }
        Double bidValue = NumberUtils.parseDouble(newAdGroup.getDefault_bid());
        if(null == bidValue|| bidValue > 0.4) {
            // 打日志,不做处理
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组近3天曝光小于等于20且近60天点击小于10且bid大于0.4,无需变更, country=%s, adGroupName=%s, 近3天曝光=%s, 近60天点击=%s, bid=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adGroup.getName(), adGroup.getImpressions(), newAdGroup.getClicks(), bidValue));
            return;
        }
        if(bidValue > 0.3) {
            // 昨天和今天有变更日志则不处理
            new AdGroupUtils().addBidWithLog(newAdGroup, adGroupType, bidValue + 0.01, configuration, 1);
        } else {
            // 昨天和今天有变更日志则不处理
            new AdGroupUtils().addBidWithLog(newAdGroup, adGroupType, bidValue + 0.02, configuration, 1);
        }
    }
}
