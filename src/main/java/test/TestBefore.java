package test;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementChangeBidRequest;
import model.request.AdPlacementPauseRequest;
import model.request.AdPlacementRequest;
import model.response.AdPlacement;
import repository.read.AdReadRepository;
import repository.write.AdWriteRepository;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
public class TestBefore {
//    public static void main(String[] args) {
//        doOperate();
//    }

    public static void doOperate() {
        /**
         * 操作ad group - 关键词/asin/自动/category
         * https://ads.lingxing.com/ad_report/core/api/handle
         * profile_id: 1769812645248266
         * _token: 2pONKGfrHr1vup9ra5RQACgoW8Sa4sP6m08TWgzB
         * api_method: put_adGroups
         * api_version: v3
         * ad_type: sp
         * params: {"adGroups":[{"defaultBid":"0.16","adGroupId":477228857436742,"is_base_value":0}]}
         */

        Configuration configuration = new Configuration();
        configuration.setCookie("amzbi=nfgj8w5JTG458RIv4TexbPVh3u6Oh2YOL23yBlid");
        configuration.setToken("VnzXz21iwswrUzDoEFB9HNlBHklMAcPsh6kG2QAG");

        //AdWriteRepository adWriteRepository = new AdWriteRepository();
//        AdPlacementPauseRequest adPlacementPauseRequest = new AdPlacementPauseRequest();
//        adPlacementPauseRequest.setTargetId(57863557488741L);
//        adPlacementPauseRequest.setProfileId(1769812645248266L);
//        adWriteRepository.doAdPlacementOperatePause(adPlacementPauseRequest, configuration, AdGroupType.ASIN_AD_GROUP);
//
//        AdPlacementChangeBidRequest adPlacementChangeBidRequest = new AdPlacementChangeBidRequest();
//        adPlacementChangeBidRequest.setBid(0.32);
//        adPlacementChangeBidRequest.setTargetId(57863557488741L);
//        adPlacementChangeBidRequest.setProfileId(1769812645248266L);
//        adWriteRepository.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, AdGroupType.ASIN_AD_GROUP);


//        AdGroupReadRepository adGroupReadRepository = new AdGroupReadRepository();
//        List<Long> profileIds = new ArrayList<>();
//        profileIds.add(1769812645248266l);
//        profileIds.add(4318992733363080l);
//        profileIds.add(2045052115486676l);
//        profileIds.add(4038430271573273l);
//        List<Double> spends = new ArrayList<>();
//        spends.add(0.5);
//        spends.add(100000000.0);
//        AdGroupRequest adGroupRequest = new AdGroupRequest();
//        LocalDate date30DaysAgo = LocalDate.now().minusDays(30);
//        LocalDate date1DaysAgo = LocalDate.now();
//        adGroupRequest.setReport_date(String.format("%s - %s", date30DaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), date1DaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        adGroupRequest.setProfile_ids(profileIds);
//        adGroupRequest.setSpends(spends);
//        List<AdGroup> adGroupList = adGroupReadRepository.queryAdGroupList(adGroupRequest, configuration);
//
//        AdReadRepository adReadRepository = new AdReadRepository();
//        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
//        adPlacementRequest.setProfile_id(2045052115486676l);
//        adPlacementRequest.setCampaign_id(467955609588574l);
//        adPlacementRequest.setReport_date("2024-08-20 - 2024-09-18");
//        adPlacementRequest.setPage(1);
//        adPlacementRequest.setLength(100);
//        adPlacementRequest.setStart(0);
//        List<AdPlacement> list = adReadRepository.queryAdPlacementList(adPlacementRequest, configuration, AdGroupType.AUTO_AD_GROUP);

//        List<AdPlacementRequest> adPlacementRequestList = new ArrayList<>();
//        AdPlacementRequest adPlacementRequest1 = new AdPlacementRequest();
//        adPlacementRequest1.setProfile_id(1769812645248266l);//仓id
//        adPlacementRequest1.setCampaign_id(552573962166633l);//广告组id
//        adPlacementRequest1.setReport_date("2024-08-04 - 2024-09-02");
//        adPlacementRequestList.add(adPlacementRequest1);
//        AdPlacementRequest adPlacementRequest2 = new AdPlacementRequest();
//        adPlacementRequest2.setProfile_id(1769812645248266l);
//        adPlacementRequest2.setCampaign_id(333691287872573l);
//        adPlacementRequest2.setReport_date("2024-08-04 - 2024-09-02");
//        adPlacementRequestList.add(adPlacementRequest2);
//        AdPlacementRequest adPlacementRequest3 = new AdPlacementRequest();
//        adPlacementRequest3.setProfile_id(2045052115486676l);
//        adPlacementRequest3.setCampaign_id(411705691932732l);
//        adPlacementRequest3.setReport_date("2024-08-04 - 2024-09-02");
//        adPlacementRequestList.add(adPlacementRequest3);
//
//        new AdControlAction().adListHandle(adPlacementRequestList, configuration);

    }
}
