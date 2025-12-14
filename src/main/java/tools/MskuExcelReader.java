package tools;

import model.constant.FilePath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * V1产品层面MSKU Excel读取工具类
 * Excel格式：第一行为站点代码（profile_id），从第二行开始每列都是对应站点的SKU清单
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class MskuExcelReader {

    /**
     * 读取MSKU Excel文件，返回按站点分组的MSKU列表
     * 
     * @param filePath Excel文件路径
     * @return Map<站点profile_id, List<MSKU>>，如果文件不存在或为空则返回空Map
     */
    public static Map<String, List<String>> readMskuExcel(String filePath) {
        Map<String, List<String>> result = new HashMap<>();
        
        // 先检查文件是否存在
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(String.format("Excel文件不存在，跳过处理: %s", filePath));
            return result;
        }
        
        try {
            // 读取Excel文件
            List<List<String>> excelData = ExcelUtils.readExcel(filePath);
            
            // 检查文件是否为空（只有表头或没有任何数据）
            if (CollectionUtils.isEmpty(excelData) || excelData.size() <= 1) {
                System.out.println(String.format("Excel文件为空或只有表头，跳过处理: %s", filePath));
                return result;
            }
            
            // 第一行是站点代码（profile_id）
            List<String> headerRow = excelData.get(0);
            if (CollectionUtils.isEmpty(headerRow)) {
                System.out.println(String.format("Excel文件表头为空，跳过处理: %s", filePath));
                return result;
            }
            
            // 从第二行开始读取MSKU数据
            for (int rowIndex = 1; rowIndex < excelData.size(); rowIndex++) {
                List<String> row = excelData.get(rowIndex);
                if (CollectionUtils.isEmpty(row)) {
                    continue;
                }
                
                // 遍历每一列（对应一个站点）
                for (int colIndex = 0; colIndex < headerRow.size() && colIndex < row.size(); colIndex++) {
                    String profileId = headerRow.get(colIndex);
                    String msku = row.get(colIndex);
                    
                    // 过滤空值和"null"字符串
                    if (StringUtils.isBlank(profileId) || StringUtils.isBlank(msku) || "null".equalsIgnoreCase(msku)) {
                        continue;
                    }
                    
                    // 去除profile_id和msku的前后空格
                    profileId = profileId.trim();
                    msku = msku.trim();
                    
                    // 添加到结果中
                    result.computeIfAbsent(profileId, k -> new ArrayList<>()).add(msku);
                }
            }
            
            // 对每个站点的MSKU列表去重
            result = result.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().stream().distinct().collect(Collectors.toList())
                    ));
            
        } catch (Exception e) {
            System.out.println(String.format("读取MSKU Excel文件失败: %s, 错误信息: %s", filePath, e.getMessage()));
            e.printStackTrace();
            // 文件格式错误时终止整个流程
            throw new RuntimeException(String.format("Excel文件格式错误: %s", filePath), e);
        }
        
        return result;
    }
}

