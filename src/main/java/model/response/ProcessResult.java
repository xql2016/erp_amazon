package model.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 未处理的MSKU列表（查询不到广告组或所有广告组都符合策略）
     */
    private List<String> unprocessedMskuList = new ArrayList<>();
    
    /**
     * 未处理的MSKU按站点分组（用于输出Excel）
     * Map<ProfileId, List<MSKU>>
     */
    private Map<String, List<String>> unprocessedMskuByProfile = new HashMap<>();
    
    /**
     * 添加未处理的MSKU
     * 
     * @param profileId 站点ID
     * @param msku MSKU
     */
    public void addUnprocessedMsku(String profileId, String msku) {
        // 添加到总列表
        if (!unprocessedMskuList.contains(msku)) {
            unprocessedMskuList.add(msku);
        }
        
        // 添加到按站点分组的Map
        unprocessedMskuByProfile.computeIfAbsent(profileId, k -> new ArrayList<>()).add(msku);
    }
    
    /**
     * 添加未处理的MSKU（兼容旧接口，不知道profileId时使用）
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

