package service.functionV6.spamValidFromPlace;

import lombok.Data;

/**
 * 扩展投放入口的垃圾流量-广告组配置
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamValidFromPlaceDetail {

    private SpamValidFromPlaceSearchCondition spamValidFromPlaceSearchCondition;

    private SpamValidFromPlaceDoOperation spamValidFromPlaceDoOperation;

    private Integer operateSeq; // 执行顺序,先小后大
}
