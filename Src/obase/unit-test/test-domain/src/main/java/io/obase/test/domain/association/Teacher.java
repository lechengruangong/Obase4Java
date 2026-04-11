package io.obase.test.domain.association;

/**
 * 教师
 */
public class Teacher {

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 所属学校
     */
    private School school;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 教师ID
     */
    private long teacherId;

    /**
     * 所拥有的的通行证
     */
    private PassPaper[] passPaperList;

    /**
     * 获取学校ID
     *
     * @return 学校ID
     */
    public long getSchoolId() {
        return this.schoolId;
    }

    /**
     * 设置学校ID
     *
     * @param schoolId 学校ID
     */
    public void setSchoolId(long schoolId) {
        this.schoolId = schoolId;
    }

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
     * 获取所属学校
     *
     * @return 所属学校
     */
    public School getSchool() {
        return this.school;
    }

    /**
     * 设置所属学校
     *
     * @param school 所属学校
     */
    public void setSchool(School school) {
        this.school = school;
    }

    /**
     * 获取所拥有的的通行证
     *
     * @return 所拥有的的通行证
     */
    public PassPaper[] getPassPaperList() {
        return this.passPaperList;
    }

    /**
     * 设置所拥有的的通行证
     *
     * @param passPaperList 所拥有的的通行证
     */
    public void setPassPaperList(PassPaper[] passPaperList) {
        this.passPaperList = passPaperList;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + this.name + '\'' +
                ", school=" + this.school +
                ", schoolId=" + this.schoolId +
                ", teacherId=" + this.teacherId +
                '}';
    }
}
