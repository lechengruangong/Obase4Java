package io.obase.test.domain.functional;

/**
 * 用于通知的学生信息
 */
public class NoticeStudentInfo {

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
     * 获取学生id
     *
     * @return 学生ID
     */
    public long getStudentId() {
        return this.studentId;
    }

    /**
     * 设置学生ID
     *
     * @param studentId 学生ID
     */
    public void setStudentId(long studentId) {
        this.studentId = studentId;
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
     * 重写转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "StudentInfo:{{StudentId-" + this.studentId + ",Description-\"" + this.description + "\",Background-\"" + this.background + "\"}}";
    }
}
