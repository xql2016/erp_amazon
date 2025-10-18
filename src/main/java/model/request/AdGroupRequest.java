package model.request;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/29
 */
@Data
public class AdGroupRequest extends PageRequest {

    private String report_date; // 日期

    private List<Long> profile_ids; // 仓库id,例子1769812645248266-德国

    private Long profile_id;

    private Long campaign_id; // 广告组id,例子325657352132867

    private List<Long> portfolio_id; // 广告组合,新品老品-208410301142303

    private List<Double> spends;

    private List<Long> orders; // 订单数

    private List<Long> acos; // acos

    /**
     * 搜索类型,campaign_name
     */
    private String search_type;
    /**
     * 搜索名称,name
     */
    private List<String> name;

    private List<String> sku;

    private List<Long> clicks;

    private List<String> countries;
}
