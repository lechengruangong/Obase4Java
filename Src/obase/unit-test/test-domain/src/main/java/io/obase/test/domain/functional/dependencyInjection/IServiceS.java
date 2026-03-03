package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务接口
 */
public interface IServiceS {

    /**
     * 获取服务的Code
     *
     * @return 服务的Code
     */
    String getCode();

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    LocalDateTime getCreateTime();
}
