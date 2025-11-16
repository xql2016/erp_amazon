package service.functionV7.spamTrafficFromAdGroup;

import lombok.Data;

import java.util.List;

/**
 * 控制广告入口的垃圾流量
 *
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
@Data
public class SpamTrafficFromAdGroupConfiguration {

    // [1.查询广告组条件, 2.根据广告组查询出来数据,对广告组遍历处理, [3.不同的广告组类型[4.进行不同条件的投放入口查询, 5.投放处理]]]
    private List<SpamTrafficFromAdGroupDetail> spamTrafficFromAdGroupDetailList; // 策略列表

}
