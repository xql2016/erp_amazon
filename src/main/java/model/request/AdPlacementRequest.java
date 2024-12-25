package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/29
 */
@Data
public class AdPlacementRequest extends PageRequest {

    private String report_date;

    private long profile_id;

    private long campaign_id;
}
