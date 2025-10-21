package service.functionV2;

import model.configuration.Configuration;
import model.enums.AdGroupType;
import model.request.AdPlacementRequest;
import model.response.AdGroup;
import model.response.AdPlacement;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdReadRepository;
import tools.AdPlacementUtils;
import tools.DateUtils;
import tools.HubUtils;

import java.util.List;


public class AutoAdGroupDetailAction{

    // 1. 如果广告组近x天的广告订单为0
    //    1.1 如果总点击数<x不做处理
    //    1.2 如果总点击数>=x则看投放入口,改投放入口bid
    // 2. 如果广告组近x天的广告订单不为0
    //    2.1 如果总acos<=x不做处理
    //    2.2 如果总acos>=x则看投放入口,改投放入口bid
    public void executeAdGroupDetail(AdGroup newAdGroup, AdGroupType adGroupType, Configuration configuration, int realDays) {
        String key1 = "V2自动广告组层面长期ACOS控制基准下限";
        int value1 = configuration.getFeatures().getIntValue(key1);
        String key2 = "V2自动广告组层面长期ACOS控制基准值";
        int value2 = configuration.getFeatures().getIntValue(key2);
        if((null == newAdGroup.getOrders() || 0 == newAdGroup.getOrders()) && newAdGroup.getClicks() < 10) {
            // 不处理,打日志
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组广告订单数为0且点击数小于10,无需变更, country=%s, adGroupName=%s, acos =%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),newAdGroup.getStore_country(), newAdGroup.getName(), newAdGroup.getAcos()));
            return;
        }
        if((null != newAdGroup.getOrders() && 0 != newAdGroup.getOrders()) && Double.parseDouble(newAdGroup.getAcos()) <= value1) {
            // 不处理,打日志
            System.out.println(String.format("    店铺id=%s,名称=%s,当前广告组广告订单数非0且acos小于等于%s,无需变更, country=%s, adGroupName=%s, acos =%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),value1, newAdGroup.getStore_country(), newAdGroup.getName(), newAdGroup.getAcos()));
            return;
        }

        // 查询出投放列表
        AdPlacementRequest adPlacementRequest = new AdPlacementRequest();
        adPlacementRequest.setProfile_id(Long.parseLong(newAdGroup.getProfile_id()));
        adPlacementRequest.setReport_date(DateUtils.buildReportDateString(realDays));
        adPlacementRequest.setCampaign_id(Long.parseLong(newAdGroup.getCampaign_id()));
        List<AdPlacement> adPlacementList = new AdReadRepository().queryAdPlacementList(adPlacementRequest, configuration, adGroupType);
        for(AdPlacement adPlacement : adPlacementList) {
            // 第一行统计不做处理
            if(StringUtils.isBlank(adPlacement.getAd_group_name())) {
                continue;
            }
            if(!"enabled".equalsIgnoreCase(adPlacement.getState())) {
                // 不处理,打日志
                System.out.println(String.format("   店铺id=%s,名称=%s,广告组投放入口未启用,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
                continue;
            }
            if(null == newAdGroup.getOrders() || 0 == newAdGroup.getOrders()) {
                // 无广告订单处理
                if (newAdGroup.getClicks() >= 10 && newAdGroup.getClicks() < 30) {
                    handle1(newAdGroup, adGroupType, adPlacement, configuration);
                } else if(newAdGroup.getClicks() >= 30) {
                    handle2(newAdGroup, adGroupType, adPlacement, configuration);
                }
            } else {
                // acos是写成30而非30%
                // 有广告订单处理
                if (Double.parseDouble(newAdGroup.getAcos()) > value1 && Double.parseDouble(newAdGroup.getAcos()) <= 60) {
                    handle3(newAdGroup, adGroupType, adPlacement, configuration, value1, value2);
                } else if(Double.parseDouble(newAdGroup.getAcos()) > 60){
                    handle4(newAdGroup, adGroupType, adPlacement, configuration, value1, value2);
                }
            }
        }
    }

    // 广告组无广告订单点击数10-30的处理
    // 1.投放入口点击>=20,bid改为0.02
    // 2.投放入口点击>=10&&<20,bid和CPC+0.08-0.01*点击数对比,如果小于等于不改,否则修改
    // 3.投放入口点击>=4&&<10,bid和CPC-0.01对比,如果小于等于不改,否则修改
    // 4.投放入口点击数<4，不做处理。
    private void handle1(AdGroup newAdGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if (adPlacement.getClicks() >= 20) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, 0.02, configuration);
        } else if (adPlacement.getClicks() >= 10) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) + 0.08 - 0.01 * adPlacement.getClicks(), configuration);
        } else if (adPlacement.getClicks() >= 4) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) - 0.01, configuration);
        } else {
            // 不处理,打日志
            System.out.println(String.format("   店铺id=%s,名称=%s,广告组无广告订单且点击数处于10-30且投放入口点击数小于4,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
        }
    }

    // 广告组无广告订单点击数30以上的处理
    // 1.投放入口点击>=20,bid改为0.02
    // 2.投放入口点击>=10&&<20,bid和CPC+0.07-0.01*点击数对比,如果小于等于不改,否则修改
    // 3.投放入口点击>=1&&<10,bid和CPC-0.02对比,如果小于等于不改,否则修改
    // 4.投放入口点击数<1，不做处理。
    private void handle2(AdGroup newAdGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration) {
        if(adPlacement.getClicks() >= 20) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, 0.02, configuration);
        } else if(adPlacement.getClicks() >= 10) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) + 0.07 - 0.01 * adPlacement.getClicks(), configuration);
        } else if(adPlacement.getClicks() >= 1) {
            new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) - 0.02, configuration);
        } else {
            // 不处理,打日志
            System.out.println(String.format("   店铺id=%s,名称=%s,广告组无广告订单且点击数大于30且投放入口点击数小于1,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
        }
    }


    // 广告组有广告订单的acos的30-60处理
    // 有广告订单处理
    // 1.投放入口acos<=30不处理
    // 2.投放入口acos>30以及<=35,bid和CPC对比,如果小于等于不改,否则修改
    // 3.投放入口acos>35,则bid和35*CPC/ACoS进行比较
    // 无广告订单处理
    // 1.投放入口点击>=20,bid改为0.02
    // 2.投放入口点击>=10&&<20,bid和CPC+0.08-0.01*点击数对比,如果小于等于不改,否则修改
    // 3.投放入口点击>=4&&<10,bid和CPC-0.01对比,如果小于等于不改,否则修改
    // 4.投放入口点击数<4，不做处理。
    private void handle3(AdGroup newAdGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration, int value1, int value2) {
        if (adPlacement.getOrders() > 0) {
            if (Double.parseDouble(adPlacement.getAcos()) <= value1) {
                // 不处理,打日志
                System.out.println(String.format("   店铺id=%s,名称=%s,广告组有广告订单且acos处于%s-60且投放入口有订单且acos小于等于%s,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),value1,value1, newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            } else if (Double.parseDouble(adPlacement.getAcos()) <= value2) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()), configuration);
            } else {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, (double) value2 * Double.parseDouble(adPlacement.getCpc()) / Double.parseDouble(adPlacement.getAcos()), configuration);
            }
        } else {
            if (adPlacement.getClicks() >= 20) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, 0.02, configuration);
            } else if (adPlacement.getClicks() >= 10) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) + 0.08 - 0.01 * adPlacement.getClicks(), configuration);
            } else if (adPlacement.getClicks() >= 4) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) - 0.01, configuration);
            } else {
                // 不处理,打日志
                System.out.println(String.format("   店铺id=%s,名称=%s,广告组有广告订单且acos处于%s-60且投放入口无订单且投放入口点击数小于4,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),value1, newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            }
        }
    }

    // 广告组有广告订单的acos的60以上的处理
    // 有广告订单处理
    // 1.投放入口acos<=30不处理
    // 2.投放入口acos>30以及<=35,bid和CPC对比,如果小于等于不改,否则修改
    // 3.投放入口acos>35,则bid和35*CPC/ACoS进行比较
    // 无广告订单处理
    // 1.投放入口点击>=20,bid改为0.02
    // 2.投放入口点击>=10&&<20,bid和CPC+0.07-0.01*点击数对比,如果小于等于不改,否则修改
    // 3.投放入口点击>=1&&<10,bid和CPC-0.02对比,如果小于等于不改,否则修改
    // 4.投放入口点击数<1，不做处理。
    private void handle4(AdGroup newAdGroup, AdGroupType adGroupType, AdPlacement adPlacement, Configuration configuration, int value1, int value2) {
        if (adPlacement.getOrders() > 0) {
            if (Double.parseDouble(adPlacement.getAcos()) <= value1) {
                // 不处理,打日志
                System.out.println(String.format("   店铺id=%s,名称=%s,广告组有广告订单且acos大于60且投放入口有订单且acos小于等于%s,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),value1, newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            } else if (Double.parseDouble(adPlacement.getAcos()) <= value2) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()), configuration);
            } else {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, (double) value2 * Double.parseDouble(adPlacement.getCpc()) / Double.parseDouble(adPlacement.getAcos()), configuration);
            }
        } else {
            if (adPlacement.getClicks() >= 20) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, 0.02, configuration);
            } else if (adPlacement.getClicks() >= 10) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) + 0.07 - 0.01 * adPlacement.getClicks(), configuration);
            } else if (adPlacement.getClicks() >= 1) {
                new AdPlacementUtils().subtractBid(newAdGroup, adGroupType, adPlacement, Double.parseDouble(adPlacement.getCpc()) - 0.02, configuration);
            } else {
                // 不处理,打日志
                System.out.println(String.format("   店铺id=%s,名称=%s,广告组有广告订单且acos大于60且投放入口无订单且投放入口点击数小于1,无需变更, country=%s, adGroupName=%s, placementName=%s， acos =%s, click=%s, cpc=%s", newAdGroup.getProfile_id(), HubUtils.getHubName(newAdGroup.getProfile_id()),newAdGroup.getStore_country(), adPlacement.getAd_group_name(), new AdPlacementUtils().getAdPlacementName(adGroupType, adPlacement), adPlacement.getAcos(), adPlacement.getClicks(), adPlacement.getCpc()));
            }
        }
    }
}
