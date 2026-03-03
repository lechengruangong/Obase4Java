package io.obase.test.domain.functional.dependencyInjection;

import java.time.LocalDateTime;

/**
 * 服务G
 */
public class ServiceSG implements IServiceS {

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 服务G
     *
     * @param dateTime 创建时间
     */
    public ServiceSG(LocalDateTime dateTime) {
        this.createTime = dateTime;
    }

    /**
     * 服务的Code
     *
     * @return ServiceG
     */
    @Override
    public String getCode() {
        return "ServiceG";
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
