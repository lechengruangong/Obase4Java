package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.EntityAttribute;
import io.obase.odm.annotation.ImplicitAssociationAttribute;
import io.obase.odm.annotation.LeftEndMappingAttribute;
import io.obase.odm.annotation.RightEndMappingAttribute;

/**
 * 标注建模测试用教师
 */
@EntityAttribute(keyAttributes = {"TeacherId"})
public class AnnotationTeacher {

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 所属学校
     */
    private AnnotationSchool school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 教师ID
     */
    private long teacherId;

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
    void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    /**
     * 获取所属学校
     *
     * @return 所属学校
     */
    @ImplicitAssociationAttribute(targetTableName = "AnnotationTeacher", enableLazyLoading = true)
    @LeftEndMappingAttribute(keyAttribute = "TeacherId", targetField = "TeacherId")
    @RightEndMappingAttribute(keyAttribute = "SchoolId", targetField = "SchoolId")
    public AnnotationSchool getSchool() {
        return this.school;
    }

    /**
     * 设置所属学校
     *
     * @param school 所属学校
     */
    public void setSchool(AnnotationSchool school) {
        this.school = school;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AnnotationTeacher{" +
                "name='" + this.name + '\'' +
                ", schoolId=" + this.schoolId +
                ", teacherId=" + this.teacherId +
                '}';
    }
}
