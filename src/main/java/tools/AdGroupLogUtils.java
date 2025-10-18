package tools;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.log.AdGroupLogRequest;
import model.response.AdGroup;
import model.response.log.AdGroupLog;
import org.apache.commons.collections4.CollectionUtils;
import repository.read.AdGroupLogReadRepository;

import java.util.ArrayList;
import java.util.List;

public class AdGroupLogUtils {

    public boolean hasBidOperateLog(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration, int days) {
        List<String> operateObject = new ArrayList<>();
        operateObject.add("campaign");
        List<String> operateType = new ArrayList<>();
        operateType.add("bid");
        List<Long> campaignId = new ArrayList<>();
        campaignId.add(Long.parseLong(adGroup.getCampaign_id()));
        AdGroupLogRequest adGroupLogRequest = new AdGroupLogRequest();
        adGroupLogRequest.setLog_type("api_log");
        adGroupLogRequest.setOperate_object(operateObject);
        adGroupLogRequest.setType("campaigns");
        adGroupLogRequest.setOperate_type(operateType);
        adGroupLogRequest.setCampaign_id(campaignId);
        adGroupLogRequest.setProfile_id(Long.parseLong(adGroup.getProfile_id()));
        adGroupLogRequest.setReport_date(DateUtils.buildReportDateString(days));
        adGroupLogRequest.setShow_child(0);
        List<AdGroupLog> adGroupLogList = new AdGroupLogReadRepository().queryAdGroupLogList(adGroupLogRequest, configuration);
        return CollectionUtils.isNotEmpty(adGroupLogList);
    }


}
