package model.configuration.spamTrafficFromPlace;

import lombok.Data;

/**
 * 扩展投放入口的垃圾流量-广告组配置
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromPlaceDetail {

    private SpamTrafficFromPlaceSearchCondition spamTrafficFromPlaceSearchCondition;

    private SpamTrafficFromPlaceDoOperation spamTrafficFromPlaceDoOperation;
}
