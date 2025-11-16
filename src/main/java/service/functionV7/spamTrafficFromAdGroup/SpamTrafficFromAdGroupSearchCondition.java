package service.functionV7.spamTrafficFromAdGroup;

import lombok.Data;

/**
 * 控制广告入口的垃圾流量
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupSearchCondition {

    private Integer daysLargerThan; // 从xx天开始

    private Integer daysSmallerThan; // 到xx天结束

    private Long ordersLargerThan; // 订单数超过

    private Long ordersSmallerThan; // 订单数小于

    private Long acosLargerThan; // acos超过

    private Long acosSmallerThan; // acos小于

    private Double spendLargerThan; // spend超过

    private Double spendSmallerThan; // spend小于
}
