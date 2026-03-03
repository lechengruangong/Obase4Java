package io.obase.test.domain.association.defaultAsNew;

/**
 * 测试关联端默认是否新建对象学生
 */
public class DefaultStudent {

    /**
     * 学生名称
     */
    private String name;

    /**
     * 学生id
     */
    private long studentId;

    /**
     * 班级ID
     */
    private long classId;

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
     * 获取学生ID
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
