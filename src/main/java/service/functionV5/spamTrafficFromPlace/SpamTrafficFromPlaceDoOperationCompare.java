package service.functionV5.spamTrafficFromPlace;

import lombok.Data;

/**
 * 扩展投放入口的垃圾流量-查询条件
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromPlaceDoOperationCompare {

    private String autoPlaceCompareValue; // 比较字段

    private String autoPlaceCompareValueType; // 比较类型

    private Double biggerThan;

    private Double smallerThan;

    private Double equalTo;
}
