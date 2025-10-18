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

    public void subtractBid(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration) {
        String nowBid = adPlacement.getBid();
        changeToBid = Math.max(0.02, changeToBid);
        BigDecimal bd = new BigDecimal(changeToBid);
        changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if(StringUtils.isNotBlank(nowBid) && Double.parseDouble(nowBid) <= changeToBid) {
            System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(changeToBid);
            adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType);
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        } else {
            System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        }
        return;
    }

    public void close(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if(!"enabled".equalsIgnoreCase(adPlacement.getState())) {
            System.out.println(String.format("    投放入口已关闭,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, adGroupType);
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        } else {
            System.out.println(String.format("    操作-simulation, 关闭入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        }
        return;
    }

    public void addBidWithLog(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration, int days) {
        if(new AdPlacementLogUtils().hasBidOperateLog(adGroup, adGroupType, adPlacement, configuration, days)) {
            System.out.println(String.format("    近%s天发生过bid操作,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", days + 1, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return;
        }
        addBid(adGroup, adGroupType, adPlacement, changeToBid, configuration);
    }

    public void addBid(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, double changeToBid, Configuration configuration) {
        String nowBid = adPlacement.getBid();
        BigDecimal bd = new BigDecimal(changeToBid);
        changeToBid = bd.setScale(2, RoundingMode.HALF_UP).doubleValue();
        if(StringUtils.isNotBlank(nowBid) && Double.parseDouble(nowBid) >= changeToBid) {
            System.out.println(String.format("    bid当前符合要求,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, nowBid %s changeToBid %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
            adPlacementChangeBidRequest.setBid(changeToBid);
            adPlacementChangeBidRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementChangeBidRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementChangeBidRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType, 5);// 只尝试5次
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        } else {
            System.out.println(String.format("    操作-simulation, 变更bid, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s, from %s to %s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc(), nowBid, changeToBid));
        }
        return;
    }

    public void open(AdGroup adGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if("enabled".equalsIgnoreCase(adPlacement.getState())) {
            System.out.println(String.format("    投放入口已打开,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            return;
        }
        if(!configuration.isDoSimulation()) {
            AdWriteRepository adWriteRepository = new AdWriteRepository();
            AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
            adPlacementPauseRequest.setTargetId(adPlacement.getTarget_id());
            adPlacementPauseRequest.setProfileId(Long.parseLong(adGroup.getProfile_id()));
            adPlacementPauseRequest.setKeywordId(adPlacement.getKeyword_id());
            boolean operateResult = adWriteRepository.doAdPlacementOperateOpen(adPlacementPauseRequest, configuration, adGroupType);
            System.out.println(String.format("    操作-真实操作, 操作结果=%s, 打开入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", operateResult, adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        } else {
            System.out.println(String.format("    操作-simulation, 打开入口, country=%s, adGroupName=%s, placementName=%s, acos =%s, click=%s", adGroup.getStore_country(), adPlacement.getAd_group_name(), getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks()));
        }
        return;
    }
}
