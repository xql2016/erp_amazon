package model.response;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/7
 */
@Data
public class OperateResult {

    private int code;

    private String message;

    private List<OperateAdPlacementResult> result;
}
