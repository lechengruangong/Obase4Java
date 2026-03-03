package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.EntityAttribute;
import io.obase.odm.annotation.ImplicitAssociationAttribute;
import io.obase.odm.annotation.LeftEndMappingAttribute;
import io.obase.odm.annotation.RightEndMappingAttribute;

/**
 * 标注建模测试用学生
 */
@EntityAttribute(keyAttributes = {"StudentId"})
public class AnnotationStudent {

    /**
     * 就读班级
     */
    private AnnotationClass clazz;

    /**
     * 班级ID
     */
    private long classId;

    /**
     * 学校
     */
    private AnnotationSchool school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 学生ID
     */
    private long studentId;

    /**
     * 学生名称
     */
    private String name;

    /**
     * 获取学生ID
     *
     * @return 学生ID
     */
    public long getStudentId() {
        return this.studentId;
    }

    /**
     * 设置获取学生ID
     *
     * @param studentId 获取学生ID
     */
    void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    /**
     * 获取班级ID
     *
     * @return 班级ID
     */
    public long getClassId() {
        return this.classId;
    }

    /**
     * 设置班级ID
     *
     * @param classId 班级ID
     */
    public void setClassId(long classId) {
        this.classId = classId;
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
     * 获取学生名称
     *
     * @return 学生名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置学生名称
     *
     * @param name 学生名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取班级
     *
     * @return 班级
     */
    @ImplicitAssociationAttribute(targetTableName = "AnnotationStudent", enableLazyLoading = true)
    @LeftEndMappingAttribute(keyAttribute = "StudentId", targetField = "StudentId")
    @RightEndMappingAttribute(keyAttribute = "ClassId", targetField = "ClassId")
    public AnnotationClass getClazz() {
        return this.clazz;
    }

    /**
     * 设置班级
     *
     * @param clazz 班级
     */
    public void setClazz(AnnotationClass clazz) {
        this.clazz = clazz;
    }

    /**
     * 获取学校
     *
     * @return 学校
     */
    @ImplicitAssociationAttribute(targetTableName = "AnnotationStudent", enableLazyLoading = true)
    @LeftEndMappingAttribute(keyAttribute = "StudentId", targetField = "StudentId")
    @RightEndMappingAttribute(keyAttribute = "SchoolId", targetField = "SchoolId")
    public AnnotationSchool getSchool() {
        return this.school;
    }

    /**
     * 设置学校
     *
     * @param school 学校
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
        return "AnnotationStudent{" +
                "clazz=" + this.clazz +
                ", classId=" + this.classId +
                ", schoolId=" + this.schoolId +
                ", studentId=" + this.studentId +
                ", name='" + this.name + '\'' +
                '}';
    }
}
