package service.adControl;

import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.configuration.Configuration;
import model.response.AdGroup;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import service.AbstractAction;
import tools.AdUtils;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/4
 */
//@Slf4j
public abstract class AbstractionAdControlAction extends AbstractAction {

    protected void execute(Configuration configuration) throws Exception {
        // 0. 加载各自特有配置
        loadSpecialConfiguration(configuration);
        // 1. 组装查询参数
        AdGroupRequest adGroupRequest = buildAdGroupRequest(configuration);
        // 2. 查询广告组列表
        AdGroupReadRepository adGroupReadRepository = new AdGroupReadRepository();
        List<AdGroup> adGroupList = adGroupReadRepository.queryAdGroupList(adGroupRequest, configuration);
        System.out.println(String.format("adGroupList.size=%s", adGroupList.size()));
        // 3. 处理广告组
        doOperateAdGroupList(adGroupList, configuration);
    }

    protected abstract AdGroupRequest buildAdGroupRequest(Configuration configuration);

    protected abstract boolean needHandle(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration);

    protected abstract void executeAdGroup(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration);

    protected abstract void loadSpecialConfiguration(Configuration configuration) throws Exception;

    protected AdGroupType findAdGroupType(AdGroup adGroup) {
        if(StringUtils.isBlank(adGroup.getName())) {
            return null;
        }
        return AdUtils.getAdGroupType(adGroup.getName());
    }

    protected void doOperateAdGroupList(List<AdGroup> adGroupList, Configuration configuration) {
        // 对每个广告组判断是否需要处理
        for(AdGroup adGroup : adGroupList) {
            if(StringUtils.isBlank(adGroup.getName())) {
                continue;
            }
            AdGroupType adGroupType = findAdGroupType(adGroup);
            if(null == adGroupType) {
                System.out.println(String.format("not find adGroupType, adGroup.name=%s", adGroup.getName()));
                continue;
            }
            if(needHandle(adGroup, adGroupType, configuration)) {
                // 4. 处理每个广告组
                System.out.println(String.format("handle adGroup=%s, adGroupType=%s", adGroup.getName(), adGroupType.getDesc()));
                executeAdGroup(adGroup, adGroupType, configuration);
            }
        }
    }
}
