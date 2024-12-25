package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.configuration.Configuration;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.request.autoPlace.AutoPlaceRequest;
import model.response.autoPlace.AutoPlace;
import model.response.autoPlace.AutoPlacePageResult;
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
 * 自动投放查询接口
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class AutoPlaceReadRepository {

    @SneakyThrows
    public List<AutoPlace> queryAutoPlaceList(AutoPlaceRequest autoPlaceRequest, Configuration configuration) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<AutoPlace> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            AutoPlaceRequest autoPlacePageRequest = new AutoPlaceRequest();
            BeanUtils.copyProperties(autoPlacePageRequest, autoPlaceRequest);
            autoPlacePageRequest.setPage(page);
            autoPlacePageRequest.setStart(start);
            autoPlacePageRequest.setLength(pageSize);
            AutoPlacePageResult autoPlacePageResult = this.pageQueryAutoPlaceList(autoPlacePageRequest, configuration);
            if(null == autoPlacePageResult || !autoPlacePageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<AutoPlace> autoPlaceList = autoPlacePageResult.getData();
            if(CollectionUtils.isNotEmpty(autoPlaceList)) {
                result.addAll(autoPlaceList);
            }
            if(CollectionUtils.isEmpty(autoPlaceList) || autoPlaceList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        result = result.stream().filter(it -> StringUtils.isNotBlank(it.getAd_group_name())).collect(Collectors.toList());
        return result;
    }

    /**
     * https://ads.lingxing.com/ad_report/target/auto/index?ajax
     *
     * @param autoPlaceRequest
     * @param configuration
     * @return
     */
    public AutoPlacePageResult pageQueryAutoPlaceList(AutoPlaceRequest autoPlaceRequest, Configuration configuration) throws Exception {
        String url = "https://ads.lingxing.com/ad_report/target/auto/index?ajax";
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

        String result = HttpUtils.doPost(url, header, autoPlaceRequest, 10000, 10000, 3000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(CommonConstant.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, AutoPlacePageResult.class);
    }
}
