package io.obase.test.domain.association;

/**
 * 学生
 */
public class Student extends BaseStudent {

    /**
     * 就读班级
     */
    private Class clazz;

    /**
     * 班级ID
     */
    private long classId;

    /**
     * 学校
     */
    private School school;

    /**
     * 学校ID
     */
    private long schoolId;

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
     * 获取就读班级
     *
     * @return 就读班级
     */
    public Class getClazz() {
        return this.clazz;
    }

    /**
     * 设置就读班级
     *
     * @param clazz 就读班级
     */
    public void setClazz(Class clazz) {
        this.clazz = clazz;
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
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "Student{" +
                "clazz=" + this.clazz +
                ", classId=" + this.classId +
                ", name='" + this.getName() + '\'' +
                ", school=" + this.school +
                ", schoolId=" + this.schoolId +
                ", studentId=" + this.getStudentId() +
                ", studentInfo=" + this.getStudentInfo() +
                '}';
    }
}
