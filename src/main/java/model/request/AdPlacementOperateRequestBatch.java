package model.request;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class AdPlacementOperateRequestBatch {

    private List<AdPlacementOperateRequestSingle> targetingClauses;

    private List<AdPlacementOperateRequestSingle> keywords;
}
