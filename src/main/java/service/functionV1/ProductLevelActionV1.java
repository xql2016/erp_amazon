package service.functionV1;

import model.configuration.Configuration;
import model.response.ProcessResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import service.AbstractAction;
import service.functionV1.controlAd.*;
import service.functionV1.importAd.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * V1产品层面广告自动化统一入口类
 * 内部先执行控制流量，再执行导入流量
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class ProductLevelActionV1 extends AbstractAction {

    @Override
    protected void execute(Configuration configuration) throws Exception {
        System.out.println("========== 开始执行V1产品层面广告自动化 ==========");
        
        // 先执行控制流量
        ProcessResult controlResult = new ControlTrafficActionV1().execute(configuration);
        
        // 再执行导入流量
        ProcessResult importResult = new ImportTrafficActionV1().execute(configuration);
        
        // 输出总体统计
        System.out.println("========== V1产品层面广告自动化执行完成 ==========");
        System.out.println(String.format("控制流量 - 已处理MSKU数量: %d, 未处理MSKU数量: %d", 
                controlResult.getProcessedCount(), gettUnprocessedMskuList(controlResult.getUnprocessedMskuByProfile())));
        System.out.println(String.format("导入流量 - 已处理MSKU数量: %d, 未处理MSKU数量: %d", 
                importResult.getProcessedCount(), gettUnprocessedMskuList(importResult.getUnprocessedMskuByProfile())));
    }

    private long gettUnprocessedMskuList(Map<String, List<String>> unprocessedMskuByProfile) {
        if(MapUtils.isEmpty(unprocessedMskuByProfile)) {
            return 0;
        }
        List<String> skuList = new ArrayList<>();
        for(String key : unprocessedMskuByProfile.keySet()) {
            List<String> tempList = unprocessedMskuByProfile.get(key);
            if(CollectionUtils.isNotEmpty(tempList)) {
                skuList.addAll(tempList);
            }
        }
        return skuList.size();
    }

    @Override
    protected String getCode() {
        return "function_v1";
    }
}

