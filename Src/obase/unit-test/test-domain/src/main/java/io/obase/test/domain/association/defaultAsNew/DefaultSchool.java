package io.obase.test.domain.association.defaultAsNew;

/**
 * 测试关联端默认是否新建对象学校
 */
public class DefaultSchool {

    /**
     * 学校名称
     */
    private String name;

    /**
     * 学校ID
     */
    private long schoolId;

    /**
     * 获取学校名称
     *
     * @return 学校名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * 设置学校名称
     *
     * @param name 学校名称
     */
    public void setName(String name) {
        this.name = name;
    }

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
}
