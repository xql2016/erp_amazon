package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.configuration.Configuration;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.keyPlace.KeyPlace;
import model.response.keyPlace.KeyPlacePageResult;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import tools.HttpUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 关键词投放查询接口
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class KeyPlaceReadRepository {

    @SneakyThrows
    public List<KeyPlace> queryKeyPlaceList(KeyPlaceRequest keyPlaceRequest, Configuration configuration) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<KeyPlace> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            KeyPlaceRequest keyPlacePageRequest = new KeyPlaceRequest();
            BeanUtils.copyProperties(keyPlacePageRequest, keyPlaceRequest);
            keyPlacePageRequest.setPage(page);
            keyPlacePageRequest.setStart(start);
            keyPlacePageRequest.setLength(pageSize);
            KeyPlacePageResult keyPlacePageResult = this.pageQueryKeyPlaceList(keyPlacePageRequest, configuration);
            if(null == keyPlacePageResult || !keyPlacePageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<KeyPlace> keyPlaceList = keyPlacePageResult.getData();
            if(CollectionUtils.isNotEmpty(keyPlaceList)) {
                result.addAll(keyPlaceList);
            }
            if(CollectionUtils.isEmpty(keyPlaceList) || keyPlaceList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        result = result.stream().filter(it -> StringUtils.isNotBlank(it.getAd_group_name())).collect(Collectors.toList());
        return result;
    }

    /**
     * https://ads.lingxing.com/ad_report/keyword/profile/index?ajax
     *
     * @param keyPlaceRequest
     * @param configuration
     * @return
     */
    public KeyPlacePageResult pageQueryKeyPlaceList(KeyPlaceRequest keyPlaceRequest, Configuration configuration) throws Exception {
        String url = "https://ads.lingxing.com/ad_report/keyword/profile/index?ajax";
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

        String result = HttpUtils.doPost(url, header, keyPlaceRequest, 10000, 10000, 3000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(CommonConstant.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, KeyPlacePageResult.class);
    }
}
