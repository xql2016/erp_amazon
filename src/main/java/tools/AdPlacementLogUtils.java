package tools;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.log.AdPlacementLogRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import model.response.log.AdPlacementLog;
import org.apache.commons.collections4.CollectionUtils;
import repository.read.AdPlacementLogReadRepository;

import java.util.ArrayList;
import java.util.List;

public class AdPlacementLogUtils {

    public boolean hasBidOperateLog(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration, int days) {
        String type = null;
        List<String> operateObject = new ArrayList<>();
        List<Long> targetId = new ArrayList<>();
        List<Long> keywordId = new ArrayList<>();
        switch (adGroupType) {
            case AUTO_AD_GROUP:
                type = "targets";
                operateObject.add("auto_target");
                targetId.add(adPlacement.getTarget_id());
                keywordId = null;
                break;
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                type = "targets";
                operateObject.add("product_target");
                targetId.add(adPlacement.getTarget_id());
                keywordId = null;
                break;
            case KEY_AD_GROUP:
                type = "keywords";
                operateObject.add("keyword");
                targetId = null;
                keywordId.add(adPlacement.getKeyword_id());
                break;
        }
        List<Long> campaignId = new ArrayList<>();
        campaignId.add(Long.parseLong(adGroup.getCampaign_id()));
        List<Long> adGroupId = new ArrayList<>();
        adGroupId.add(adGroup.getAd_group_id());
        List<String> operateType = new ArrayList<>();
        operateType.add("bid");

        AdPlacementLogRequest adPlacementLogRequest = new AdPlacementLogRequest();
        adPlacementLogRequest.setLog_type("api_log");
        adPlacementLogRequest.setOperate_object(operateObject);
        adPlacementLogRequest.setType(type);
        adPlacementLogRequest.setOperate_type(operateType);
        adPlacementLogRequest.setCampaign_id(campaignId);
        adPlacementLogRequest.setAd_group_id(adGroupId);
        adPlacementLogRequest.setKeyword_id(keywordId);
        adPlacementLogRequest.setTarget_id(targetId);
        adPlacementLogRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adPlacementLogRequest.setReport_date(DateUtils.buildReportDateString(days));
        adPlacementLogRequest.setShow_child(0);
        List<AdPlacementLog> logList = new AdPlacementLogReadRepository().queryAdGroupLogList(adPlacementLogRequest, configuration);
        return CollectionUtils.isNotEmpty(logList);
    }
}
