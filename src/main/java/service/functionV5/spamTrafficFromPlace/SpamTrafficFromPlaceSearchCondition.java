package service.functionV5.spamTrafficFromPlace;

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
public class SpamTrafficFromPlaceSearchCondition {

    private Integer daysLargerThan; // 从xx天开始

    private Integer daysSmallerThan; // 到xx天结束

    private Long clicksLargerThan; // 点击数超过

    private Long clicksSmallerThan; // 点击数小于

    private Long ordersLargerThan; // 订单数超过

    private Long ordersSmallerThan; // 订单数小于

    private Long acosLargerThan; // acos超过

    private Long acosSmallerThan; // acos小于
}
