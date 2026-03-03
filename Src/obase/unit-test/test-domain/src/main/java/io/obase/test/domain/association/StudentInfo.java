package io.obase.test.domain.association;

/**
 * 学生详细信息
 */
public class StudentInfo {

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
     * 所归属的学生
     */
    private Student student;

    /**
     * 学生id
     */
    private long studentId;

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
     * 获取所归属的学生
     *
     * @return 所归属的学生
     */
    public Student getStudent() {
        return this.student;
    }

    /**
     * 设置所归属的学生
     *
     * @param student 所归属的学生
     */
    public void setStudent(Student student) {
        this.student = student;
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
     * 获取学生详细信息ID
     *
     * @return 学生详细信息ID
     */
    public long getStudentInfoId() {
        return this.studentInfoId;
    }

    /**
     * 设置学生详细信息ID
     *
     * @param studentInfoId 学生详细信息ID
     */
    public void setStudentInfoId(long studentInfoId) {
        this.studentInfoId = studentInfoId;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "StudentInfo{" +
                "background='" + this.background + '\'' +
                ", description='" + this.description + '\'' +
                ", student=" + this.student +
                ", studentId=" + this.studentId +
                '}';
    }
}
