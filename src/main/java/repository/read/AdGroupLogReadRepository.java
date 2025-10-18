package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.configuration.Configuration;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.request.log.AdGroupLogRequest;
import model.response.log.AdGroupLog;
import model.response.log.AdGroupLogPageResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import tools.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdGroupLogReadRepository {

    @SneakyThrows
    public List<AdGroupLog> queryAdGroupLogList(AdGroupLogRequest adGroupLogRequest, Configuration configuration) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<AdGroupLog> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            AdGroupLogRequest request = new AdGroupLogRequest();
            BeanUtils.copyProperties(request, adGroupLogRequest);
            request.setPage(page);
            request.setStart(start);
            request.setLength(pageSize);
            AdGroupLogPageResult adGroupLogPageResult = this.pageQueryAdGroupLogList(request, configuration);
            if(null == adGroupLogPageResult || !adGroupLogPageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<AdGroupLog> adGroupLogList = adGroupLogPageResult.getData();
            if(CollectionUtils.isNotEmpty(adGroupLogList)) {
                result.addAll(adGroupLogList);
            }
            if(CollectionUtils.isEmpty(adGroupLogList) || adGroupLogList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        return result;
    }

    private AdGroupLogPageResult pageQueryAdGroupLogList(AdGroupLogRequest adGroupLogRequest, Configuration configuration) throws Exception {
        String url = "https://ads.lingxing.com/ad_report/api_log/profile/list";
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

        String result = HttpUtils.doPost(url, header, adGroupLogRequest, 10000, 10000, 5000);
        if (CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.HTTP_ERROR);
        }
        if (CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, AdGroupLogPageResult.class);
    }
}
