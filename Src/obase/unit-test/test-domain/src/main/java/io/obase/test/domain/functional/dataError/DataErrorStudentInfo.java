package io.obase.test.domain.functional.dataError;

/**
 * 数据错误的学生信息测试类
 * 用于测试引用是一对一但实际数据确是一对多的情况
 */
public class DataErrorStudentInfo {

    /**
     * 学生信息Id
     */
    private long studentInfoId;

    /**
     * 学生背景
     */
    private String background;

    /**
     * 学生详细描述
     */
    private String description;

    /**
     * 学生id
     */
    private long studentId;

    /**
     * 获取学生信息Id
     *
     * @return 学生信息Id
     */
    public long getStudentInfoId() {
        return this.studentInfoId;
    }

    /**
     * 设置学生信息Id
     *
     * @param studentInfoId 学生信息Id
     */
    public void setStudentInfoId(long studentInfoId) {
        this.studentInfoId = studentInfoId;
    }

    /**
     * 获取学生背景
     *
     * @return 学生背景
     */
    public String getBackground() {
        return this.background;
    }

    /**
     * 设置学生背景
     *
     * @param background 学生背景
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * 获取学生详细描述
     *
     * @return 学生详细描述
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * 设置学生详细描述
     *
     * @param description 学生详细描述
     */
    public void setDescription(String description) {
        this.description = description;
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
}
