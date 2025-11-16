package service.functionV6.spamValidFromPlace;

import lombok.Data;

import java.util.List;

/**
 * 扩展投放入口的有效流量
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamValidFromPlaceConfiguration {

    private List<SpamValidFromPlaceDetail> autoPlaceSpamValidFromPlaceDetailList; // 自动投放策略

    private List<SpamValidFromPlaceDetail> categoryPlaceSpamValidFromPlaceDetailList; // 类目投放策略

    private List<SpamValidFromPlaceDetail> keyPlaceSpamValidFromPlaceDetailList; // 关键词投放策略

    private List<SpamValidFromPlaceDetail> asinPlaceSpamValidFromPlaceDetailList; // asin投放策略

}
