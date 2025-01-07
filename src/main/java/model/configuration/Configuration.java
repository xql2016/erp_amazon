package model.configuration;

import lombok.Data;
import model.configuration.spamTrafficFromAdGroup.SpamTrafficFromAdGroupConfiguration;
import model.configuration.spamTrafficFromPlace.SpamTrafficFromPlaceConfiguration;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceConfiguration;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceDoOperation;
import model.strategy.adControl.AdControlStrategy;
import model.strategy.newAdControl.NewAdControlStrategy;
import model.strategy.oldAdStrategy.OldAdControlStrategy;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/30
 */
@Data
public class Configuration {

    private String cookie;

    private String token;

    private boolean doSimulation;

    private List<String> executeActionCodeList;

    private List<Long> hubIdList;

    private List<String> skuList;

    private List<String> adGroupNameList;

    private List<AdControlStrategy> adControlStrategyList;

    private List<NewAdControlStrategy> newAdControlStrategyList;

    private List<OldAdControlStrategy> oldAdControlStrategyList;

    private NewAdConfiguration newAdConfiguration;

    private OldAdConfiguration oldAdConfiguration;

    private SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration; // 垃圾流量控制

    private SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration; // 有效流量控制

    private SpamTrafficFromAdGroupConfiguration spamTrafficFromAdGroupConfiguration; // 垃圾流量控制-来源广告组

    private List<HubPortfolioId> hubPortfolioIdList;

}
