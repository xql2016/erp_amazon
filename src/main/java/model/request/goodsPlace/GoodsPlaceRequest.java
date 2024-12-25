package model.request.goodsPlace;

import lombok.Data;
import model.request.PageRequest;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class GoodsPlaceRequest extends PageRequest {

    private String report_date; // 查询日期

    private List<Long> portfolio_id; // 新品,老品... 新品对应=9484519370702,老品对应=208410301142303

    private List<String> expression_types; // asinSameAs=商品,asinCategorySameAs=类目

    private List<Long> clicks; // 点击数

    private List<Long> orders; // 广告数

    private List<Long> acos; // acos

    private List<Double> cpc; // cpc

    private long profile_id; // 国家仓库信息
}
