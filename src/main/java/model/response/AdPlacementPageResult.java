package model.response;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/8/30
 */
@Data
public class AdPlacementPageResult extends PageResult{

    private List<AdPlacement> data;


}
