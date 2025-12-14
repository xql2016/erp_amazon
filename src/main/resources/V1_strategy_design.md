# V1产品层面广告自动化技术方案设计

## 一、需求概述

### 1.1 功能描述

V1功能实现产品层面的广告自动化，分为两个子功能：
- **控制流量**：降低表现不佳的广告组/投放入口的竞价，控制垃圾流量
- **导入流量**：提高表现良好的广告组/投放入口的竞价，增加有效流量

### 1.2 核心特点

1. **基于MSKU清单**：从Excel文件读取MSKU清单，按站点分组
2. **产品维度筛选**：根据MSKU筛选对应的广告组
3. **多广告组类型支持**：支持关键词、ASIN、自动、类目四种广告组类型
4. **处理结果统计**：记录每个MSKU的处理情况，如果所有广告组都不处理则输出提示

---

## 二、整体架构设计

### 2.1 模块划分

```
service/functionV1/
├── ProductLevelActionV1.java                    # 主入口类（抽象）
│   ├── ControlTrafficActionV1.java               # 控制流量功能
│   └── ImportTrafficActionV1.java                # 导入流量功能
├── detail/
│   ├── KeyAdGroupDetailActionV1.java            # 关键词广告组处理
│   ├── AsinAdGroupDetailActionV1.java           # ASIN广告组处理
│   ├── AutoAdGroupDetailActionV1.java           # 自动广告组处理
│   └── CategoryAdGroupDetailActionV1.java       # 类目广告组处理
└── model/
    ├── MskuSiteMapping.java                     # MSKU-站点映射模型
    └── ProcessResult.java                       # 处理结果统计模型
```

### 2.2 数据流设计

```
Excel文件 
  → Excel读取工具 
    → MSKU-站点映射 
      → 按站点分组 
        → 查询广告组（按MSKU筛选）
          → 按广告组类型分发
            → 具体处理逻辑
              → 结果统计
                → 输出提示信息
```

---

## 三、详细设计

### 3.1 Excel文件读取设计

#### 3.1.1 Excel格式说明

- **第一行**：站点ID（如：DE, FR, IT, ES等）
- **第二行开始**：每列对应站点的SKU清单
- **示例格式**：
  ```
  DE    | FR    | IT    | ES
  SKU1  | SKU4  | SKU7  | SKU10
  SKU2  | SKU5  | SKU8  | SKU11
  SKU3  | SKU6  | SKU9  | SKU12
  ```

#### 3.1.2 读取工具设计

**类名**: `tools.MskuExcelReader`

**方法**:
```java
/**
 * 读取MSKU Excel文件，返回MSKU-站点映射
 * @param filePath Excel文件路径
 * @return Map<站点代码, List<MSKU>>
 */
public static Map<String, List<String>> readMskuExcel(String filePath)
```

**实现要点**:
1. 使用现有的 `ExcelUtils.readExcel()` 方法
2. 第一行解析为站点代码列表
3. 从第二行开始，按列读取SKU
4. 过滤空值，去除重复
5. 返回 `Map<站点代码, List<MSKU>>`

#### 3.1.3 配置项设计

在 `Configuration` 中新增字段：
```java
/**
 * V1功能Excel文件路径
 */
private String v1MskuExcelPath;

/**
 * V1功能模式：control_traffic（控制流量）或 import_traffic（导入流量）
 */
private String v1Mode;
```

或在 `InputConfigurationDetail.txt` 中配置：
```json
{
  "v1MskuExcelPath": "/path/to/msku.xlsx",
  "v1Mode": "control_traffic"  // 或 "import_traffic"
}
```

---

### 3.2 主入口类设计

#### 3.2.1 控制流量主类（ControlTrafficActionV1）

**类名**: `service.functionV1.ControlTrafficActionV1`

**继承**: `AbstractAction`

**功能代码**: `function_v1_control_traffic`

**主要流程**:

1. **读取MSKU清单**
   ```java
   Map<String, List<String>> siteMskuMap = MskuExcelReader.readMskuExcel(configuration.getV1MskuExcelPath());
   ```

2. **按站点循环处理**
   ```java
   for (Map.Entry<String, List<String>> entry : siteMskuMap.entrySet()) {
       String site = entry.getKey();
       List<String> mskuList = entry.getValue();
       processSiteMsku(site, mskuList, configuration);
   }
   ```

3. **处理单个站点的MSKU**
   - 查询近3天的广告组数据（按MSKU筛选）
   - 过滤：只处理启用的广告组，跳过名称为空的
   - 按广告组类型分发处理
   - 记录处理结果

4. **结果统计和输出**
   - 统计每个MSKU的处理情况
   - 如果某个MSKU的所有广告组都不处理，输出提示

#### 3.2.2 导入流量主类（ImportTrafficActionV1）

**类名**: `service.functionV1.ImportTrafficActionV1`

**继承**: `AbstractAction`

**功能代码**: `function_v1_import_traffic`

**主要流程**: 与控制流量类似，但查询近30天的数据

---

### 3.3 广告组查询设计

#### 3.3.1 查询条件构建

**控制流量**:
```java
AdGroupRequest request = new AdGroupRequest();
request.setReport_date(DateUtils.buildReportDateString(2)); // 近3天
request.setSku(mskuList); // 按MSKU筛选
request.setCountries(Arrays.asList(site)); // 按站点筛选
if (CollectionUtils.isNotEmpty(configuration.getHubIdList())) {
    request.setProfile_ids(configuration.getHubIdList());
}
```

**导入流量**:
```java
AdGroupRequest request = new AdGroupRequest();
request.setReport_date(DateUtils.buildReportDateString(29)); // 近30天
request.setSku(mskuList);
request.setCountries(Arrays.asList(site));
// ... 其他条件
```

#### 3.3.2 查询方法

使用现有的 `AdGroupReadRepository.queryAdGroupList()` 方法

---

### 3.4 处理结果统计设计

#### 3.4.1 结果模型

**类名**: `service.functionV1.model.ProcessResult`

```java
public class ProcessResult {
    private String site;                    // 站点代码
    private String msku;                    // MSKU
    private Map<AdGroupType, Boolean> processed; // 各广告组类型是否处理
    private int totalAdGroups;               // 总广告组数
    private int processedAdGroups;           // 已处理广告组数
}
```

#### 3.4.2 统计逻辑

```java
// 为每个MSKU创建结果记录
Map<String, ProcessResult> resultMap = new HashMap<>();

// 处理广告组时更新结果
for (AdGroup adGroup : adGroupList) {
    AdGroupType type = AdUtils.getAdGroupType(adGroup.getName());
    boolean processed = processAdGroup(adGroup, type, configuration);
    
    ProcessResult result = resultMap.getOrDefault(msku, new ProcessResult(site, msku));
    result.getProcessed().put(type, processed || result.getProcessed().getOrDefault(type, false));
    result.setTotalAdGroups(result.getTotalAdGroups() + 1);
    if (processed) {
        result.setProcessedAdGroups(result.getProcessedAdGroups() + 1);
    }
}

// 输出未处理的MSKU
for (ProcessResult result : resultMap.values()) {
    if (result.getProcessedAdGroups() == 0) {
        System.out.println(String.format("MSKU %s 站点 %s 不满足%s策略，请人工分析处理", 
            result.getMsku(), result.getSite(), 
            mode.equals("control_traffic") ? "降Bid" : "加Bid"));
    }
}
```

---

### 3.5 各广告组类型处理设计

#### 3.5.1 控制流量处理逻辑

**关键词/ASIN广告组** (`KeyAdGroupDetailActionV1`, `AsinAdGroupDetailActionV1`):

1. **判断条件**
   - 近3天点击=0：不处理
   - 近3天点击>0：查询近30天数据

2. **分类处理**
   - **情况A**: 点击数≥20 且 (花费>5欧不出单 或 CPA>5)
     - 投放入口有订单：按ACOS调整bid
     - 投放入口无订单：按点击数调整bid或关闭
   - **情况B**: 其他情况
     - 投放入口有订单：按ACOS调整bid
     - 投放入口无订单：按点击数调整bid或关闭

3. **配置项**
   - `V1产品层面关键词广告组长期ACOS控制基准上限`: 35
   - `V1产品层面ASIN广告组长期ACOS控制基准上限`: 35

**自动/类目广告组** (`AutoAdGroupDetailActionV1`, `CategoryAdGroupDetailActionV1`):

逻辑类似，但bid调整规则不同（参考V1_strategy.md）

#### 3.5.2 导入流量处理逻辑

**关键词/ASIN广告组**:

1. **判断条件**
   - 近30天点击≥15：按ACOS调整bid
   - 近30天点击<15 且 昨天点击≥1：按ACOS调整bid
   - 近30天点击<15 且 昨天点击<1：分析投放入口数据

2. **投放入口处理**
   - 无订单：根据bid阈值调整广告组bid
   - 有订单：根据ACOS区间调整投放入口bid或打开投放入口

3. **配置项**
   - `V1产品层面关键词投放入口保持打开的ACOS临界值`: 60
   - `V1产品层面关键词投放入口ACOS控制基准`: 35
   - `V1产品层面关键词投放入口竞价不低于CPC-0.01的ACOS临界值`: 25
   - `V1产品层面关键词投放入口竞价不低于CPC+0.02的ACOS临界值`: 15

**自动/类目广告组**: 逻辑类似，但阈值不同

---

### 3.6 工具类设计

#### 3.6.1 MSKU Excel读取工具

**类名**: `tools.MskuExcelReader`

```java
public class MskuExcelReader {
    /**
     * 读取MSKU Excel文件
     * @param filePath Excel文件路径
     * @return Map<站点代码, List<MSKU>>
     */
    public static Map<String, List<String>> readMskuExcel(String filePath) {
        // 1. 使用ExcelUtils.readExcel读取
        // 2. 第一行作为站点代码
        // 3. 后续行按列读取SKU
        // 4. 返回映射
    }
}
```

#### 3.6.2 数值计算工具扩展

在现有的 `NumberUtils` 或新建 `BidUtils` 中添加：

```java
/**
 * 计算调整后的bid值（向上取整，保留两位小数）
 * @param baseValue 基础值
 * @param formula 计算公式结果
 * @return 调整后的bid
 */
public static double calculateBid(double baseValue, double formula) {
    // 向上取整，保留两位小数
}

/**
 * 计算CPA
 * @param spend 花费
 * @param orders 订单数
 * @return CPA值
 */
public static double calculateCPA(double spend, int orders) {
    // CPA = spend / orders (如果orders > 0)
}
```

---

## 四、配置项设计

### 4.1 控制流量配置项

在 `InputConfigurationCustom.txt` 中添加：

```json
{
  "V1产品层面关键词广告组长期ACOS控制基准上限": 35,
  "V1产品层面ASIN广告组长期ACOS控制基准上限": 35,
  "V1产品层面自动广告组长期ACOS控制基准上限": 35,
  "V1产品层面类目广告组长期ACOS控制基准上限": 35,
  "V1控制流量花费阈值（欧元）": 5,
  "V1控制流量CPA阈值": 5,
  "V1控制流量点击数阈值": 20
}
```

### 4.2 导入流量配置项

```json
{
  "V1产品层面关键词投放入口保持打开的ACOS临界值": 60,
  "V1产品层面关键词投放入口ACOS控制基准": 35,
  "V1产品层面关键词投放入口竞价不低于CPC-0.01的ACOS临界值": 25,
  "V1产品层面关键词投放入口竞价不低于CPC+0.02的ACOS临界值": 15,
  "V1产品层面ASIN投放入口保持打开的ACOS临界值": 60,
  "V1产品层面ASIN投放入口ACOS控制基准": 35,
  "V1产品层面ASIN投放入口竞价不低于CPC-0.01的ACOS临界值": 25,
  "V1产品层面ASIN投放入口竞价不低于CPC+0.02的ACOS临界值": 15,
  "V1产品层面自动投放入口保持打开的ACOS临界值": 60,
  "V1产品层面自动投放入口ACOS控制基准": 35,
  "V1产品层面自动投放入口竞价不低于CPC-0.01的ACOS临界值": 25,
  "V1产品层面自动投放入口竞价不低于CPC+0.02的ACOS临界值": 15,
  "V1产品层面类目投放入口保持打开的ACOS临界值": 60,
  "V1产品层面类目投放入口ACOS控制基准": 35,
  "V1产品层面类目投放入口竞价不低于CPC-0.01的ACOS临界值": 25,
  "V1产品层面类目投放入口竞价不低于CPC+0.02的ACOS临界值": 15,
  "V1导入流量点击数阈值（关键词/ASIN）": 15,
  "V1导入流量点击数阈值（自动/类目）": 10,
  "V1导入流量Bid调整阈值1": 0.3,
  "V1导入流量Bid调整阈值2": 0.45,
  "V1导入流量曝光阈值": 20
}
```

### 4.3 执行配置

在 `InputConfigurationDetail.txt` 中添加：

```json
{
  "executeActionCodeList": [
    "function_v1_control_traffic"  // 或 "function_v1_import_traffic"
  ],
  "v1MskuExcelPath": "/path/to/msku_list.xlsx"
}
```

---

## 五、代码结构设计

### 5.1 目录结构

```
src/main/java/service/functionV1/
├── ControlTrafficActionV1.java
├── ImportTrafficActionV1.java
├── detail/
│   ├── KeyAdGroupDetailActionV1.java
│   ├── AsinAdGroupDetailActionV1.java
│   ├── AutoAdGroupDetailActionV1.java
│   └── CategoryAdGroupDetailActionV1.java
└── model/
    ├── MskuSiteMapping.java
    └── ProcessResult.java

src/main/java/tools/
└── MskuExcelReader.java
```

### 5.2 类职责划分

| 类名 | 职责 |
|------|------|
| `ControlTrafficActionV1` | 控制流量主入口，协调整个流程 |
| `ImportTrafficActionV1` | 导入流量主入口，协调整个流程 |
| `KeyAdGroupDetailActionV1` | 关键词广告组具体处理逻辑 |
| `AsinAdGroupDetailActionV1` | ASIN广告组具体处理逻辑 |
| `AutoAdGroupDetailActionV1` | 自动广告组具体处理逻辑 |
| `CategoryAdGroupDetailActionV1` | 类目广告组具体处理逻辑 |
| `MskuExcelReader` | Excel文件读取工具 |
| `ProcessResult` | 处理结果统计模型 |

---

## 六、关键实现细节

### 6.1 Excel读取实现

```java
public static Map<String, List<String>> readMskuExcel(String filePath) {
    List<List<String>> excelData = ExcelUtils.readExcel(filePath);
    if (excelData.isEmpty()) {
        throw new IllegalArgumentException("Excel文件为空");
    }
    
    // 第一行是站点代码
    List<String> sites = excelData.get(0);
    Map<String, List<String>> result = new HashMap<>();
    
    // 初始化每个站点的列表
    for (String site : sites) {
        if (StringUtils.isNotBlank(site)) {
            result.put(site.trim(), new ArrayList<>());
        }
    }
    
    // 从第二行开始读取SKU
    for (int i = 1; i < excelData.size(); i++) {
        List<String> row = excelData.get(i);
        for (int j = 0; j < row.size() && j < sites.size(); j++) {
            String sku = row.get(j);
            String site = sites.get(j);
            if (StringUtils.isNotBlank(sku) && StringUtils.isNotBlank(site)) {
                result.get(site.trim()).add(sku.trim());
            }
        }
    }
    
    return result;
}
```

### 6.2 站点代码映射

需要将Excel中的站点代码（如"DE", "FR"）映射到国家代码，可以使用现有的 `HubUtils` 或新建映射：

```java
private static final Map<String, String> SITE_COUNTRY_MAP = new HashMap<>();
static {
    SITE_COUNTRY_MAP.put("DE", "DE");
    SITE_COUNTRY_MAP.put("FR", "FR");
    SITE_COUNTRY_MAP.put("IT", "IT");
    SITE_COUNTRY_MAP.put("ES", "ES");
}
```

### 6.3 Bid计算和调整

参考现有的 `AdPlacementUtils.subtractBid()` 和 `AdPlacementUtils.addBidWithLog()` 方法，需要：

1. **向上取整，保留两位小数**：
   ```java
   double newBid = Math.ceil(formula * 100) / 100.0;
   ```

2. **比较当前bid和新bid**：
   - 控制流量：如果当前bid > 新bid，则调整
   - 导入流量：如果当前bid < 新bid，则调整

3. **调用API调整bid**：使用现有的 `AdWriteRepository` 方法

### 6.4 CPA计算

```java
public static double calculateCPA(double spend, int orders) {
    if (orders <= 0) {
        return Double.MAX_VALUE; // 或返回0，表示无订单
    }
    return spend / orders;
}
```

### 6.5 处理结果统计

```java
// 在ControlTrafficActionV1或ImportTrafficActionV1中
Map<String, ProcessResult> resultMap = new HashMap<>();

// 处理每个MSKU
for (String msku : mskuList) {
    ProcessResult result = new ProcessResult(site, msku);
    resultMap.put(msku, result);
    
    // 查询该MSKU的广告组
    List<AdGroup> adGroups = queryAdGroupsByMsku(msku, site, configuration);
    
    // 处理每个广告组
    for (AdGroup adGroup : adGroups) {
        AdGroupType type = AdUtils.getAdGroupType(adGroup.getName());
        boolean processed = processAdGroup(adGroup, type, configuration);
        
        result.getProcessed().put(type, processed);
        result.setTotalAdGroups(result.getTotalAdGroups() + 1);
        if (processed) {
            result.setProcessedAdGroups(result.getProcessedAdGroups() + 1);
        }
    }
}

// 输出未处理的MSKU
for (ProcessResult result : resultMap.values()) {
    if (result.getProcessedAdGroups() == 0) {
        String mode = configuration.getV1Mode();
        String strategy = mode.equals("control_traffic") ? "降Bid" : "加Bid";
        System.out.println(String.format("MSKU %s 站点 %s 不满足%s策略，请人工分析处理", 
            result.getMsku(), result.getSite(), strategy));
    }
}
```

---

## 七、与现有代码的集成

### 7.1 Processor集成

在 `Processor.loadAllActionList()` 中添加：

```java
private List<AbstractAction> loadAllActionList() {
    List<AbstractAction> actionList = new ArrayList<>();
    // ... 现有代码
    actionList.add(new ControlTrafficActionV1());
    actionList.add(new ImportTrafficActionV1());
    return actionList;
}
```

### 7.2 复用现有工具类

- `AdUtils.getAdGroupType()`: 识别广告组类型
- `AdPlacementUtils`: bid调整工具
- `AdGroupUtils`: 广告组bid调整工具
- `DateUtils.buildReportDateString()`: 日期字符串构建
- `NumberUtils.parseDouble()`: 数值解析
- `HubUtils.getHubName()`: Hub名称获取

### 7.3 复用现有Repository

- `AdGroupReadRepository.queryAdGroupList()`: 查询广告组列表
- `AdGroupReadRepository.queryAdGroupListInAdGroupPage()`: 查询单个广告组详情
- `AdReadRepository.queryAdPlacementList()`: 查询投放入口列表
- `AdWriteRepository`: 写入操作（bid调整）

---

## 八、异常处理和日志

### 8.1 异常处理

1. **Excel文件不存在或格式错误**：
   ```java
   try {
       Map<String, List<String>> mskuMap = MskuExcelReader.readMskuExcel(filePath);
   } catch (Exception e) {
       System.out.println(String.format("读取MSKU Excel文件失败: %s", e.getMessage()));
       return;
   }
   ```

2. **API调用失败**：
   - 使用现有的异常处理机制
   - 记录日志，继续处理下一个MSKU

3. **数据为空**：
   - 如果某个站点的MSKU列表为空，跳过该站点
   - 如果查询不到广告组，记录日志

### 8.2 日志输出

参考现有的日志格式：

```java
// 处理开始
System.out.println(String.format("开始处理站点 %s, MSKU数量: %s", site, mskuList.size()));

// 处理广告组
System.out.println(String.format("处理广告组: %s, 类型: %s", adGroup.getName(), adGroupType.getDesc()));

// 处理结果
System.out.println(String.format("调整bid: %s -> %s", oldBid, newBid));

// 未处理提示
System.out.println(String.format("MSKU %s 站点 %s 不满足%s策略，请人工分析处理", msku, site, strategy));
```

---

## 九、测试建议

### 9.1 单元测试

1. **Excel读取测试**：
   - 正常格式的Excel文件
   - 空文件
   - 格式错误的文件
   - 包含空值的文件

2. **Bid计算测试**：
   - 向上取整逻辑
   - 保留两位小数
   - 边界值测试

3. **处理逻辑测试**：
   - 各种ACOS区间
   - 各种点击数区间
   - 有无订单的情况

### 9.2 集成测试

1. **端到端测试**：
   - 完整的Excel文件处理流程
   - 多个站点、多个MSKU的处理
   - 结果统计和输出

2. **API集成测试**：
   - 模拟API调用
   - 测试bid调整功能

---

## 十、实施步骤建议

### 阶段一：基础框架搭建
1. 创建目录结构
2. 实现Excel读取工具
3. 创建主入口类框架
4. 实现结果统计模型

### 阶段二：控制流量功能
1. 实现ControlTrafficActionV1主流程
2. 实现关键词广告组处理逻辑
3. 实现ASIN广告组处理逻辑
4. 实现自动广告组处理逻辑
5. 实现类目广告组处理逻辑

### 阶段三：导入流量功能
1. 实现ImportTrafficActionV1主流程
2. 实现各广告组类型的导入流量逻辑

### 阶段四：集成和测试
1. 集成到Processor
2. 配置项添加
3. 单元测试
4. 集成测试
5. 文档完善

---

## 十一、注意事项

1. **Excel文件格式**：需要明确第一行是站点代码，后续行是SKU
2. **站点代码映射**：需要确认Excel中的站点代码格式（如"DE"还是"德国"）
3. **MSKU格式**：需要确认MSKU在广告系统中的格式，可能需要转换
4. **性能考虑**：如果MSKU数量很大，考虑批量查询或异步处理
5. **错误恢复**：如果某个MSKU处理失败，应该继续处理其他MSKU
6. **日志完整性**：确保所有关键操作都有日志记录
7. **配置灵活性**：所有阈值都应该可配置，不要硬编码

---

*设计文档版本: 1.0*  
*创建时间: 2024年*

