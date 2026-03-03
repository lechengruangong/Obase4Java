package io.obase.test.domain.functional.dataError;

/**
 * 数据错误的学生测试类
 * 用于测试引用是一对一但实际数据确是一对多的情况
 */
public class DataErrorStudent {

    /**
     * 学生名称
     */
    private String name;

    /**
     * 学生id
     */
    private long studentId;

    /**
     * 学生详细信息
     */
    private DataErrorStudentInfo studentInfo;

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
    public DataErrorStudentInfo getStudentInfo() {
        return this.studentInfo;
    }

    /**
     * 设置学生详细信息
     *
     * @param studentInfo 学生详细信息
     */
    public void setStudentInfo(DataErrorStudentInfo studentInfo) {
        this.studentInfo = studentInfo;
    }
}
