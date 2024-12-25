package service;

import model.configuration.Configuration;

/**
 * @author <a href="mailto:qinglong.xql@cainiao.com">qinglong.xql</a>
 * @version 1.0
 * @since 2024/12/21
 */
public abstract class AbstractAction {

    protected abstract void execute(Configuration configuration) throws Exception;

    protected abstract String getCode();
}
