# V1功能Excel文件格式说明及生成指南

## 一、Excel文件格式要求

### 1. 控制流量MSKU清单.xlsx

**用途**: 存放需要降低Bid控制垃圾流量的MSKU清单

**格式要求**:
```
| 123456789  | 234567890  | 345678901  | 456789012  | 567890123  |
|------------|------------|------------|------------|------------|
| CTRL-DE-001| CTRL-FR-001| CTRL-IT-001| CTRL-ES-001| CTRL-UK-001|
| CTRL-DE-002| CTRL-FR-002| CTRL-IT-002| CTRL-ES-002| CTRL-UK-002|
| CTRL-DE-003| CTRL-FR-003| CTRL-IT-003| CTRL-ES-003| CTRL-UK-003|
| ...        | ...        | ...        | ...        | ...        |
```

**说明**:
- **第一行**: 站点的ProfileId（数字，如123456789）
- **第二行开始**: 每列对应该站点的MSKU清单
- **可以有多列**: 每列代表一个不同的站点
- **可以有多行**: 每个站点可以有多个MSKU

### 2. 导入流量MSKU清单.xlsx

**用途**: 存放需要提高Bid增加有效流量的MSKU清单

**格式要求**: 与控制流量Excel完全相同

```
| 123456789  | 234567890  | 345678901  | 456789012  | 567890123  |
|------------|------------|------------|------------|------------|
| IMPT-DE-001| IMPT-FR-001| IMPT-IT-001| IMPT-ES-001| IMPT-UK-001|
| IMPT-DE-002| IMPT-FR-002| IMPT-IT-002| IMPT-ES-002| IMPT-UK-002|
| IMPT-DE-003| IMPT-FR-003| IMPT-IT-003| IMPT-ES-003| IMPT-UK-003|
| ...        | ...        | ...        | ...        | ...        |
```

---

## 二、如何生成Excel文件

### 方法1: 使用Java生成器（推荐）

已提供 `V1ExcelGenerator.java` 工具类，在IDEA中：

1. 打开文件: `src/main/java/tools/V1ExcelGenerator.java`
2. 右键点击文件 → Run 'V1ExcelGenerator.main()'
3. 自动生成两个Excel示例文件到resources目录

### 方法2: 使用Python脚本生成

如果没有Java环境，可以使用Python生成（需安装openpyxl）:

```python
pip install openpyxl
python generate_v1_excel.py
```

Python脚本内容见下方附录。

### 方法3: 手动创建Excel

1. 新建Excel文件
2. 在第一行填入站点ProfileId
3. 从第二行开始，每列填入对应站点的MSKU
4. 保存为 `.xlsx` 格式
5. 放到 `src/main/resources/` 目录下

---

## 三、Excel文件放置位置

将生成的Excel文件放到以下位置：

```
src/main/resources/
├── 控制流量MSKU清单.xlsx
└── 导入流量MSKU清单.xlsx
```

---

## 四、配置文件中指定Excel路径

在 `InputConfigurationDetail.txt` 中配置：

```json
{
  "v1SkuControlExcel路径": "src/main/resources/控制流量MSKU清单.xlsx",
  "v1SkuAddExcel路径": "src/main/resources/导入流量MSKU清单.xlsx"
}
```

或使用绝对路径：

```json
{
  "v1SkuControlExcel路径": "/Users/xql/cursor/erp_amazon/src/main/resources/控制流量MSKU清单.xlsx",
  "v1SkuAddExcel路径": "/Users/xql/cursor/erp_amazon/src/main/resources/导入流量MSKU清单.xlsx"
}
```

---

## 五、实际使用示例

### 示例1: 德国和法国站点的MSKU

```
| 3123456789 | 3234567890 |
|------------|------------|
| ABC-DE-001 | ABC-FR-001 |
| ABC-DE-002 | ABC-FR-002 |
| ABC-DE-003 | ABC-FR-003 |
| DEF-DE-001 | DEF-FR-001 |
| XYZ-DE-999 | XYZ-FR-999 |
```

### 示例2: 单个站点多个MSKU

```
| 3123456789 |
|------------|
| PRODUCT-A  |
| PRODUCT-B  |
| PRODUCT-C  |
| PRODUCT-D  |
| PRODUCT-E  |
```

### 示例3: 不同站点不同数量MSKU

```
| 3123456789 | 3234567890 | 3345678901 |
|------------|------------|------------|
| ABC-001    | DEF-001    | GHI-001    |
| ABC-002    | DEF-002    | GHI-002    |
| ABC-003    |            | GHI-003    |
| ABC-004    |            | GHI-004    |
|            |            | GHI-005    |
```

**注意**: 空单元格会被自动跳过，不影响处理。

---

## 六、常见问题

### Q1: ProfileId从哪里获取？
A: ProfileId是亚马逊广告账户的唯一标识，可以从广告管理后台或API中获取。

### Q2: MSKU格式有要求吗？
A: 没有特定格式要求，只要与系统中的SKU字段能匹配即可。

### Q3: 可以同时处理多个站点吗？
A: 可以，Excel支持多列，每列代表一个站点及其MSKU清单。

### Q4: 单个站点可以有多少个MSKU？
A: 理论上无限制，但建议单次处理不超过1000个MSKU以保证性能。

### Q5: 可以使用.xls格式吗？
A: 建议使用.xlsx格式（Excel 2007+），但系统也支持.xls格式。

### Q6: Excel文件可以有多个Sheet吗？
A: 系统只读取第一个Sheet，其他Sheet会被忽略。

---

## 附录：Python生成脚本

创建文件 `generate_v1_excel.py`:

```python
import openpyxl
from openpyxl import Workbook

def generate_control_traffic_excel():
    """生成控制流量MSKU清单.xlsx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "控制流量MSKU"
    
    # 第一行：站点ProfileId
    profile_ids = ["123456789", "234567890", "345678901", "456789012", "567890123"]
    for col, profile_id in enumerate(profile_ids, start=1):
        ws.cell(row=1, column=col, value=profile_id)
    
    # 第二行开始：MSKU清单
    msku_data = [
        ["CTRL-DE-001", "CTRL-FR-001", "CTRL-IT-001", "CTRL-ES-001", "CTRL-UK-001"],
        ["CTRL-DE-002", "CTRL-FR-002", "CTRL-IT-002", "CTRL-ES-002", "CTRL-UK-002"],
        ["CTRL-DE-003", "CTRL-FR-003", "CTRL-IT-003", "CTRL-ES-003", "CTRL-UK-003"],
        ["CTRL-DE-004", "CTRL-FR-004", "CTRL-IT-004", "CTRL-ES-004", "CTRL-UK-004"],
        ["CTRL-DE-005", "CTRL-FR-005", "CTRL-IT-005", "CTRL-ES-005", "CTRL-UK-005"],
        ["CTRL-DE-006", "CTRL-FR-006", "CTRL-IT-006", "CTRL-ES-006", "CTRL-UK-006"],
        ["CTRL-DE-007", "CTRL-FR-007", "CTRL-IT-007", "CTRL-ES-007", "CTRL-UK-007"],
        ["CTRL-DE-008", "CTRL-FR-008", "CTRL-IT-008", "CTRL-ES-008", "CTRL-UK-008"],
        ["CTRL-DE-009", "CTRL-FR-009", "CTRL-IT-009", "CTRL-ES-009", "CTRL-UK-009"],
        ["CTRL-DE-010", "CTRL-FR-010", "CTRL-IT-010", "CTRL-ES-010", "CTRL-UK-010"],
    ]
    
    for row_idx, row_data in enumerate(msku_data, start=2):
        for col_idx, msku in enumerate(row_data, start=1):
            ws.cell(row=row_idx, column=col_idx, value=msku)
    
    # 保存文件
    wb.save("控制流量MSKU清单.xlsx")
    print("✓ 控制流量MSKU清单.xlsx 生成成功！")

def generate_import_traffic_excel():
    """生成导入流量MSKU清单.xlsx"""
    wb = Workbook()
    ws = wb.active
    ws.title = "导入流量MSKU"
    
    # 第一行：站点ProfileId
    profile_ids = ["123456789", "234567890", "345678901", "456789012", "567890123"]
    for col, profile_id in enumerate(profile_ids, start=1):
        ws.cell(row=1, column=col, value=profile_id)
    
    # 第二行开始：MSKU清单
    msku_data = [
        ["IMPT-DE-001", "IMPT-FR-001", "IMPT-IT-001", "IMPT-ES-001", "IMPT-UK-001"],
        ["IMPT-DE-002", "IMPT-FR-002", "IMPT-IT-002", "IMPT-ES-002", "IMPT-UK-002"],
        ["IMPT-DE-003", "IMPT-FR-003", "IMPT-IT-003", "IMPT-ES-003", "IMPT-UK-003"],
        ["IMPT-DE-004", "IMPT-FR-004", "IMPT-IT-004", "IMPT-ES-004", "IMPT-UK-004"],
        ["IMPT-DE-005", "IMPT-FR-005", "IMPT-IT-005", "IMPT-ES-005", "IMPT-UK-005"],
        ["IMPT-DE-006", "IMPT-FR-006", "IMPT-IT-006", "IMPT-ES-006", "IMPT-UK-006"],
        ["IMPT-DE-007", "IMPT-FR-007", "IMPT-IT-007", "IMPT-ES-007", "IMPT-UK-007"],
        ["IMPT-DE-008", "IMPT-FR-008", "IMPT-IT-008", "IMPT-ES-008", "IMPT-UK-008"],
        ["IMPT-DE-009", "IMPT-FR-009", "IMPT-IT-009", "IMPT-ES-009", "IMPT-UK-009"],
        ["IMPT-DE-010", "IMPT-FR-010", "IMPT-IT-010", "IMPT-ES-010", "IMPT-UK-010"],
        ["IMPT-DE-011", "IMPT-FR-011", "IMPT-IT-011", "IMPT-ES-011", "IMPT-UK-011"],
        ["IMPT-DE-012", "IMPT-FR-012", "IMPT-IT-012", "IMPT-ES-012", "IMPT-UK-012"],
    ]
    
    for row_idx, row_data in enumerate(msku_data, start=2):
        for col_idx, msku in enumerate(row_data, start=1):
            ws.cell(row=row_idx, column=col_idx, value=msku)
    
    # 保存文件
    wb.save("导入流量MSKU清单.xlsx")
    print("✓ 导入流量MSKU清单.xlsx 生成成功！")

if __name__ == "__main__":
    print("开始生成V1功能Excel文件...")
    generate_control_traffic_excel()
    generate_import_traffic_excel()
    print("\n全部完成！请将生成的Excel文件移动到 src/main/resources/ 目录下。")
```

### 使用方法：

1. 将上述Python代码保存为 `generate_v1_excel.py`
2. 在终端运行: `python generate_v1_excel.py`
3. 将生成的两个Excel文件移动到 `src/main/resources/` 目录
4. 在配置文件中指定文件路径即可使用

---

**文档版本**: v1.0  
**创建时间**: 2025-12-14  
**维护者**: ERP自动化团队

