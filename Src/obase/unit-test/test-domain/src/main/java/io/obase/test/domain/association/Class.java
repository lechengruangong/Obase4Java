package io.obase.test.domain.association;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 班级
 */
public class Class {

    /**
     * 班级id
     */
    private long classId;

    /**
     * 班级任课老师
     */
    private List<ClassTeacher> classTeachers;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 学校
     */
    private School school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 学生
     */
    private List<Student> students;

    /**
     * 获取学校
     *
     * @return 学校
     */
    public School getSchool() {
        return this.school;
    }

    /**
     * 设置学校
     *
     * @param school 学校
     */
    public void setSchool(School school) {
        this.school = school;
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
     * 获取班级任课老师
     *
     * @return 班级任课老师
     */
    public List<ClassTeacher> getClassTeachers() {
        return this.classTeachers;
    }

    /**
     * 设置班级任课老师
     *
     * @param classTeachers 班级任课老师
     */
    public void setClassTeachers(List<ClassTeacher> classTeachers) {
        this.classTeachers = classTeachers;
    }

    /**
     * 获取学生
     *
     * @return 学生
     */
    public List<Student> getStudents() {
        return this.students;
    }

    /**
     * 设置学生
     *
     * @param students 学生
     */
    void setStudents(List<Student> students) {
        this.students = students;
    }

    /**
     * 获取教师
     *
     * @return 教师
     */
    public List<Teacher> getTeachers() {
        return this.classTeachers == null ? null : this.classTeachers.stream().map(ClassTeacher::getTeacher).collect(Collectors.toList());
    }
}
