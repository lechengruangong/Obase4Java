package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务接口
 */
public interface IServiceSO {

    /**
     * 获取创建时间
     *
     * @return 创建时间
     */
    LocalDateTime createTime();
}
