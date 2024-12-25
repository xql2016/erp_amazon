package model.configuration.spamTrafficFromPlace;

import lombok.Data;

import java.util.List;

/**
 * 扩展投放入口的垃圾流量
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromPlaceConfiguration {

    private List<SpamTrafficFromPlaceDetail> autoPlaceSpamTrafficFromPlaceDetailList; // 自动投放策略

    private List<SpamTrafficFromPlaceDetail> categoryPlaceSpamTrafficFromPlaceDetailList; // 类目投放策略

    private List<SpamTrafficFromPlaceDetail> keyPlaceSpamTrafficFromPlaceDetailList; // 关键词投放策略

    private List<SpamTrafficFromPlaceDetail> asinPlaceSpamTrafficFromPlaceDetailList; // asin投放策略

}
