package model.configuration.spamTrafficFromAdGroup;

import lombok.Data;

/**
 * 控制广告入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupDoOperationSearch {

    private Integer daysLargerThan; // 从xx天开始

    private Integer daysSmallerThan; // 到xx天结束

    private Long clicksLargerThan; // 点击数超过

    private Long clicksSmallerThan; // 点击数小于

    private Long acosLargerThan; // acos超过

    private Long acosSmallerThan; // acos小于

    private Long ordersLargerThan; // order超过

    private Long ordersSmallerThan; // order小于
}
