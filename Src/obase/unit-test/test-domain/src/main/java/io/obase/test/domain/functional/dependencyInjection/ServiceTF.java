package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务F
 */
public class ServiceTF implements IServiceS {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务F
     *
     * @param dateTime 创建时间
     */
    public ServiceTF(LocalDateTime dateTime) {
        this.createTime = dateTime;
    }

    /**
     * 服务的Code
     *
     * @return ServiceF
     */
    @Override
    public String getCode() {
        return "ServiceF";
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