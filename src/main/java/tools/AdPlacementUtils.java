package tools;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;
import repository.write.AdWriteRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AdPlacementUtils {

    public String getAdPlacementName(AdGroupType adGroupType, AdPlacement adPlacement) {
        String adPlacementName = null;
        switch (adGroupType) {
            case KEY_AD_GROUP:
                adPlacementName = adPlacement.getKeyword_text();
                break;
            case AUTO_AD_GROUP:
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                adPlacementName = adPlacement.getTargeting_text_zh();
                break;
        }
        return adPlacementName;
    }

    public boolean subtractBid(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration) {
        Double nowBid = BidUtils.getAdPlacementBid(adPlacement);
        changeToBid = Math.max(0.02, changeToBid);
        BigDecimal bd = new BigDecimal(changeToBid);
        changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if(null != nowBid && nowBid <= changeToBid) {
            System.out.println(String.format("    店铺id=%s,名称=%s,bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
            return false;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(changeToBid);
            adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType);
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        } else {
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        }
        return true;
    }

    public boolean close(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if(!"enabled".equalsIgnoreCase(adPlacement.getState())) {
            System.out.println(String.format("    店铺id=%s,名称=%s,投放入口已关闭,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return false;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, adGroupType);
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-真实操作, 操作结果=%s, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        } else {
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-simulation, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        }
        return true;
    }

    public void addBidWithLog(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration, int days) {
        if(new AdPlacementLogUtils().hasBidOperateLog(adGroup, adGroupType, adPlacement, configuration, 1)) {
            System.out.println(String.format("    店铺id=%s,名称=%s,近%s天发生过bid操作,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),days + 1, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return;
        }
        addBid(adGroup, adGroupType, adPlacement, changeToBid, configuration);
    }

    public boolean addBid(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration) {
        Double nowBid = BidUtils.getAdPlacementBid(adPlacement);
        BigDecimal bd = new BigDecimal(changeToBid);
        changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if(null != nowBid && nowBid >= changeToBid) {
            System.out.println(String.format("    店铺id=%s,名称=%s,bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
            return false;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(changeToBid);
            adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType, 5);// 只尝试5次
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        } else {
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        }
        return true;
    }

    public void open(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if("enabled".equalsIgnoreCase(adPlacement.getState())) {
            System.out.println(String.format("    店铺id=%s,名称=%s,投放入口已打开,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, adGroupType);
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-真实操作, 操作结果=%s, 打开入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        } else {
            System.out.println(String.format("    店铺id=%s,名称=%s,操作-simulation, 打开入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()),adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        }
        return;
    }
}
