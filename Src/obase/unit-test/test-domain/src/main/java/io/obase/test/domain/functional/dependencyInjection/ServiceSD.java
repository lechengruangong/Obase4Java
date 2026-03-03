package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务D
 */
public class ServiceSD implements IServiceS {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime = LocalDateTime.now();

    /**
     * 服务的Code
     *
     * @return ServiceD
     */
    @Override
    public String getCode() {
        return "ServiceD";
    }

    /**
     * 创建时间
     *
     * @return 创建时间
     */
    @Override
    public LocalDateTime getCreateTime() {
        return this.createTime;
    }
}
