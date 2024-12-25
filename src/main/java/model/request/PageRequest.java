package model.request;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/2
 */
@Data
public class PageRequest {

    private Integer page;

    private Integer start;

    private Integer length;
}
