package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务A
 */
public class ServiceTA implements IServiceT {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务A
     */
    public ServiceTA() {
        this.createTime = LocalDateTime.now();
    }

    /**
     * 服务的Code
     *
     * @return ServiceA
     */
    @Override
    public String getCode() {
        return "ServiceA";
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
