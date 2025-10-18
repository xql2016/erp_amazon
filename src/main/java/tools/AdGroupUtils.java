package tools;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdGroupChangeBidRequest;
import model.response.AdGroup;
import repository.write.AdWriteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AdGroupUtils {

    public void addBidWithLog(AdGroup adGroup, AdGroupType adGroupType, double changeToBid, Configuration configuration, int days) {
        // todo 如果days里面有变更bid日志则不再变更
        addBid(adGroup, adGroupType, changeToBid, configuration);
    }

    public void addBid(AdGroup adGroup, AdGroupType adGroupType, double changeToBid, Configuration configuration) {
        BigDecimal bd = new BigDecimal(changeToBid);
        changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        Double nowBid = Double.parseDouble(adGroup.getDefault_bid());
        if(nowBid >= changeToBid) {
            System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, click=%s, 曝光=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), nowBid, changeToBid));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdGroupChangeBidRequest adGroupChangeBidRequest = new AdGroupChangeBidRequest();
            adGroupChangeBidRequest.setChangeToBid(changeToBid);
            adGroupChangeBidRequest.setAdGroupId(adGroup.getAd_group_id());
            adGroupChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            boolean operateResult = adWriteRepository.doAdGroupOperateChangeBid(adGroupChangeBidRequest, configuration, adGroupType);
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, click=%s, 曝光=%s, nowBid %s changeToBid %s", operateResult, adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), nowBid, changeToBid));
        } else {
            System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, click=%s, 曝光=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adGroup.getName(), adGroup.getClicks(), adGroup.getImpressions(), nowBid, changeToBid));
        }
    }
}
