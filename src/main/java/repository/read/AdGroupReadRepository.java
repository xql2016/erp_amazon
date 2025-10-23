package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.request.AdGroupRequest;
import model.configuration.Configuration;
import model.response.AdGroup;
import model.response.AdGroupPageResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import tools.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 广告组读取接口
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/29
 */
public class AdGroupReadRepository {

    @SneakyThrows
    public AdGroupPageResult queryAdGroupSimpleInfoList(String cookie, String token, List<Long> hubIdList) {
        String url = "https://ads.lingxing.com/ad_report/suggestion/power_ad_group/portfolios";
        Map<String, String> header = new HashMap<String, String>();
        header.put("Accept", "application/json, text/javascript, */*; q=0.01");
        header.put("Accept-Language", "zh-CN,zh;q=0.9");
        header.put("Connection", "keep-alive");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        header.put("Origin", "https://ads.lingxing.com");
        header.put("Sec-Fetch-Site", "same-origin");
        header.put("X-AK-Company-Id", "901303884196873728");
        header.put("X-CSRF-TOKEN", token);
        header.put("Cookie", cookie);

        AdGroupRequest adGroupRequest = new AdGroupRequest();
        adGroupRequest.setProfile_ids(hubIdList);
        String result = HttpUtils.doPost(url, header, adGroupRequest, 10000, 10000, 5000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return convertToAdGroupPageResult(result);
    }


    // 分页查询广告组列表
    private AdGroupPageResult pageQueryAdGroup(AdGroupRequest adGroupRequest, Configuration configuration) throws Exception{
        String url = "https://ads.lingxing.com/ad_report/profile/campaign/index?ajax";
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

        String result = HttpUtils.doPost(url, header, adGroupRequest, 10000, 10000, 5000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return convertToAdGroupPageResult(result);
    }

    @SneakyThrows
    public List<AdGroup> queryAdGroupList(AdGroupRequest adGroupRequest, Configuration configuration) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<AdGroup> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            AdGroupRequest adGroupPageRequest = new AdGroupRequest();
            BeanUtils.copyProperties(adGroupPageRequest, adGroupRequest);
            adGroupPageRequest.setPage(page);
            adGroupPageRequest.setStart(start);
            adGroupPageRequest.setLength(pageSize);
            AdGroupPageResult adGroupPageResult = this.pageQueryAdGroup(adGroupPageRequest, configuration);
            if(null == adGroupPageResult || !adGroupPageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<AdGroup> adGroupList = adGroupPageResult.getData();
            if(CollectionUtils.isNotEmpty(adGroupList)) {
                result.addAll(adGroupList);
            }
            if(CollectionUtils.isEmpty(adGroupList) || adGroupList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        result = result.stream().filter(it -> StringUtils.isNotBlank(it.getName())).collect(Collectors.toList());
        return result;
    }

    private AdGroupPageResult pageQueryAdGroupInAdGroupPage(AdGroupRequest adGroupRequest, Configuration configuration) throws Exception{
        String url = "https://ads.lingxing.com/ad_report/ad_group/index/index?ajax";
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

        String result = HttpUtils.doPost(url, header, adGroupRequest, 10000, 10000, 5000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return convertToAdGroupPageResult(result);
    }

    @SneakyThrows
    public AdGroup queryAdGroupListInAdGroupPage(AdGroupRequest adGroupRequest, Configuration configuration) {
        int pageSize = 10;
        int pageMax = 10;
        int start = 0;

        List<AdGroup> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            AdGroupRequest adGroupPageRequest = new AdGroupRequest();
            BeanUtils.copyProperties(adGroupPageRequest, adGroupRequest);
            adGroupPageRequest.setPage(page);
            adGroupPageRequest.setStart(start);
            adGroupPageRequest.setLength(pageSize);
            AdGroupPageResult adGroupPageResult = this.pageQueryAdGroupInAdGroupPage(adGroupPageRequest, configuration);
            if(null == adGroupPageResult || !adGroupPageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<AdGroup> adGroupList = adGroupPageResult.getData();
            if(CollectionUtils.isNotEmpty(adGroupList)) {
                result.addAll(adGroupList);
            }
            if(CollectionUtils.isEmpty(adGroupList) || adGroupList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        result = result.stream().filter(it -> StringUtils.isNotBlank(it.getName())).collect(Collectors.toList());
        return result.get(0);
    }

    private AdGroupPageResult convertToAdGroupPageResult(String str) {
        if(StringUtils.isBlank(str)) {
            System.out.println("调用领星接口查询广告组报错,正在重试中");
            return null;
        }
        AdGroupPageResult adGroupPageResult = JSONObject.parseObject(str, AdGroupPageResult.class);
        if(CollectionUtils.isNotEmpty(adGroupPageResult.getData())) {
            adGroupPageResult.getData().forEach(it -> it.setName(StringEscapeUtils.unescapeJava(it.getName())));
        }
        return adGroupPageResult;
    }
}
