package tools;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
public class DateUtils {

    public static String buildReportDateString(int days) {
        LocalDate dateDaysAgo = LocalDate.now().minusDays(days);
        LocalDate today = LocalDate.now();
        return String.format("%s - %s", dateDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    public static String buildReportDateString(int fromDays, int toDays) {
        LocalDate dateDaysFrom = LocalDate.now().minusDays(fromDays);
        LocalDate dateDaysTo = LocalDate.now().minusDays(toDays);
        return String.format("%s - %s", dateDaysFrom.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), dateDaysTo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }
}
