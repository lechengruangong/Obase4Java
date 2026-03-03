package io.obase.test.domain.association.defaultAsNew;

import java.util.List;

/**
 * 关联端默认不创建新对象的班级
 */
public class DefaultClass {

    /**
     * 班级id
     */
    private long classId;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 学生
     */
    private List<DefaultStudent> students;

    /**
     * 学校
     */
    private DefaultSchool school;

    /**
     * 班级任课老师
     */
    private List<DefaultClassTeacher> classTeachers;

    /**
     * 获取班级id
     *
     * @return 班级id
     */
    public long getClassId() {
        return this.classId;
    }

    /**
     * 设置班级id
     *
     * @param classId 班级id
     */
    public void setClassId(long classId) {
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
     * 获取学生
     *
     * @return 学生
     */
    public List<DefaultStudent> getStudents() {
        return this.students;
    }

    /**
     * 设置学生
     *
     * @param students 学生
     */
    public void setStudents(List<DefaultStudent> students) {
        this.students = students;
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
     * 获取学校
     *
     * @return 学校
     */
    public DefaultSchool getSchool() {
        return this.school;
    }

    /**
     * 设置学校
     *
     * @param school 学校
     */
    public void setSchool(DefaultSchool school) {
        this.school = school;
    }

    /**
     * 获取班级任课老师
     *
     * @return 班级任课老师
     */
    public List<DefaultClassTeacher> getClassTeachers() {
        return this.classTeachers;
    }

    /**
     * 设置班级任课老师
     *
     * @param classTeachers 班级任课老师
     */
    public void setClassTeachers(List<DefaultClassTeacher> classTeachers) {
        this.classTeachers = classTeachers;
    }
}
