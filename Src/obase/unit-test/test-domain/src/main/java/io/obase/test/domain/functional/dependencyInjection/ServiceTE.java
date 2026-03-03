package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务E
 */
public class ServiceTE implements IServiceT {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务E
     *
     * @param serviceSD 服务D
     */
    public ServiceTE(ServiceSD serviceSD) {
        this.createTime = serviceSD.getCreateTime();
    }

    /**
     * 服务的Code
     *
     * @return ServiceE
     */
    @Override
    public String getCode() {
        return "ServiceE";
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
