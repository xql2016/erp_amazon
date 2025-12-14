package tools;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/28
 */
public class NumberUtils {

    public static String toString(Long val) {
        if(null == val) {
            return StringUtils.EMPTY;
        }
        return val.toString();
    }

    public static String toString(Integer val) {
        if(null == val) {
            return StringUtils.EMPTY;
        }
        return val.toString();
    }

    public static Double parseDouble(String str) {
        if(null == str || StringUtils.isBlank(str) || "null".equalsIgnoreCase(str)) {
            return null;
        }
        return Double.parseDouble(str);
    }

    /**
     * 解析字符串为Long类型
     * 支持处理Excel中数字格式可能返回的带小数点格式（如"2872359474906836.0"）
     * @param str 待解析的字符串
     * @return Long值，如果字符串为空或null则返回null
     */
    public static Long parseLong(String str) {
        if(null == str || StringUtils.isBlank(str) || "null".equalsIgnoreCase(str)) {
            return null;
        }
        // 先转double再转long，处理Excel中可能返回的带小数点格式
        try {
            double doubleValue = Double.parseDouble(str.trim());
            return (long) doubleValue;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
