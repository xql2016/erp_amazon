package model.configuration.spamValidFromPlace;

import lombok.Data;

import java.util.List;

/**
 * 扩展投放入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamValidFromPlaceSearchCondition {

    private Integer daysLargerThan; // 从xx天开始

    private Integer daysSmallerThan; // 到xx天结束

    private List<Long> portfolioIdList; // 新品,老品... 新品对应=9484519370702,老品对应=208410301142303

    private Long ordersLargerThan; // 订单数超过

    private Long ordersSmallerThan; // 订单数小于

    private Long acosLargerThan; // acos超过

    private Long acosSmallerThan; // acos小于

    private Double cpcLargerThan; // cpc超过

    private Double cpcSmallerThan; // cpc小于
}
