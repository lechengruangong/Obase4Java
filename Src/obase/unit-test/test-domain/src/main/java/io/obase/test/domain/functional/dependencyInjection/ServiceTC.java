package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务C
 */
public class ServiceTC implements IServiceT {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务C
     */
    public ServiceTC() {
        this.createTime = LocalDateTime.of(1999, 12, 31, 0, 0, 0);
    }

    /**
     * 服务的Code
     *
     * @return ServiceC
     */
    @Override
    public String getCode() {
        return "ServiceC";
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
