package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.*;

import java.util.List;

/**
 * 标注建模测试用班级
 */
@EntityAttribute(keyAttributes = {"ClassId"})
public class AnnotationClass {

    /**
     * 班级id
     */
    private long classId;

    /**
     * 班级任课老师
     */
    private List<AnnotationClassTeacher> classTeachers;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 学校
     */
    private AnnotationSchool school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 学生
     */
    private List<AnnotationStudent> students;

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
    void setClassId(long classId) {
        this.classId = classId;
    }

    /**
     * 获取班级名称
     *
     * @return 班级名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置班级名称
     *
     * @param name 班级名称
     */
    public void setName(String name) {
        this.name = name;
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
     * 获取班级任课老师
     *
     * @return 班级任课老师
     */
    @AssociationReferenceAttribute()
    public List<AnnotationClassTeacher> getClassTeachers() {
        return this.classTeachers;
    }

    /**
     * 设置班级任课老师
     *
     * @param classTeachers 班级任课老师
     */
    public void setClassTeachers(List<AnnotationClassTeacher> classTeachers) {
        this.classTeachers = classTeachers;
    }

    /**
     * 获取学校
     *
     * @return 学校
     */
    @ImplicitAssociationAttribute(targetTableName = "AnnotationClass")
    @LeftEndMappingAttribute(keyAttribute = "ClassId", targetField = "ClassId")
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
     * 获取学生
     *
     * @return 学生
     */
    @ImplicitAssociationAttribute(targetTableName = "AnnotationStudent", enableLazyLoading = true)
    @LeftEndMappingAttribute(keyAttribute = "ClassId", targetField = "ClassId")
    @RightEndMappingAttribute(keyAttribute = "StudentId", targetField = "StudentId")
    public List<AnnotationStudent> getStudents() {
        return this.students;
    }

    /**
     * 获取学生
     *
     * @param students 学生
     */
    public void setStudents(List<AnnotationStudent> students) {
        this.students = students;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AnnotationClass{" +
                "classId=" + this.classId +
                ", name='" + this.name + '\'' +
                ", schoolId=" + this.schoolId +
                '}';
    }
}
