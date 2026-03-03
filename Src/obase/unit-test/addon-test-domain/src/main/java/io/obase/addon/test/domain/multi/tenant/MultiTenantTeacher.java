package io.obase.addon.test.domain.multi.tenant;

import java.util.UUID;

/**
 * 定义了多租户字段的教师
 */
public class MultiTenantTeacher {

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 所属学校
     */
    private MultiTenantSchool school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 教师ID
     */
    private long teacherId;

    /**
     * 多租户ID
     */
    private UUID multiTenantId;

    /**
     * 获取教师ID
     *
     * @return 教师ID
     */
    public long getTeacherId() {
        return this.teacherId;
    }

    /**
     * 设置教师ID
     *
     * @param teacherId 教师ID
     */
    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

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
     * 获取教师姓名
     *
     * @return 教师姓名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置教师姓名
     *
     * @param name 教师姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取所属学校
     *
     * @return 所属学校
     */
    public MultiTenantSchool getSchool() {
        return this.school;
    }

    /**
     * 设置所属学校
     *
     * @param school 所属学校
     */
    public void setSchool(MultiTenantSchool school) {
        this.school = school;
    }

    /**
     * 获取多租户ID
     *
     * @return 多租户ID
     */
    public UUID getMultiTenantId() {
        return this.multiTenantId;
    }

    /**
     * 设置多租户ID
     *
     * @param multiTenantId 多租户ID
     */
    public void setMultiTenantId(UUID multiTenantId) {
        this.multiTenantId = multiTenantId;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "MultiTenantTeacher{" +
                "name='" + this.name + '\'' +
                ", schoolId=" + this.schoolId +
                ", teacherId=" + this.teacherId +
                ", multiTenantId=" + this.multiTenantId +
                '}';
    }
}
