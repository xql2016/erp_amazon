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
}
