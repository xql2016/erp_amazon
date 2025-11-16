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
public class SpamTrafficFromAdGroupDoOperationAction {

    private SpamTrafficFromAdGroupDoOperationSearch spamTrafficFromAdGroupDoOperationSearch; // 投放的查询条件

    private SpamTrafficFromAdGroupDoOperationDetail spamTrafficFromAdGroupDoOperationDetail; // 投放的处理

}
