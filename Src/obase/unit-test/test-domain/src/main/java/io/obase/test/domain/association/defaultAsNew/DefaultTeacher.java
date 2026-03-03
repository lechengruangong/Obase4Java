package io.obase.test.domain.association.defaultAsNew;

/**
 * 测试关联端默认是否新建对象教师
 */
public class DefaultTeacher {

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 教师ID
     */
    private long teacherId;

    /**
     * 获取教师姓名
     *
     * @return 教师姓名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置教师姓名
     *
     * @param name 教师姓名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取教师ID
     *
     * @return 教师ID
     */
    public long getTeacherId() {
        return this.teacherId;
    }

    /**
     * 设置教师ID
     *
     * @param teacherId 教师ID
     */
    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }
}
