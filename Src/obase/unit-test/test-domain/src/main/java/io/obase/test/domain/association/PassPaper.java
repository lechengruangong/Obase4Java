package io.obase.test.domain.association;

/**
 * 教师的通行证
 */
public class PassPaper {

    /**
     * 教师ID
     */
    private final long teacherId;

    /**
     * 通行证类型
     */
    private final EPassPaperType type;

    /**
     * 备注
     */
    private String memo;

    /**
     * 所属的教师
     */
    private Teacher teacher;

    /**
     * 构造函数
     *
     * @param teacherId 教师ID
     * @param type      通行证类型
     */
    public PassPaper(long teacherId, EPassPaperType type) {
        this.teacherId = teacherId;
        this.type = type;
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
     * 获取通行证类型
     *
     * @return 通行证类型
     */
    public EPassPaperType getType() {
        return this.type;
    }

    /**
     * 获取备注
     *
     * @return 备注
     */
    public String getMemo() {
        return this.memo;
    }

    /**
     * 设置备注
     *
     * @param memo 备注
     */
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 获取所属的教师
     *
     * @return 所属的教师
     */
    public Teacher getTeacher() {
        return this.teacher;
    }

    /**
     * 设置所属的教师
     *
     * @param teacher 所属的教师
     */
    void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }
}
