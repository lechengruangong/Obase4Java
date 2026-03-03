package io.obase.addon.test.domain.multi.tenant;

import io.obase.multi.tenant.MultiTenantAttribute;
import io.obase.odm.annotation.EntityAttribute;
import io.obase.odm.annotation.ImplicitAssociationAttribute;
import io.obase.odm.annotation.LeftEndMappingAttribute;
import io.obase.odm.annotation.RightEndMappingAttribute;

import java.util.UUID;

/**
 * 没定义多租户字段的教师 标注配置
 */
@EntityAttribute(tableName = "Teacher", keyAttributes = {"TeacherId"})
@MultiTenantAttribute(multiTenantField = "MultiTenantId", tenantIdType = UUID.class)
public class MultiTenantTeacherNoDefAnnotation {

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 所属学校
     */
    private MultiTenantSchoolNoDefAnnotation school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 教师ID
     */
    private long teacherId;

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
    @ImplicitAssociationAttribute(targetTableName = "Teacher", enableLazyLoading = true)
    @LeftEndMappingAttribute(keyAttribute = "TeacherId", targetField = "TeacherId")
    @RightEndMappingAttribute(keyAttribute = "SchoolId", targetField = "SchoolId")
    public MultiTenantSchoolNoDefAnnotation getSchool() {
        return this.school;
    }

    /**
     * 设置所属学校
     *
     * @param school 所属学校
     */
    public void setSchool(MultiTenantSchoolNoDefAnnotation school) {
        this.school = school;
    }
}
