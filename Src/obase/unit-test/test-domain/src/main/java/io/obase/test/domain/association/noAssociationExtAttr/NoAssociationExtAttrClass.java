package io.obase.test.domain.association.noAssociationExtAttr;

import java.util.List;

/**
 * 无关联冗余属性的班级
 */
public class NoAssociationExtAttrClass {

    /**
     * 班级id
     */
    private long classId;

    /**
     * 班级任课老师
     */
    private List<NoAssociationExtAttrClassTeacher> classTeachers;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 学校
     */
    private NoAssociationExtAttrSchool school;

    /**
     * 学生
     */
    private List<NoAssociationExtAttrStudent> students;

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
     * 获取班级任课老师
     *
     * @return 班级任课老师
     */
    public List<NoAssociationExtAttrClassTeacher> getClassTeachers() {
        return this.classTeachers;
    }

    /**
     * 设置班级任课老师
     *
     * @param classTeachers 班级任课老师
     */
    public void setClassTeachers(List<NoAssociationExtAttrClassTeacher> classTeachers) {
        this.classTeachers = classTeachers;
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
     * 获取学校
     *
     * @return 学校
     */
    public NoAssociationExtAttrSchool getSchool() {
        return this.school;
    }

    /**
     * 设置学校
     *
     * @param school 学校
     */
    public void setSchool(NoAssociationExtAttrSchool school) {
        this.school = school;
    }

    /**
     * 获取学生
     *
     * @return 学生
     */
    public List<NoAssociationExtAttrStudent> getStudents() {
        return this.students;
    }

    /**
     * 设置学生
     *
     * @param students 学生
     */
    public void setStudents(List<NoAssociationExtAttrStudent> students) {
        this.students = students;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "NoAssociationExtAttrClass{" +
                "classId=" + this.classId +
                ", classTeachers=" + this.classTeachers +
                ", name='" + this.name + '\'' +
                ", school=" + this.school +
                ", students=" + this.students +
                '}';
    }
}
