package tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public class FileUtils {

    public static String loadFile(String filePath) {
        StringBuilder sb = new StringBuilder();
        // 使用FileReader读取文件
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
