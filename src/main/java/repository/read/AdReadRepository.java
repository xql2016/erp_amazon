package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.enums.AdGroupType;
import model.request.AdPlacementRequest;
import model.configuration.Configuration;
import model.response.AdPlacement;
import model.response.AdPlacementPageResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import tools.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/30
 */
public class AdReadRepository {

    // 广告投放信息分页查询
    private AdPlacementPageResult pageQueryKeywordAdPlacement(AdPlacementRequest adPlacementRequest, Configuration configuration) throws Exception{
        String url = "https://ads.lingxing.com/ad_report/keyword/index/index?ajax";
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
        header.put("Accept-Language", "zh-CN,zh;q=0.9");
        header.put("Connection", "keep-alive");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Origin", "https://ads.lingxing.com");
        header.put("Sec-Fetch-Site", "same-origin");
        header.put("X-AK-Company-Id", "901303884196873728");
        header.put("X-CSRF-TOKEN", configuration.getToken());
        header.put("Cookie", configuration.getCookie());

        String result = HttpUtils.doPost(url, header, adPlacementRequest, 10000, 10000, 3000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(CommonConstant.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, AdPlacementPageResult.class);
    }

    private AdPlacementPageResult pageQueryAutoAndAsinAndCategoryAdPlacement(AdPlacementRequest adPlacementRequest, Configuration configuration) throws Exception{
        String url = "https://ads.lingxing.com/ad_report/target/index/index?ajax";
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
        header.put("Accept-Language", "zh-CN,zh;q=0.9");
        header.put("Connection", "keep-alive");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Origin", "https://ads.lingxing.com");
        header.put("Sec-Fetch-Site", "same-origin");
        header.put("X-AK-Company-Id", "901303884196873728");
        header.put("X-CSRF-TOKEN", configuration.getToken());
        header.put("Cookie", configuration.getCookie());

        String result = HttpUtils.doPost(url, header, adPlacementRequest, 10000, 10000, 3000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(CommonConstant.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, AdPlacementPageResult.class);
    }

    // 关键词广告投放信息列表查询
    @SneakyThrows
    public List<AdPlacement> queryAdPlacementList(AdPlacementRequest adPlacementRequest, Configuration configuration, AdGroupType adGroupType) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<AdPlacement> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            AdPlacementRequest adPlacementPageRequest = new AdPlacementRequest();
            BeanUtils.copyProperties(adPlacementPageRequest, adPlacementRequest);
            adPlacementPageRequest.setPage(page);
            adPlacementPageRequest.setStart(start);
            adPlacementPageRequest.setLength(pageSize);
            AdPlacementPageResult adPlacementPageResult = null;
            switch (adGroupType) {
                case CATEGORY_AD_GROUP:
                case AUTO_AD_GROUP:
                case ASIN_AD_GROUP:
                    adPlacementPageResult = this.pageQueryAutoAndAsinAndCategoryAdPlacement(adPlacementPageRequest, configuration);
                    break;
                case KEY_AD_GROUP:
                    adPlacementPageResult = this.pageQueryKeywordAdPlacement(adPlacementPageRequest, configuration);
                    break;
            }
            if(null == adPlacementPageResult || !adPlacementPageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<AdPlacement> adPlacementList = adPlacementPageResult.getData();
            if(CollectionUtils.isNotEmpty(adPlacementList)) {
                result.addAll(adPlacementList);
            }
            if(CollectionUtils.isEmpty(adPlacementList) || adPlacementList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        return result;

    }
}
