package model.configuration;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationDetail {

    private List<String> executeActionCodeList;

    private List<Long> hubIdList;

    private List<String> adGroupNameList;

    private List<String> skuList;

    private List<InputConfigurationStrategy> adControlInputConfigurationStrategyList;

    private List<InputConfigurationNewAdStrategy> newAdControlInputConfigurationStrategyList;

    private List<InputConfigurationOldAdStrategy> oldAdControlInputConfigurationStrategyList;

    private List<HubPortfolioId> hubPortfolioIdList;


}
