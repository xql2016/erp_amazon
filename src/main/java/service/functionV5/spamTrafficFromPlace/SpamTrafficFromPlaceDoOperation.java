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
public class SpamTrafficFromPlaceDoOperation {

    private List<SpamTrafficFromPlaceDoOperationSingle> doOperationSingleList; // 从doOperationSingleList找到符合条件的并进行操作


}
