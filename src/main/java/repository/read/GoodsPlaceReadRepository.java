package repository.read;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import model.configuration.Configuration;
import model.constant.CommonConstant;
import model.constant.ErrorMessage;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.response.goodsPlace.GoodsPlace;
import model.response.goodsPlace.GoodsPlacePageResult;
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
 * 商品投放查询接口
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class GoodsPlaceReadRepository {

    @SneakyThrows
    public List<GoodsPlace> queryGoodsPlaceList(GoodsPlaceRequest goodsPlaceRequest, Configuration configuration) {
        int pageSize = 100;
        int pageMax = 1000;
        int start = 0;

        List<GoodsPlace> result = new ArrayList<>();
        for(int page = 1; page < pageMax; page ++) {
            GoodsPlaceRequest goodsPlacePageRequest = new GoodsPlaceRequest();
            BeanUtils.copyProperties(goodsPlacePageRequest, goodsPlaceRequest);
            goodsPlacePageRequest.setPage(page);
            goodsPlacePageRequest.setStart(start);
            goodsPlacePageRequest.setLength(pageSize);
            GoodsPlacePageResult goodsPlacePageResult = this.pageQueryGoodsPlaceList(goodsPlacePageRequest, configuration);
            if(null == goodsPlacePageResult || !goodsPlacePageResult.isSuccessful()) {
                page = page - 1;
                continue;
            }
            List<GoodsPlace> goodsPlaceList = goodsPlacePageResult.getData();
            if(CollectionUtils.isNotEmpty(goodsPlaceList)) {
                result.addAll(goodsPlaceList);
            }
            if(CollectionUtils.isEmpty(goodsPlaceList) || goodsPlaceList.size() < pageSize) {
                break;
            }
            start += pageSize;
        }
        result = result.stream().filter(it -> StringUtils.isNotBlank(it.getAd_group_name())).collect(Collectors.toList());
        return result;
    }

    /**
     * https://ads.lingxing.com/ad_report/target/profile/index?ajax
     *
     * @param goodsPlaceRequest
     * @param configuration
     * @return
     */
    public GoodsPlacePageResult pageQueryGoodsPlaceList(GoodsPlaceRequest goodsPlaceRequest, Configuration configuration) throws Exception {
        String url = "https://ads.lingxing.com/ad_report/target/profile/index?ajax";
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

        String result = HttpUtils.doPost(url, header, goodsPlaceRequest, 10000, 10000, 3000);
        if(CommonConstant.HTTP_ERROR.equalsIgnoreCase(result)) {
            throw new Exception(CommonConstant.HTTP_ERROR);
        }
        if(CommonConstant.INVALID_TOKEN.equalsIgnoreCase(result)) {
            throw new Exception(ErrorMessage.TOKEN_INVALID);
        }
        return JSONObject.parseObject(result, GoodsPlacePageResult.class);
    }
}
