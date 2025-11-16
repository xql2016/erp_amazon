package service.functionV7.spamTrafficFromAdGroup;

import lombok.Data;

import java.util.List;

/**
 * 控制广告入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupDoOperation {

    // [4.进行不同条件的投放入口查询, 5.投放处理]
    private List<SpamTrafficFromAdGroupDoOperationAction> doOperationActionList;


}
