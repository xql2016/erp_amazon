package tools;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/28
 */
public class ExcelUtils {

    @SneakyThrows
    public static List<List<String>> readExcel(String filePath){
        FileInputStream inputStream = new FileInputStream(filePath);
        Workbook workbook;
        if (filePath.endsWith(".xls")) {
            workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(inputStream);
        } else if (filePath.endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not an Excel file.");
        }

        List<List<String>> result = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            List<String> rowResult = new ArrayList<>();
            for (Cell cell : row) {
                rowResult.add(cell.toString());
            }
            result.add(rowResult);
        }
        workbook.close();
        inputStream.close();
        return result;
    }

    @SneakyThrows
    public static void writeExcel(String filePath, List<String> header, List<List<String>> dataList){
        // 创建新的Excel工作簿
        XSSFWorkbook workbook = new XSSFWorkbook();
        // 创建一个工作表
        Sheet sheet = workbook.createSheet("Sheet1");

        // 创建header
        Row headerRow = sheet.createRow(0);
        for (int colIndex = 0; colIndex < header.size(); colIndex++) {
            Cell cell = headerRow.createCell(colIndex);
            cell.setCellValue(header.get(colIndex));
        }

        // 遍历数据列表，创建行和单元格
        for (int rowIndex = 1; rowIndex < dataList.size(); rowIndex++) {
            Row row = sheet.createRow(rowIndex);
            for (int colIndex = 0; colIndex < dataList.get(rowIndex).size(); colIndex++) {
                Cell cell = row.createCell(colIndex);
                cell.setCellValue(dataList.get(rowIndex).get(colIndex));
            }
        }

        // 自动调整列宽
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        // 将工作簿写入文件
        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            workbook.write(outputStream);
        }
        // 关闭工作簿
        workbook.close();
    }
}
