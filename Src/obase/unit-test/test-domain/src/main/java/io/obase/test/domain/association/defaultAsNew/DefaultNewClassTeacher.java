package io.obase.test.domain.association.defaultAsNew;

/**
 * 测试关联端默认新建对象任课教师
 */
public class DefaultNewClassTeacher {

    /**
     * 班级
     */
    private DefaultNewClass clazz;

    /**
     * 班级ID
     */
    private long classId;

    /**
     * 是否是班主任
     */
    private boolean isManage;

    /**
     * 教师
     */
    private DefaultTeacher teacher;

    /**
     * 教师ID
     */
    private long teacherId;

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
     * 获取班级
     *
     * @return 班级
     */
    public DefaultNewClass getClazz() {
        return this.clazz;
    }

    /**
     * 设置班级
     *
     * @param clazz 班级
     */
    public void setClazz(DefaultNewClass clazz) {
        this.clazz = clazz;
    }

    /**
     * 获取教师ID
     *
     * @return 教师ID
     */
    public long getTeacherId() {
        return this.teacherId > 0 ? this.teacherId : this.teacher != null ? this.teacher.getTeacherId() : 0;
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
     * 获取教师
     *
     * @return 教师
     */
    public DefaultTeacher getTeacher() {
        return this.teacher;
    }

    /**
     * 设置教师
     *
     * @param teacher 教师
     */
    public void setTeacher(DefaultTeacher teacher) {
        this.teacher = teacher;
    }

    /**
     * 获取是否是班主任
     *
     * @return 是否是班主任
     */
    public boolean getIsManage() {
        return this.isManage;
    }

    /**
     * 设置是否是班主任
     *
     * @param manage 是否是班主任
     */
    public void setIsManage(boolean manage) {
        this.isManage = manage;
    }
}
