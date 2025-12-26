package service.functionV1.importAd;

import model.configuration.Configuration;
import model.constant.FilePath;
import model.enums.AdGroupType;
import model.request.AdGroupRequest;
import model.response.AdGroup;
import model.response.ProcessResult;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import repository.read.AdGroupReadRepository;
import tools.AdUtils;
import tools.DateUtils;
import tools.ExcelUtils;
import tools.HubUtils;
import tools.MskuExcelReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * V1产品层面导入流量处理类
 * 负责读取导入流量Excel文件，按站点处理MSKU，提高Bid
 * 
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2025/01/XX
 */
public class ImportTrafficActionV1 {

    /**
     * 执行导入流量处理
     * 
     * @param configuration 配置信息
     * @return 处理结果统计
     */
    public ProcessResult execute(Configuration configuration) {
        ProcessResult result = new ProcessResult();
        
        System.out.println("========== 开始处理V1产品层面导入流量 ==========");
        
        // 读取导入流量Excel文件
        Map<String, List<String>> mskuMap = MskuExcelReader.readMskuExcel(FilePath.v1SkuAddExcel);
        
        if (mskuMap.isEmpty()) {
            System.out.println("导入流量Excel文件为空或不存在，跳过处理");
            return result;
        }
        
        // 按站点处理MSKU
        for (Map.Entry<String, List<String>> entry : mskuMap.entrySet()) {
            String profileId = entry.getKey();
            List<String> mskuList = entry.getValue();
            
            if (CollectionUtils.isEmpty(mskuList)) {
                System.out.println(String.format("站点 %s MSKU列表为空，跳过", profileId));
                continue;
            }
            
            System.out.println(String.format("开始处理站点 %s, MSKU数量: %s", profileId, mskuList.size()));
            
            // 处理该站点的每个MSKU
            for (int i = 0; i < mskuList.size(); i++) {
                String msku = mskuList.get(i);
                System.out.println(String.format("  处理MSKU %s (%d/%d)", msku, i + 1, mskuList.size()));
                
                // 查询该MSKU的广告组（近30天数据）
                AdGroupRequest adGroupRequest = new AdGroupRequest();
                adGroupRequest.setProfile_id(Long.parseLong(profileId));
                List<String> mskuTempList = new ArrayList<>();
                mskuTempList.add(msku);
                adGroupRequest.setSku(mskuTempList);
                adGroupRequest.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
                
                List<AdGroup> adGroupList = new AdGroupReadRepository().queryAdGroupList(adGroupRequest, configuration);
                
                if (CollectionUtils.isEmpty(adGroupList)) {
                    // 查询不到广告组，记录到未处理列表
                    result.addUnprocessedMsku(profileId, msku);
                    System.out.println(String.format("    MSKU %s 查询不到广告组", msku));
                    continue;
                }
                
                // 处理每个广告组
                boolean hasProcessed = false;
                for (AdGroup adGroup : adGroupList) {
                    // 第一行的广告组不处理
                    if (StringUtils.isBlank(adGroup.getName())) {
                        continue;
                    }
                    
                    // 判断广告组类型
                    AdGroupType adGroupType = AdUtils.getAdGroupType(adGroup.getName());
                    if (adGroupType == null) {
                        System.out.println(String.format("    店铺id=%s,名称=%s,not find adGroupType, adGroup.name=%s", 
                                adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getName()));
                        continue;
                    }
                    
                    // 只处理启用的广告组
                    if (!"enabled".equalsIgnoreCase(adGroup.getState())) {
                        System.out.println(String.format("    店铺id=%s,名称=%s,广告组未启用, adGroup.name=%s", 
                                adGroup.getProfile_id(), HubUtils.getHubName(adGroup.getProfile_id()), adGroup.getName()));
                        continue;
                    }
                    
                    // 处理该广告组
                    System.out.println(String.format("    处理广告组: %s, 类型: %s", adGroup.getName(), adGroupType.getDesc()));
                    boolean bidChanged = executeAdGroupDetail(adGroup, adGroupType, configuration);
                    if (bidChanged) {
                        hasProcessed = true;
                    }
                }
                
                if (hasProcessed) {
                    result.incrementProcessed();
                } else {
                    // 该MSKU的所有广告组都符合策略要求，无需变更bid
                    result.addUnprocessedMsku(profileId, msku);
                    System.out.println(String.format("    MSKU %s 所有广告组都符合策略要求，无需变更bid", msku));
                }
            }
        }
        
        // 输出统计结果
        System.out.println(String.format("========== 导入流量处理完成 =========="));
        System.out.println(String.format("已处理MSKU数量: %d", result.getProcessedCount()));
        if (!result.getUnprocessedMskuList().isEmpty()) {
            System.out.println(String.format("未处理MSKU列表: %s", String.join(", ", result.getUnprocessedMskuList())));
            
            // 输出未处理MSKU到Excel
            String outputPath = FilePath.v1SkuAddExcel_Output;
            exportUnprocessedMskuToExcel(result.getUnprocessedMskuByProfile(), outputPath);
            System.out.println(String.format("未处理MSKU已输出到: %s", outputPath));
        }
        
        return result;
    }
    
    /**
     * 导出未处理的MSKU到Excel文件
     * Excel格式与输入格式相同：第一行为站点代码，从第二行开始每列是对应站点的MSKU
     * 
     * @param unprocessedMskuByProfile 按站点分组的未处理MSKU
     * @param outputPath 输出文件路径
     */
    private void exportUnprocessedMskuToExcel(Map<String, List<String>> unprocessedMskuByProfile, String outputPath) {
        if (unprocessedMskuByProfile.isEmpty()) {
            return;
        }
        
        try {
            // 准备表头（站点ID列表）
            List<String> profileIds = new ArrayList<>(unprocessedMskuByProfile.keySet());
            
            // 准备数据（按列组织：每列是一个站点的MSKU列表）
            // 先找出最长的MSKU列表，确定需要多少行
            int maxRows = unprocessedMskuByProfile.values().stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(0);
            
            // 构建数据列表
            List<List<String>> dataList = new ArrayList<>();
            dataList.add(profileIds); // 第一行是表头
            
            // 从第二行开始填充数据
            for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
                List<String> row = new ArrayList<>();
                for (String profileId : profileIds) {
                    List<String> mskuList = unprocessedMskuByProfile.get(profileId);
                    if (rowIndex < mskuList.size()) {
                        row.add(mskuList.get(rowIndex));
                    } else {
                        row.add(""); // 空单元格
                    }
                }
                dataList.add(row);
            }
            
            // 写入Excel（注意：ExcelUtils.writeExcel的第一个参数是header，第二个是dataList）
            // 但实际上这里的dataList已经包含了header，所以需要分开
            ExcelUtils.writeExcel(outputPath, profileIds, dataList);
            
        } catch (Exception e) {
            System.out.println(String.format("导出未处理MSKU到Excel失败: %s, 错误信息: %s", outputPath, e.getMessage()));
        }
    }
    
    /**
     * 根据广告组类型调用对应的处理类
     * 
     * @return 是否执行了bid变更操作
     */
    private boolean executeAdGroupDetail(AdGroup adGroup, AdGroupType adGroupType, Configuration configuration) {
        switch (adGroupType) {
            case KEY_AD_GROUP:
                return new KeyAdGroupDetailActionV1().executeAdGroupDetail(adGroup, adGroupType, configuration);
            case ASIN_AD_GROUP:
                return new AsinAdGroupDetailActionV1().executeAdGroupDetail(adGroup, adGroupType, configuration);
            case AUTO_AD_GROUP:
                return new AutoAdGroupDetailActionV1().executeAdGroupDetail(adGroup, adGroupType, configuration);
            case CATEGORY_AD_GROUP:
                return new CategoryAdGroupDetailActionV1().executeAdGroupDetail(adGroup, adGroupType, configuration);
            default:
                return false;
        }
    }
}

