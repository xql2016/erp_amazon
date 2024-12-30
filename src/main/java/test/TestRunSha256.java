package test;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/30
 */
public class TestRunSha256 {

//    public static void main(String[] args) {
//        String inputFilePath = "/Users/qinglongxu/Desktop/zhifubao-original.xlsx"; // 输入文件路径
//        String outputFilePath = "/Users/qinglongxu/Desktop/zhifubao-original-output.xlsx"; // 输出到桌面
//
//        try (FileInputStream fis = new FileInputStream(inputFilePath);
//             Workbook workbook = new XSSFWorkbook(fis);
//             FileOutputStream fos = new FileOutputStream(outputFilePath)) {
//
//            Sheet sheet = workbook.getSheetAt(0); // 读取第一个工作表
//            int lastRowNum = sheet.getLastRowNum();
//
//            // 创建新工作表
//            Workbook outputWorkbook = new XSSFWorkbook();
//            Sheet outputSheet = outputWorkbook.createSheet("Output");
//
//            // 处理每一行
//            for (int i = 0; i <= lastRowNum; i++) {
//                Row row = sheet.getRow(i);
//                if (row == null) continue;
//
//                // 读取第二列（索引1）
//                Cell cardNumberCell = row.getCell(1);
//                String cardNumber = (cardNumberCell != null) ? cardNumberCell.getStringCellValue() : "";
//
//                // 计算SHA-256哈希值
//                String sha256Hash = calculateSHA256(cardNumber);
//
//                // 将原始卡号和哈希值写入新工作表
//                Row outputRow = outputSheet.createRow(i);
//                outputRow.createCell(0).setCellValue(cardNumber); // 原始卡号
//                outputRow.createCell(1).setCellValue(sha256Hash); // SHA-256哈希值
//            }
//
//            // 写入输出文件
//            outputWorkbook.write(fos);
//            outputWorkbook.close();
//
//            System.out.println("处理完成，结果已输出到桌面。");
//
//        } catch (IOException | NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static String calculateSHA256(String input) throws NoSuchAlgorithmException {
//        MessageDigest digest = MessageDigest.getInstance("SHA-256");
//        byte[] hashBytes = digest.digest(input.getBytes());
//
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : hashBytes) {
//            String hex = Integer.toHexString(0xff & b);
//            if (hex.length() == 1) hexString.append('0');
//            hexString.append(hex);
//        }
//        return hexString.toString();
//    }
}
