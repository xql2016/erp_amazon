package model.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * V1产品层面处理结果统计模型
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
@Data
public class ProcessResult {
    
    /**
     * 已处理的MSKU数量
     */
    private int processedCount = 0;
    
    /**
     * 未处理的MSKU列表（查询不到广告组）
     */
    private List<String> unprocessedMskuList = new ArrayList<>();
    
    /**
     * 添加未处理的MSKU
     */
    public void addUnprocessedMsku(String msku) {
        if (!unprocessedMskuList.contains(msku)) {
            unprocessedMskuList.add(msku);
        }
    }
    
    /**
     * 增加已处理数量
     */
    public void incrementProcessed() {
        processedCount++;
    }
}

