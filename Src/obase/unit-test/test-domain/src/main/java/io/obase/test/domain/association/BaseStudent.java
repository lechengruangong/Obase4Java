package io.obase.test.domain.association;

/**
 * 学生基类
 */
public abstract class BaseStudent {

    /**
     * 学生id
     */
    private long studentId;

    /**
     * 学生名称
     */
    private String name;

    /**
     * 学生详细信息
     */
    private StudentInfo studentInfo;

    /**
     * 获取学生id
     *
     * @return 学生id
     */
    public long getStudentId() {
        return this.studentId;
    }

    /**
     * 设置学生id
     *
     * @param studentId 学生id
     */
    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    /**
     * 获取学生详细信息
     *
     * @return 学生详细信息
     */
    public StudentInfo getStudentInfo() {
        return this.studentInfo;
    }

    /**
     * 设置学生详细信息
     *
     * @param studentInfo 学生详细信息
     */
    public void setStudentInfo(StudentInfo studentInfo) {
        this.studentInfo = studentInfo;
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
}
