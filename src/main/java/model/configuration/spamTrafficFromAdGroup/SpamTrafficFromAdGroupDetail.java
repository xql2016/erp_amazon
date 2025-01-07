package model.configuration.spamTrafficFromAdGroup;

import lombok.Data;

/**
 * 控制广告入口的垃圾流量-广告组配置
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupDetail {

    // 1.查询广告组条件, 2.根据广告组查询出来数据,对广告组遍历处理, [3.不同的广告组类型[4.进行不同条件的投放入口查询, 5.投放处理]]
    private SpamTrafficFromAdGroupSearchCondition spamTrafficFromAdGroupSearchCondition; // 广告组查询条件

    private SpamTrafficFromAdGroupDoOperation autoSpamTrafficFromAdGroupDoOperation; // 自动广告组的处理

    private SpamTrafficFromAdGroupDoOperation categorySpamTrafficFromAdGroupDoOperation; // category广告组的处理

    private SpamTrafficFromAdGroupDoOperation keySpamTrafficFromAdGroupDoOperation; // key广告组的处理

    private SpamTrafficFromAdGroupDoOperation asinSpamTrafficFromAdGroupDoOperation; // asin广告组的处理
}
