package service;

import model.configuration.*;
import org.apache.commons.collections4.CollectionUtils;
import service.configuration.ConfigurationService;
import service.functionV2.AdGroupActionV2;
import service.functionV3Common.AdGroupActionV3Common;
import service.functionV3Enhanced.AdGroupActionV3Enhanced;
import service.functionV5.SpamTrafficFromPlaceAction;
import service.functionV6.SpamValidFromPlaceAction;
import service.functionV7.SpamTrafficFromAdGroupAction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/4
 */
public class Processor {

    public void doHandle() {
        try {
            // 加载用户配置文件
            Configuration configuration = new ConfigurationService().loadLocalConfiguration();
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

    private List<AbstractAction> loadAllActionList() {
        List<AbstractAction> abstractionActionList = new ArrayList<>();
        abstractionActionList.add(new AdGroupActionV2());
        abstractionActionList.add(new AdGroupActionV3Common());
        abstractionActionList.add(new AdGroupActionV3Enhanced());
        abstractionActionList.add(new SpamTrafficFromPlaceAction());
        abstractionActionList.add(new SpamValidFromPlaceAction());
        abstractionActionList.add(new SpamTrafficFromAdGroupAction());
        return abstractionActionList;
    }
}
