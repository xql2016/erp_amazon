package model.request;

import lombok.Data;

import java.util.List;

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

    private List<Long> clicks; // 点击数

    private List<Long> orders; // 广告数

    private List<Long> acos; // acos
}
