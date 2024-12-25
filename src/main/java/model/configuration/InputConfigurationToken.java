package model.configuration;

import lombok.Data;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/9/6
 */
@Data
public class InputConfigurationToken {

    private String cookie;

    private String token;

    private boolean doSimulation;
}
