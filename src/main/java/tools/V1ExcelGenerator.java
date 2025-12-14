package tools;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;

/**
 * V1功能Excel文件生成工具
 * 用于生成控制流量和导入流量的MSKU清单Excel模板
 * 
 * @author ERP Team
 * @version 1.0
 */
public class V1ExcelGenerator {

    public static void main(String[] args) {
        try {
            // 生成控制流量Excel
            generateControlTrafficExcel();
            System.out.println("控制流量MSKU清单.xlsx 生成成功！");
            
            // 生成导入流量Excel
            generateImportTrafficExcel();
            System.out.println("导入流量MSKU清单.xlsx 生成成功！");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成控制流量MSKU清单Excel（示例数据）
     */
    public static void generateControlTrafficExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("控制流量MSKU");

        // 第一行：站点ProfileId（示例数据）
        Row headerRow = sheet.createRow(0);
        String[] profileIds = {"123456789", "234567890", "345678901", "456789012", "567890123"};
        for (int i = 0; i < profileIds.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(profileIds[i]);
        }

        // 第二行开始：MSKU清单（示例数据）
        // 站点1的MSKU
        String[][] mskuData = {
            {"CTRL-DE-001", "CTRL-FR-001", "CTRL-IT-001", "CTRL-ES-001", "CTRL-UK-001"},
            {"CTRL-DE-002", "CTRL-FR-002", "CTRL-IT-002", "CTRL-ES-002", "CTRL-UK-002"},
            {"CTRL-DE-003", "CTRL-FR-003", "CTRL-IT-003", "CTRL-ES-003", "CTRL-UK-003"},
            {"CTRL-DE-004", "CTRL-FR-004", "CTRL-IT-004", "CTRL-ES-004", "CTRL-UK-004"},
            {"CTRL-DE-005", "CTRL-FR-005", "CTRL-IT-005", "CTRL-ES-005", "CTRL-UK-005"},
            {"CTRL-DE-006", "CTRL-FR-006", "CTRL-IT-006", "CTRL-ES-006", "CTRL-UK-006"},
            {"CTRL-DE-007", "CTRL-FR-007", "CTRL-IT-007", "CTRL-ES-007", "CTRL-UK-007"},
            {"CTRL-DE-008", "CTRL-FR-008", "CTRL-IT-008", "CTRL-ES-008", "CTRL-UK-008"},
            {"CTRL-DE-009", "CTRL-FR-009", "CTRL-IT-009", "CTRL-ES-009", "CTRL-UK-009"},
            {"CTRL-DE-010", "CTRL-FR-010", "CTRL-IT-010", "CTRL-ES-010", "CTRL-UK-010"}
        };

        for (int i = 0; i < mskuData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < mskuData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(mskuData[i][j]);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < profileIds.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        String filePath = "src/main/resources/控制流量MSKU清单.xlsx";
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    /**
     * 生成导入流量MSKU清单Excel（示例数据）
     */
    public static void generateImportTrafficExcel() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("导入流量MSKU");

        // 第一行：站点ProfileId（示例数据）
        Row headerRow = sheet.createRow(0);
        String[] profileIds = {"123456789", "234567890", "345678901", "456789012", "567890123"};
        for (int i = 0; i < profileIds.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(profileIds[i]);
        }

        // 第二行开始：MSKU清单（示例数据）
        String[][] mskuData = {
            {"IMPT-DE-001", "IMPT-FR-001", "IMPT-IT-001", "IMPT-ES-001", "IMPT-UK-001"},
            {"IMPT-DE-002", "IMPT-FR-002", "IMPT-IT-002", "IMPT-ES-002", "IMPT-UK-002"},
            {"IMPT-DE-003", "IMPT-FR-003", "IMPT-IT-003", "IMPT-ES-003", "IMPT-UK-003"},
            {"IMPT-DE-004", "IMPT-FR-004", "IMPT-IT-004", "IMPT-ES-004", "IMPT-UK-004"},
            {"IMPT-DE-005", "IMPT-FR-005", "IMPT-IT-005", "IMPT-ES-005", "IMPT-UK-005"},
            {"IMPT-DE-006", "IMPT-FR-006", "IMPT-IT-006", "IMPT-ES-006", "IMPT-UK-006"},
            {"IMPT-DE-007", "IMPT-FR-007", "IMPT-IT-007", "IMPT-ES-007", "IMPT-UK-007"},
            {"IMPT-DE-008", "IMPT-FR-008", "IMPT-IT-008", "IMPT-ES-008", "IMPT-UK-008"},
            {"IMPT-DE-009", "IMPT-FR-009", "IMPT-IT-009", "IMPT-ES-009", "IMPT-UK-009"},
            {"IMPT-DE-010", "IMPT-FR-010", "IMPT-IT-010", "IMPT-ES-010", "IMPT-UK-010"},
            {"IMPT-DE-011", "IMPT-FR-011", "IMPT-IT-011", "IMPT-ES-011", "IMPT-UK-011"},
            {"IMPT-DE-012", "IMPT-FR-012", "IMPT-IT-012", "IMPT-ES-012", "IMPT-UK-012"}
        };

        for (int i = 0; i < mskuData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < mskuData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(mskuData[i][j]);
            }
        }

        // 自动调整列宽
        for (int i = 0; i < profileIds.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入文件
        String filePath = "src/main/resources/导入流量MSKU清单.xlsx";
        FileOutputStream outputStream = new FileOutputStream(filePath);
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}

