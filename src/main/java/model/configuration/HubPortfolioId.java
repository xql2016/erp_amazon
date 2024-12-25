package model.configuration;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/23
 */
@Data
public class HubPortfolioId {

    private Long hubId;

    private List<Long> portfolioIdList;
}
