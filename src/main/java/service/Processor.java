package service;

import com.alibaba.fastjson.JSONObject;
import model.configuration.*;
import model.configuration.spamTrafficFromPlace.SpamTrafficFromPlaceConfiguration;
import model.configuration.spamTrafficFromPlace.SpamTrafficFromPlaceDoOperationAction;
import model.configuration.spamTrafficFromPlace.SpamTrafficFromPlaceDoOperationCompare;
import model.configuration.spamTrafficFromPlace.SpamTrafficFromPlaceDoOperationSingle;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceConfiguration;
import model.configuration.spamValidFromPlace.SpamValidFromPlaceDetail;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.enums.adGroup.AdGroupCompareValue;
import model.enums.adGroup.AdGroupOperateType;
import model.enums.adPlacement.CompareValue;
import model.enums.CompareValueType;
import model.enums.adPlacement.OperateType;
import model.strategy.adControl.AdControlStrategy;
import model.strategy.adControl.StrategyOperation;
import model.strategy.adControl.StrategySearchCondition;
import model.strategy.newAdControl.NewAdControlStrategy;
import model.strategy.newAdControl.NewAdStrategyOperation;
import model.strategy.newAdControl.NewAdStrategySearchCondition;
import model.strategy.oldAdStrategy.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import service.adControl.*;
import service.spamControl.SpamTrafficFromPlaceAction;
import service.spamControl.SpamValidFromPlaceAction;
import tools.FileUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/4
 */
//@Slf4j
public class Processor {

    public void doHandle() {
        try {
            // 加载配置文件
            Configuration configuration = loadLocalConfiguration();
            if(null == configuration) {
                System.out.println("not find configuration");
                return;
            }
            // 选择处理器进行处理
            List<AbstractAction> abstractionActionList = loadAllActionList();
            if(CollectionUtils.isEmpty(abstractionActionList)) {
                System.out.println("loadAllActionList empty");
                return;
            }
            List<AbstractAction> executeActionList = abstractionActionList.stream().filter(it -> configuration.getExecuteActionCodeList().contains(it.getCode())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(executeActionList)) {
                System.out.println(String.format("not find executeAction, executeCodeList=%s", configuration.getExecuteActionCodeList()));
                return;
            }
            for(AbstractAction abstractAction : executeActionList) {
                abstractAction.execute(configuration);
            }
        } catch (Exception e) {
            System.out.println(String.format("system error, errorMsg=%s", e.getMessage()));
        }

    }

    private Configuration loadLocalConfiguration() {
        InputConfigurationToken inputConfigurationToken = loadInputConfigurationToken();
        InputConfigurationDetail inputConfigurationDetail = loadInputConfigurationDetail();
        SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration = loadSpamTrafficFromPlaceConfiguration();
        JSONObject spamTrafficCustomConfiguration = loadSpamTrafficCustomConfiguration();
        SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration = loadSpamValidConfiguration();
        JSONObject spamValidCustomConfiguration = loadSpamValidCustomConfiguration();
        return assembleConfiguration(inputConfigurationToken, inputConfigurationDetail, spamTrafficFromPlaceConfiguration, spamTrafficCustomConfiguration, spamValidFromPlaceConfiguration, spamValidCustomConfiguration);
    }

    private InputConfigurationToken loadInputConfigurationToken() {
        String str = FileUtils.loadFile(FilePath.inputConfigurationToken);
        return JSONObject.parseObject(str, InputConfigurationToken.class);
    }

    private InputConfigurationDetail loadInputConfigurationDetail() {
        String str = FileUtils.loadFile(FilePath.inputConfigurationDetail);
        return JSONObject.parseObject(str, InputConfigurationDetail.class);
    }

    private JSONObject loadSpamTrafficCustomConfiguration() {
        String str = FileUtils.loadFile(FilePath.spamTrafficFromPlaceConfigurationCustom);
        return JSONObject.parseObject(str);
    }
    private JSONObject loadSpamValidCustomConfiguration() {
        String str = FileUtils.loadFile(FilePath.spamValidFromPlaceConfigurationCustom);
        return JSONObject.parseObject(str);
    }

    private SpamValidFromPlaceConfiguration loadSpamValidConfiguration() {
        String str = FileUtils.loadFile(FilePath.spamValidFromPlaceConfiguration);
        return JSONObject.parseObject(str, SpamValidFromPlaceConfiguration.class);
    }


    private SpamTrafficFromPlaceConfiguration loadSpamTrafficFromPlaceConfiguration() {
        String str = FileUtils.loadFile(FilePath.spamTrafficFromPlaceConfiguration);
        return JSONObject.parseObject(str, SpamTrafficFromPlaceConfiguration.class);
    }

    private Configuration assembleConfiguration(InputConfigurationToken inputConfigurationToken, InputConfigurationDetail inputConfigurationDetail, SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration, JSONObject spamTrafficCustomConfiguration, SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration, JSONObject spamValidCustomConfiguration) {
        if(null == inputConfigurationToken || null == inputConfigurationDetail) {
            return null;
        }
        if(StringUtils.isBlank(inputConfigurationToken.getCookie()) || StringUtils.isBlank(inputConfigurationToken.getToken())) {
            return null;
        }
        Configuration configuration = new Configuration();
        configuration.setToken(inputConfigurationToken.getToken());
        configuration.setCookie(inputConfigurationToken.getCookie());
        configuration.setDoSimulation(inputConfigurationToken.isDoSimulation());
        if(CollectionUtils.isEmpty(inputConfigurationDetail.getExecuteActionCodeList())) {
            return null;
        }
        List<AdControlStrategy> adControlStrategyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(inputConfigurationDetail.getAdControlInputConfigurationStrategyList())) {
            adControlStrategyList = inputConfigurationDetail.getAdControlInputConfigurationStrategyList().stream()
                    .map(this::convertToAdControlStrategy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        List<NewAdControlStrategy> newAdControlStrategyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(inputConfigurationDetail.getNewAdControlInputConfigurationStrategyList())) {
            newAdControlStrategyList = inputConfigurationDetail.getNewAdControlInputConfigurationStrategyList().stream()
                    .map(this::convertToNewAdControlStrategy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        List<OldAdControlStrategy> oldAdControlStrategyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(inputConfigurationDetail.getOldAdControlInputConfigurationStrategyList())) {
            oldAdControlStrategyList = inputConfigurationDetail.getOldAdControlInputConfigurationStrategyList().stream()
                    .map(this::convertToOldAdControlStrategy)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(adControlStrategyList) && CollectionUtils.isEmpty(newAdControlStrategyList) && CollectionUtils.isEmpty(oldAdControlStrategyList)) {
            return null;
        }

        SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfigurationAssemble = assembleSpamTrafficFromPlaceConfiguration(spamTrafficFromPlaceConfiguration, spamTrafficCustomConfiguration);
        SpamValidFromPlaceConfiguration spamValidFromPlaceConfigurationAssemble = assembleSpamValidFromPlaceConfiguration(spamValidFromPlaceConfiguration, spamValidCustomConfiguration);

        configuration.setSkuList(inputConfigurationDetail.getSkuList());
        configuration.setHubIdList(inputConfigurationDetail.getHubIdList());
        configuration.setAdGroupNameList(inputConfigurationDetail.getAdGroupNameList());
        configuration.setExecuteActionCodeList(inputConfigurationDetail.getExecuteActionCodeList());
        configuration.setAdControlStrategyList(adControlStrategyList);
        configuration.setNewAdControlStrategyList(newAdControlStrategyList);
        configuration.setOldAdControlStrategyList(oldAdControlStrategyList);
        configuration.setSpamTrafficFromPlaceConfiguration(spamTrafficFromPlaceConfigurationAssemble);
        configuration.setSpamValidFromPlaceConfiguration(spamValidFromPlaceConfigurationAssemble);
        configuration.setHubPortfolioIdList(inputConfigurationDetail.getHubPortfolioIdList());
        return configuration;
    }

    private SpamValidFromPlaceConfiguration assembleSpamValidFromPlaceConfiguration(SpamValidFromPlaceConfiguration spamValidFromPlaceConfiguration, JSONObject spamValidFromPlaceCustomConfiguration) {
        Integer nearlyDays = spamValidFromPlaceCustomConfiguration.getInteger("投放入口，放大长期有效流量的天数");
        Long autoPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("自动投放入口打开ACOS临界值");
        Long autoPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("自动投放竞价不低于CPC-0.01的ACOS临界值");
        Double autoPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("自动投放ACOS＜20% 竞价增加值");
        Long categoryPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("类目投放ACOS临界值");
        Long categoryPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("类目投放竞价不低于CPC-0.01的ACOS临界值");
        Double categoryPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("类目投放ACOS＜20% 竞价增加值");
        Long keyPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("关键词投放ACOS临界值");
        Long keyPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("关键词投放竞价不低于CPC-0.01的ACOS临界值");
        Double keyPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("关键词投放ACOS＜20% 竞价增加值");
        Long asinPlaceAcosOpen = spamValidFromPlaceCustomConfiguration.getLong("ASIN投放ACOS临界值");
        Long asinPlaceAcosSub = spamValidFromPlaceCustomConfiguration.getLong("ASIN投放竞价不低于CPC-0.01的ACOS临界值");
        Double asinPlaceAcosAdd = spamValidFromPlaceCustomConfiguration.getDouble("ASIN投放ACOS＜20% 竞价增加值");
        if(null != nearlyDays) {
            spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamValidFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamValidFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getAutoPlaceSpamValidFromPlaceDetailList()) {
            if(null != autoPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoPlaceAcosOpen);
            }
            if(null != autoPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(autoPlaceAcosSub);
            }
            if(null != autoPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(autoPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getAsinPlaceSpamValidFromPlaceDetailList()) {
            if(null != asinPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinPlaceAcosOpen);
            }
            if(null != asinPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(asinPlaceAcosSub);
            }
            if(null != asinPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(asinPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getCategoryPlaceSpamValidFromPlaceDetailList()) {
            if(null != categoryPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categoryPlaceAcosOpen);
            }
            if(null != categoryPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(categoryPlaceAcosSub);
            }
            if(null != categoryPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(categoryPlaceAcosAdd);
            }
        }
        for(SpamValidFromPlaceDetail spamValidFromPlaceDetail : spamValidFromPlaceConfiguration.getKeyPlaceSpamValidFromPlaceDetailList()) {
            if(null != keyPlaceAcosOpen &&"OPEN".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keyPlaceAcosOpen);
            }
            if(null != keyPlaceAcosSub &&"MORE_THAN_CPC_SUBTRACT_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceSearchCondition().setAcosSmallerThan(keyPlaceAcosSub);
            }
            if(null != keyPlaceAcosAdd &&"MORE_THAN_CPC_ADD_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().getSpamValidOperateType())) {
                spamValidFromPlaceDetail.getSpamValidFromPlaceDoOperation().getSpamValidFromPlaceDoOperationAction().setCpcAddValue(keyPlaceAcosAdd);
            }
        }
        return spamValidFromPlaceConfiguration;
    }

    private SpamTrafficFromPlaceConfiguration assembleSpamTrafficFromPlaceConfiguration(SpamTrafficFromPlaceConfiguration spamTrafficFromPlaceConfiguration, JSONObject spamTrafficCustomConfiguration) {
        Integer nearlyDays = spamTrafficCustomConfiguration.getInteger("投放入口，控制长期垃圾流量的天数");
        Long autoPlaceClicks = spamTrafficCustomConfiguration.getLong("自动点击临界数");
        Long categoryPlaceClicks = spamTrafficCustomConfiguration.getLong("类目点击临界数");
        Long keyPlaceClicks = spamTrafficCustomConfiguration.getLong("关键词点击临界数");
        Long asinPlaceClicks = spamTrafficCustomConfiguration.getLong("ASIN点击临界数");
        if(null != nearlyDays) {
            spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
            spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysLargerThan(nearlyDays - 1);
                        it.getSpamTrafficFromPlaceSearchCondition().setDaysSmallerThan(0);
                    }
            );
        }
        if(null != autoPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getAutoPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setClicksLargerThan(autoPlaceClicks);
                    }
            );
        }
        if(null != categoryPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getCategoryPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        it.getSpamTrafficFromPlaceSearchCondition().setClicksLargerThan(autoPlaceClicks);
                    }
            );
        }
        if(null != keyPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getKeyPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        for(SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle : it.getSpamTrafficFromPlaceDoOperation().getDoOperationSingleList()) {
                            for(SpamTrafficFromPlaceDoOperationAction spamTrafficFromPlaceDoOperationAction : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
                                if("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())) {
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setSmallerThan((double) keyPlaceClicks);
                                        }
                                    }
                                } else if("CLOSE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())){
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setBiggerThan((double) keyPlaceClicks);
                                        }
                                    }
                                }
                            }
                        }
                    }
            );
        }
        if(null != asinPlaceClicks) {
            spamTrafficFromPlaceConfiguration.getAsinPlaceSpamTrafficFromPlaceDetailList().forEach(
                    it -> {
                        for(SpamTrafficFromPlaceDoOperationSingle spamTrafficFromPlaceDoOperationSingle : it.getSpamTrafficFromPlaceDoOperation().getDoOperationSingleList()) {
                            for(SpamTrafficFromPlaceDoOperationAction spamTrafficFromPlaceDoOperationAction : spamTrafficFromPlaceDoOperationSingle.getActionList()) {
                                if("CPC_SUBTRACT_CLICK_MULTIPLY_VALUE_AND_LAGER_THAN_VALUE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())) {
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setSmallerThan((double) asinPlaceClicks);
                                        }
                                    }
                                } else if("CLOSE".equalsIgnoreCase(spamTrafficFromPlaceDoOperationAction.getAutoPlaceOperateType())){
                                    for(SpamTrafficFromPlaceDoOperationCompare spamTrafficFromPlaceDoOperationCompare : spamTrafficFromPlaceDoOperationSingle.getCompareList()) {
                                        if("CLICK".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValue()) && "BIGGER_OR_EQUAL_TO_SMALLER".equalsIgnoreCase(spamTrafficFromPlaceDoOperationCompare.getAutoPlaceCompareValueType())) {
                                            spamTrafficFromPlaceDoOperationCompare.setBiggerThan((double) asinPlaceClicks);
                                        }
                                    }
                                }
                            }
                        }
                    }
            );
        }
        return spamTrafficFromPlaceConfiguration;
    }

    private OldAdControlStrategy convertToOldAdControlStrategy(InputConfigurationOldAdStrategy inputConfigurationOldAdStrategy) {
        AdGroupType adGroupType = AdGroupType.getByValue(inputConfigurationOldAdStrategy.getAdGroupType());
        AdGroupCompareValue adGroupCompareValue = AdGroupCompareValue.getByValue(inputConfigurationOldAdStrategy.getAdGroupCompareValue());
        CompareValueType adGroupCompareValueType = CompareValueType.getByValue(inputConfigurationOldAdStrategy.getAdGroupCompareValueType());

        if(null == adGroupType || null == adGroupCompareValue || null == adGroupCompareValueType) {
            return null;
        }

        List<OldAdPlacementStrategy> oldAdPlacementStrategyList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(inputConfigurationOldAdStrategy.getOldAdPlacementStrategyList())) {
            oldAdPlacementStrategyList = inputConfigurationOldAdStrategy.getOldAdPlacementStrategyList().stream()
                    .map(it -> convertToOldAdPlacementStrategy(it, adGroupType))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(oldAdPlacementStrategyList)) {
            return null;
        }

        OldAdStrategySearchCondition oldAdStrategySearchCondition = new OldAdStrategySearchCondition();
        oldAdStrategySearchCondition.setAdGroupType(adGroupType);
        oldAdStrategySearchCondition.setAdGroupCompareValue(adGroupCompareValue);
        oldAdStrategySearchCondition.setAdGroupCompareValueType(adGroupCompareValueType);
        oldAdStrategySearchCondition.setAdGroupEqualsValue(inputConfigurationOldAdStrategy.getAdGroupEqualsValue());
        oldAdStrategySearchCondition.setAdGroupBiggerThanValue(inputConfigurationOldAdStrategy.getAdGroupBiggerThanValue());
        oldAdStrategySearchCondition.setAdGroupSmallerThanOrEqualToValue(inputConfigurationOldAdStrategy.getAdGroupSmallerThanOrEqualToValue());
        oldAdStrategySearchCondition.setAdPlacementSearchFromDays(inputConfigurationOldAdStrategy.getAdPlacementSearchFromDays());
        oldAdStrategySearchCondition.setAdPlacementSearchToDays(inputConfigurationOldAdStrategy.getAdPlacementSearchToDays());
        oldAdStrategySearchCondition.setOldAdPlacementStrategyList(oldAdPlacementStrategyList);

        OldAdControlStrategy oldAdControlStrategy = new OldAdControlStrategy();
        oldAdControlStrategy.setOldAdStrategySearchCondition(oldAdStrategySearchCondition);
        return oldAdControlStrategy;
    }

    private OldAdPlacementStrategy convertToOldAdPlacementStrategy(InputConfigurationOldAdPlacementStrategy inputConfigurationOldAdPlacementStrategy, AdGroupType adGroupType) {
        CompareValue firstCompareValue = CompareValue.getByValue(inputConfigurationOldAdPlacementStrategy.getFirstCompareValue());
        CompareValueType firstCompareValueType = CompareValueType.getByValue(inputConfigurationOldAdPlacementStrategy.getFirstCompareValueType());
        CompareValue secondCompareValue = CompareValue.getByValue(inputConfigurationOldAdPlacementStrategy.getSecondCompareValue());
        CompareValueType secondCompareValueType = CompareValueType.getByValue(inputConfigurationOldAdPlacementStrategy.getSecondCompareValueType());
        OperateType operateType = OperateType.getByValue(inputConfigurationOldAdPlacementStrategy.getAdOperateType());

        if(null == firstCompareValue || null == firstCompareValueType || null == secondCompareValue || null == secondCompareValueType || null == operateType) {
            return null;
        }

        OldAdPlacementSearchCondition oldAdPlacementSearchCondition = new OldAdPlacementSearchCondition();
        oldAdPlacementSearchCondition.setAdGroupType(adGroupType);
        oldAdPlacementSearchCondition.setHasOrder(inputConfigurationOldAdPlacementStrategy.isHasOrder());
        oldAdPlacementSearchCondition.setNeedCompare(inputConfigurationOldAdPlacementStrategy.isNeedCompare());
        oldAdPlacementSearchCondition.setNeedCompareSecond(inputConfigurationOldAdPlacementStrategy.isNeedCompareSecond());
        oldAdPlacementSearchCondition.setFirstCompareValue(firstCompareValue);
        oldAdPlacementSearchCondition.setFirstCompareValueType(firstCompareValueType);
        oldAdPlacementSearchCondition.setFirstEqualsValue(inputConfigurationOldAdPlacementStrategy.getFirstEqualsValue());
        oldAdPlacementSearchCondition.setFirstBiggerThanValue(inputConfigurationOldAdPlacementStrategy.getFirstBiggerThanValue());
        oldAdPlacementSearchCondition.setFirstSmallerThanOrEqualToValue(inputConfigurationOldAdPlacementStrategy.getFirstSmallerThanOrEqualToValue());
        oldAdPlacementSearchCondition.setSecondCompareValue(secondCompareValue);
        oldAdPlacementSearchCondition.setSecondCompareValueType(secondCompareValueType);
        oldAdPlacementSearchCondition.setSecondEqualsValue(inputConfigurationOldAdPlacementStrategy.getSecondEqualsValue());
        oldAdPlacementSearchCondition.setSecondBiggerThanValue(inputConfigurationOldAdPlacementStrategy.getSecondBiggerThanValue());
        oldAdPlacementSearchCondition.setSecondSmallerThanOrEqualToValue(inputConfigurationOldAdPlacementStrategy.getSecondSmallerThanOrEqualToValue());

        OldAdPlacementStrategyOperation oldAdPlacementStrategyOperation = new OldAdPlacementStrategyOperation();
        oldAdPlacementStrategyOperation.setAdOperateType(operateType);
        oldAdPlacementStrategyOperation.setDoOperate(inputConfigurationOldAdPlacementStrategy.getDoOperate());
        oldAdPlacementStrategyOperation.setChangeBidValue(inputConfigurationOldAdPlacementStrategy.getChangeBidValue());
        oldAdPlacementStrategyOperation.setDoOpen(inputConfigurationOldAdPlacementStrategy.getDoOpen());
        oldAdPlacementStrategyOperation.setMaxValue(inputConfigurationOldAdPlacementStrategy.getMaxValue());

        OldAdPlacementStrategy oldAdPlacementStrategy = new OldAdPlacementStrategy();
        oldAdPlacementStrategy.setOldAdPlacementSearchCondition(oldAdPlacementSearchCondition);
        oldAdPlacementStrategy.setOldAdPlacementStrategyOperation(oldAdPlacementStrategyOperation);
        return oldAdPlacementStrategy;
    }

    private NewAdControlStrategy convertToNewAdControlStrategy(InputConfigurationNewAdStrategy inputConfigurationNewAdStrategy) {
        AdGroupType adGroupType = AdGroupType.getByValue(inputConfigurationNewAdStrategy.getAdGroupType());
        AdGroupCompareValue firstAdGroupCompareValue = AdGroupCompareValue.getByValue(inputConfigurationNewAdStrategy.getFirstCompareValue());
        CompareValueType firstCompareValueType = CompareValueType.getByValue(inputConfigurationNewAdStrategy.getFirstCompareValueType());
        AdGroupCompareValue secondAdGroupCompareValue = AdGroupCompareValue.getByValue(inputConfigurationNewAdStrategy.getSecondCompareValue());
        CompareValueType secondCompareValueType = CompareValueType.getByValue(inputConfigurationNewAdStrategy.getSecondCompareValueType());
        AdGroupOperateType adGroupOperateType = AdGroupOperateType.getByValue(inputConfigurationNewAdStrategy.getOperateType());

        if(null == adGroupType || null == firstAdGroupCompareValue || null == firstCompareValueType) {
            return null;
        }

        NewAdStrategySearchCondition newAdStrategySearchCondition = new NewAdStrategySearchCondition();
        newAdStrategySearchCondition.setAdGroupType(adGroupType);

        newAdStrategySearchCondition.setFirstCompareValue(firstAdGroupCompareValue);
        newAdStrategySearchCondition.setFirstCompareValueType(firstCompareValueType);
        newAdStrategySearchCondition.setFirstEqualsValue(inputConfigurationNewAdStrategy.getFirstEqualsValue());
        newAdStrategySearchCondition.setFirstBiggerThanValue(inputConfigurationNewAdStrategy.getFirstBiggerThanValue());
        newAdStrategySearchCondition.setFirstSmallerThanOrEqualToValue(inputConfigurationNewAdStrategy.getFirstSmallerThanOrEqualToValue());

        newAdStrategySearchCondition.setSecondFromSearchDay(inputConfigurationNewAdStrategy.getSecondFromSearchDay());
        newAdStrategySearchCondition.setSecondToSearchDay(inputConfigurationNewAdStrategy.getSecondToSearchDay());
        newAdStrategySearchCondition.setSecondCompareValue(secondAdGroupCompareValue);
        newAdStrategySearchCondition.setSecondCompareValueType(secondCompareValueType);
        newAdStrategySearchCondition.setSecondEqualsValue(inputConfigurationNewAdStrategy.getSecondEqualsValue());
        newAdStrategySearchCondition.setSecondBiggerThanValue(inputConfigurationNewAdStrategy.getSecondBiggerThanValue());
        newAdStrategySearchCondition.setSecondSmallerThanOrEqualToValue(inputConfigurationNewAdStrategy.getSecondSmallerThanOrEqualToValue());

        NewAdStrategyOperation newAdStrategyOperation = new NewAdStrategyOperation();
        newAdStrategyOperation.setAdGroupOperateType(adGroupOperateType);
        newAdStrategyOperation.setDoOperate(inputConfigurationNewAdStrategy.isDoOperate());
        newAdStrategyOperation.setChangeAdGroupValue(inputConfigurationNewAdStrategy.getChangeAdGroupValue());

        NewAdControlStrategy newAdControlStrategy = new NewAdControlStrategy();
        newAdControlStrategy.setNewAdStrategyOperation(newAdStrategyOperation);
        newAdControlStrategy.setNewAdStrategySearchCondition(newAdStrategySearchCondition);
        return newAdControlStrategy;
    }

    private AdControlStrategy convertToAdControlStrategy(InputConfigurationStrategy inputConfigurationStrategy) {
        AdGroupType adGroupType = AdGroupType.getByValue(inputConfigurationStrategy.getAdGroupType());
        CompareValue compareValue = CompareValue.getByValue(inputConfigurationStrategy.getCompareValue());
        CompareValueType compareValueType = CompareValueType.getByValue(inputConfigurationStrategy.getCompareValueType());
        OperateType operateType = OperateType.getByValue(inputConfigurationStrategy.getOperateType());
        if(null == adGroupType || null == compareValue || null == compareValueType || null == operateType) {
            return null;
        }

        StrategySearchCondition strategySearchCondition = new StrategySearchCondition();
        strategySearchCondition.setAdGroupType(adGroupType);
        strategySearchCondition.setCompareValue(compareValue);
        strategySearchCondition.setCompareValueType(compareValueType);
        strategySearchCondition.setHasOrder(inputConfigurationStrategy.isHasOrder());
        strategySearchCondition.setEqualsValue(inputConfigurationStrategy.getEqualsValue());
        strategySearchCondition.setBiggerThanValue(inputConfigurationStrategy.getBiggerThanValue());
        strategySearchCondition.setSmallerThanOrEqualToValue(inputConfigurationStrategy.getSmallerThanOrEqualToValue());

        StrategyOperation strategyOperation = new StrategyOperation();
        strategyOperation.setOperateType(operateType);
        strategyOperation.setDoOperate(inputConfigurationStrategy.isDoOperate());
        strategyOperation.setChangeBidValue(inputConfigurationStrategy.getChangeBidValue());

        AdControlStrategy adControlStrategy = new AdControlStrategy();
        adControlStrategy.setStrategyOperation(strategyOperation);
        adControlStrategy.setStrategySearchCondition(strategySearchCondition);
        return adControlStrategy;
    }

    private List<AbstractAction> loadAllActionList() {
        List<AbstractAction> abstractionActionList = new ArrayList<>();
        abstractionActionList.add(new AdControlAdControlAction());
        abstractionActionList.add(new NewAdControlAdControlAction());
        abstractionActionList.add(new ExcelExportAdControlAction());
        abstractionActionList.add(new OldAdControlAdControlAction());
        abstractionActionList.add(new SpamTrafficFromPlaceAction());
        abstractionActionList.add(new SpamValidFromPlaceAction());
        return abstractionActionList;
    }
}
