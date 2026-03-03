package io.obase.addon.test.domain.multi.tenant;

import io.obase.addon.test.domain.annotation.ESchoolType;

import java.time.LocalDateTime;

/**
 * 未定义多租户字段的学校
 */
public class MultiTenantSchoolNoDef {

    /**
     * 录入时间
     */
    private LocalDateTime createTime;

    /**
     * 办学时间
     */
    private LocalDateTime establishmentTime;

    /**
     * 是否为重点中学
     */
    private boolean isPrime;

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 学校类型
     */
    private ESchoolType schoolType;

    /**
     * 获取学校ID
     *
     * @return 学校ID
     */
    public long getSchoolId() {
        return this.schoolId;
    }

    /**
     * 设置学校ID
     *
     * @param schoolId 学校ID
     */
    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

    /**
     * 获取学校类型
     *
     * @return 学校类型
     */
    public ESchoolType getSchoolType() {
        return this.schoolType;
    }

    /**
     * 设置学校类型
     *
     * @param schoolType 学校类型
     */
    public void setSchoolType(ESchoolType schoolType) {
        this.schoolType = schoolType;
    }

    /**
     * 获取学校名称
     *
     * @return 学校名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置学校名称
     *
     * @param name 学校名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取录入时间
     *
     * @return 录入时间
     */
    public LocalDateTime getCreateTime() {
        return this.createTime;
    }

    /**
     * 设置录入时间
     *
     * @param createTime 录入时间
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取办学时间
     *
     * @return 办学时间
     */
    public LocalDateTime getEstablishmentTime() {
        return this.establishmentTime;
    }

    /**
     * 设置办学时间
     *
     * @param establishmentTime 办学时间
     */
    public void setEstablishmentTime(LocalDateTime establishmentTime) {
        this.establishmentTime = establishmentTime;
    }

    /**
     * 获取是否为重点中学
     *
     * @return 是否为重点中学
     */
    public boolean getIsPrime() {
        return this.isPrime;
    }

    /**
     * 设置是否为重点中学
     *
     * @param prime 是否为重点中学
     */
    public void setIsPrime(boolean prime) {
        this.isPrime = prime;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "MultiTenantSchoolNoDef{" +
                "createTime=" + this.createTime +
                ", establishmentTime=" + this.establishmentTime +
                ", isPrime=" + this.isPrime +
                ", name='" + this.name + '\'' +
                ", schoolId=" + this.schoolId +
                ", schoolType=" + this.schoolType +
                '}';
    }
}
