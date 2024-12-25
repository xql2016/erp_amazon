package repository.write;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.*;
import model.response.OperateResult;
import tools.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
//@Slf4j
public class AdWriteRepository {

    public boolean doAdPlacementOperatePause(AdPlacementPauseRequest adPlacementPauseRequest, Configuration configuration, AdGroupType adGroupType) {
        AdPlacementOperateRequest adPlacementOperateRequest = new AdPlacementOperateRequest();
        AdPlacementOperateRequestSingle adPlacementOperateRequestSingle = new AdPlacementOperateRequestSingle();
        AdPlacementOperateRequestBatch adPlacementOperateRequestBatch = new AdPlacementOperateRequestBatch();
        switch (adGroupType) {
            case KEY_AD_GROUP:
                adPlacementOperateRequestSingle.setState("paused");
                adPlacementOperateRequestSingle.setKeywordId(adPlacementPauseRequest.getKeywordId());
                List<AdPlacementOperateRequestSingle> keywords = new ArrayList<>();
                keywords.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setKeywords(keywords);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_keywords");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementPauseRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
            case AUTO_AD_GROUP:
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                adPlacementOperateRequestSingle.setState("paused");
                adPlacementOperateRequestSingle.setTargetId(adPlacementPauseRequest.getTargetId());
                List<AdPlacementOperateRequestSingle> targetingClauses = new ArrayList<>();
                targetingClauses.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setTargetingClauses(targetingClauses);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_targets");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementPauseRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
        }


        return this.doAdPlacementOperateWithRetry(adPlacementOperateRequest, configuration, 5);
    }

    public boolean doAdPlacementOperateOpen(AdPlacementPauseRequest adPlacementPauseRequest, Configuration configuration, AdGroupType adGroupType) {
        AdPlacementOperateRequest adPlacementOperateRequest = new AdPlacementOperateRequest();
        AdPlacementOperateRequestSingle adPlacementOperateRequestSingle = new AdPlacementOperateRequestSingle();
        AdPlacementOperateRequestBatch adPlacementOperateRequestBatch = new AdPlacementOperateRequestBatch();
        switch (adGroupType) {
            case KEY_AD_GROUP:
                adPlacementOperateRequestSingle.setState("enabled");
                adPlacementOperateRequestSingle.setKeywordId(adPlacementPauseRequest.getKeywordId());
                List<AdPlacementOperateRequestSingle> keywords = new ArrayList<>();
                keywords.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setKeywords(keywords);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_keywords");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementPauseRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
            case AUTO_AD_GROUP:
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                adPlacementOperateRequestSingle.setState("enabled");
                adPlacementOperateRequestSingle.setTargetId(adPlacementPauseRequest.getTargetId());
                List<AdPlacementOperateRequestSingle> targetingClauses = new ArrayList<>();
                targetingClauses.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setTargetingClauses(targetingClauses);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_targets");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementPauseRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
        }


        return this.doAdPlacementOperateWithRetry(adPlacementOperateRequest, configuration, 5);
    }

    public boolean doAdPlacementOperateChangeBid(AdPlacementChangeBidRequest adPlacementChangeBidRequest, Configuration configuration, AdGroupType adGroupType, int tryTimes) {
        AdPlacementOperateRequestSingle adPlacementOperateRequestSingle = new AdPlacementOperateRequestSingle();
        AdPlacementOperateRequestBatch adPlacementOperateRequestBatch = new AdPlacementOperateRequestBatch();
        AdPlacementOperateRequest adPlacementOperateRequest = new AdPlacementOperateRequest();

        switch (adGroupType) {
            case KEY_AD_GROUP:
                adPlacementOperateRequestSingle.setBid(String.valueOf(adPlacementChangeBidRequest.getBid()));
                adPlacementOperateRequestSingle.setKeywordId(adPlacementChangeBidRequest.getKeywordId());
                adPlacementOperateRequestSingle.setIs_base_value(0);
                List<AdPlacementOperateRequestSingle> keywords = new ArrayList<>();
                keywords.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setKeywords(keywords);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_keywords");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementChangeBidRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
            case AUTO_AD_GROUP:
            case ASIN_AD_GROUP:
            case CATEGORY_AD_GROUP:
                adPlacementOperateRequestSingle.setBid(String.valueOf(adPlacementChangeBidRequest.getBid()));
                adPlacementOperateRequestSingle.setTargetId(adPlacementChangeBidRequest.getTargetId());
                adPlacementOperateRequestSingle.setIs_base_value(0);
                List<AdPlacementOperateRequestSingle> targetingClauses = new ArrayList<>();
                targetingClauses.add(adPlacementOperateRequestSingle);
                adPlacementOperateRequestBatch.setTargetingClauses(targetingClauses);

                adPlacementOperateRequest.setToken(configuration.getToken());
                adPlacementOperateRequest.setApi_method("put_targets");
                adPlacementOperateRequest.setApi_version("v3");
                adPlacementOperateRequest.setAd_type("sp");
                adPlacementOperateRequest.setOne_more(0);
                adPlacementOperateRequest.setProfile_id(adPlacementChangeBidRequest.getProfileId());
                adPlacementOperateRequest.setParams(JSONObject.toJSONString(adPlacementOperateRequestBatch));
                break;
        }

        return this.doAdPlacementOperateWithRetry(adPlacementOperateRequest, configuration, tryTimes);
    }

    public boolean doAdPlacementOperateChangeBid(AdPlacementChangeBidRequest adPlacementChangeBidRequest, Configuration configuration, AdGroupType adGroupType) {
        return this.doAdPlacementOperateChangeBid(adPlacementChangeBidRequest, configuration, adGroupType, 5);
    }


    private OperateResult doAdPlacementOperate(AdPlacementOperateRequest adPlacementOperateRequest, Configuration configuration) {
        String url = "https://ads.lingxing.com/ad_report/core/api/handle";
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
        header.put("Accept-Language", "zh-CN,zh;q=0.9");
        header.put("Connection", "keep-alive");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Origin", "https://ads.lingxing.com");
        header.put("Sec-Fetch-Site", "same-origin");
        header.put("X-AK-Company-Id", "901303884196873728");
        header.put("X-CSRF-TOKEN", configuration.getToken());
        header.put("Cookie", configuration.getCookie());

        String result = HttpUtils.doPost(url, header, adPlacementOperateRequest, 10000, 10000, 3000);
        return JSONObject.parseObject(result, OperateResult.class);
    }

    private boolean doAdPlacementOperateWithRetry(AdPlacementOperateRequest adPlacementOperateRequest, Configuration configuration, int retryTimes) {
        for(int i = 0 ; i < retryTimes; i ++) {
            OperateResult operateResult = this.doAdPlacementOperate(adPlacementOperateRequest, configuration);
            if(null != operateResult && 200 == operateResult.getCode()) {
                return true;
            }
        }
        return false;
    }

    public boolean doAdGroupOperateChangeBid(AdGroupChangeBidRequest adGroupChangeBidRequest, Configuration configuration, AdGroupType adGroupType) {
        AdGroupOperateRequestSingle adGroupOperateRequestSingle = new AdGroupOperateRequestSingle();
        adGroupOperateRequestSingle.setAdGroupId(adGroupChangeBidRequest.getAdGroupId());
        adGroupOperateRequestSingle.setDefaultBid(adGroupChangeBidRequest.getChangeToBid());
        adGroupOperateRequestSingle.setIs_base_value(0);

        List<AdGroupOperateRequestSingle> adGroupOperateRequestSingles = new ArrayList<>();
        adGroupOperateRequestSingles.add(adGroupOperateRequestSingle);

        AdGroupOperateRequestBatch adGroupOperateRequestBatch = new AdGroupOperateRequestBatch();
        adGroupOperateRequestBatch.setAdGroups(adGroupOperateRequestSingles);

        AdGroupOperateRequest adGroupOperateRequest = new AdGroupOperateRequest();
        adGroupOperateRequest.set_token(configuration.getToken());
        adGroupOperateRequest.setParams(JSONObject.toJSONString(adGroupOperateRequestBatch));
        adGroupOperateRequest.setProfile_id(adGroupChangeBidRequest.getProfileId());
        adGroupOperateRequest.setApi_method("put_adGroups");
        adGroupOperateRequest.setApi_version("v3");
        adGroupOperateRequest.setAd_type("sp");

        return this.doAdGroupOperateWithRetry(adGroupOperateRequest, configuration, 1);
    }

    private OperateResult doAdGroupOperate(AdGroupOperateRequest adGroupOperateRequest, Configuration configuration) {
        String url = "https://ads.lingxing.com/ad_report/core/api/handle";
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
        header.put("Accept-Language", "zh-CN,zh;q=0.9");
        header.put("Connection", "keep-alive");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Origin", "https://ads.lingxing.com");
        header.put("Sec-Fetch-Site", "same-origin");
        header.put("X-AK-Company-Id", "901303884196873728");
        header.put("X-CSRF-TOKEN", configuration.getToken());
        header.put("Cookie", configuration.getCookie());

        String result = HttpUtils.doPost(url, header, adGroupOperateRequest, 10000, 10000, 3000);
        return JSONObject.parseObject(result, OperateResult.class);
    }

    private boolean doAdGroupOperateWithRetry(AdGroupOperateRequest adGroupOperateRequest, Configuration configuration, int retryTimes) {
        for(int i = 0 ; i < retryTimes; i ++) {
            OperateResult operateResult = this.doAdGroupOperate(adGroupOperateRequest, configuration);
            if(null != operateResult && 200 == operateResult.getCode()) {
                return true;
            }
        }
        return false;
    }
}
