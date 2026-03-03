package io.obase.test.domain.association.noAssociationExtAttr;

/**
 * 无关联冗余属性的教师
 */
public class NoAssociationExtAttrTeacher {

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

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "NoAssociationExtAttrTeacher{" +
                "name='" + this.name + '\'' +
                ", teacherId=" + this.teacherId +
                '}';
    }
}
