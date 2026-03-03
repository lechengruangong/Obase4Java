package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务B
 */
public class ServiceTB implements IServiceT {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务B
     */
    public ServiceTB() {
        this.createTime = LocalDateTime.now();
    }

    /**
     * 服务的Code
     *
     * @return ServiceB
     */
    @Override
    public String getCode() {
        return "ServiceB";
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
