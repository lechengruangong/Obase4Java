package io.obase.test.domain.association.noAssociationExtAttr;

/**
 * 无关联冗余属性的学生
 */
public class NoAssociationExtAttrStudent {

    /**
     * 就读班级
     */
    private NoAssociationExtAttrClass clazz;

    /**
     * 学生名称
     */
    private String name;

    /**
     * 学生id
     */
    private long studentId;

    /**
     * 获取就读班级
     *
     * @return 就读班级
     */
    public NoAssociationExtAttrClass getClazz() {
        return this.clazz;
    }

    /**
     * 设置就读班级
     *
     * @param clazz 就读班级
     */
    public void setClazz(NoAssociationExtAttrClass clazz) {
        this.clazz = clazz;
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
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "NoAssociationExtAttrStudent{" +
                "clazz=" + this.clazz +
                ", name='" + this.name + '\'' +
                ", studentId=" + this.studentId +
                '}';
    }
}
