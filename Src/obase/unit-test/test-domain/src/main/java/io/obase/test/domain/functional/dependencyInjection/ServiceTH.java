package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务H
 *
 * @param createTime 创建时间
 */
public record ServiceTH(LocalDateTime createTime) implements IServiceTO {

    /**
     * 初始化服务H
     *
     * @param createTime 创建时间
     */
    public ServiceTH {
    }

    /**
     * 创建时间
     *
     * @return 创建时间
     */
    @Override
    public LocalDateTime createTime() {
        return this.createTime;
    }
}
