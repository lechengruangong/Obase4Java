package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务H
 *
 * @param createTime 创建时间
 */
public record ServiceSH(LocalDateTime createTime) implements IServiceSO {

    /**
     * 初始化服务H
     *
     * @param createTime 创建时间
     */
    public ServiceSH {
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
