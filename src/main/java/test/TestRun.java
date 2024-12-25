package test;

import com.alibaba.fastjson.JSONObject;
import model.configuration.Configuration;
import model.request.autoPlace.AutoPlaceRequest;
import model.request.goodsPlace.GoodsPlaceRequest;
import model.request.keyPlace.KeyPlaceRequest;
import model.response.autoPlace.AutoPlacePageResult;
import model.response.goodsPlace.GoodsPlacePageResult;
import model.response.keyPlace.KeyPlacePageResult;
import repository.read.AutoPlaceReadRepository;
import repository.read.GoodsPlaceReadRepository;
import repository.read.KeyPlaceReadRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public class TestRun {

    /**
    public static void main(String[] args) throws Exception {
        List<Long> clicks = new ArrayList<>();
        clicks.add(10L);
        clicks.add(null);
        List<Long> orders = new ArrayList<>();
        orders.add(null);
        orders.add(0L);
        List<Long> portfolio_ids = new ArrayList<>();
        portfolio_ids.add(9484519370702L);
        portfolio_ids.add(208410301142303L);

        AutoPlaceRequest autoPlaceRequest = new AutoPlaceRequest();
        autoPlaceRequest.setClicks(clicks);
        autoPlaceRequest.setOrders(orders);
        autoPlaceRequest.setPortfolio_id(portfolio_ids);
        autoPlaceRequest.setProfile_id(1769812645248266L);
        autoPlaceRequest.setReport_date("2024-11-22 - 2024-12-21");

        autoPlaceRequest.setPage(1);
        autoPlaceRequest.setStart(0);
        autoPlaceRequest.setLength(100);

        Configuration configuration = new Configuration();
        configuration.setToken("6dWza2eWQXwSuICQEFRixlSeoG07XPNJNPFny1iQ");
        configuration.setCookie("amzbi=tBhYWeXwsfq7bef1TfKcZDCIc8v8DaKf8LDlDaYl");

        AutoPlacePageResult b = new AutoPlaceReadRepository().pageQueryAutoPlaceList(autoPlaceRequest, configuration);
        System.out.println(JSONObject.toJSONString(b));

        List<String> expression_types = new ArrayList<>();
        expression_types.add("asinSameAs");
        GoodsPlaceRequest goodsPlaceRequest = new GoodsPlaceRequest();
        goodsPlaceRequest.setClicks(clicks);
        goodsPlaceRequest.setOrders(orders);
        goodsPlaceRequest.setPortfolio_id(portfolio_ids);
        goodsPlaceRequest.setProfile_id(1769812645248266L);
        goodsPlaceRequest.setReport_date("2024-11-22 - 2024-12-21");
        goodsPlaceRequest.setExpression_types(expression_types);

        goodsPlaceRequest.setPage(1);
        goodsPlaceRequest.setStart(0);
        goodsPlaceRequest.setLength(100);

        GoodsPlacePageResult bb = new GoodsPlaceReadRepository().pageQueryGoodsPlaceList(goodsPlaceRequest, configuration);
        System.out.println(JSONObject.toJSONString(bb));

        KeyPlaceRequest keyPlaceRequest = new KeyPlaceRequest();
        keyPlaceRequest.setClicks(clicks);
        keyPlaceRequest.setOrders(orders);
        keyPlaceRequest.setPortfolio_id(portfolio_ids);
        keyPlaceRequest.setProfile_id(1769812645248266L);
        keyPlaceRequest.setReport_date("2024-11-22 - 2024-12-21");

        keyPlaceRequest.setPage(1);
        keyPlaceRequest.setStart(0);
        keyPlaceRequest.setLength(100);

        KeyPlacePageResult bbb = new KeyPlaceReadRepository().pageQueryKeyPlaceList(keyPlaceRequest, configuration);
        System.out.println(JSONObject.toJSONString(bbb));
    }
    **/
}
