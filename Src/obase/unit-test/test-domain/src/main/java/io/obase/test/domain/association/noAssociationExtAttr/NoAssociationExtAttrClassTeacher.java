package io.obase.test.domain.association.noAssociationExtAttr;

import java.util.ArrayList;
import java.util.List;

/**
 * 无关联冗余属性的班级任课教师
 */
public class NoAssociationExtAttrClassTeacher {

    /**
     * 班级
     */
    private NoAssociationExtAttrClass clazz;

    /**
     * 是否是班主任
     */
    private boolean isManage;

    /**
     * 是否是代课老师
     */
    private boolean isSubstitute;

    /**
     * 所授科目
     */
    private List<String> subject = new ArrayList<>();

    /**
     * 教师
     */
    private NoAssociationExtAttrTeacher teacher;

    /**
     * 获取班级
     *
     * @return 班级
     */
    public NoAssociationExtAttrClass getClazz() {
        return this.clazz;
    }

    /**
     * 设置班级
     *
     * @param clazz 班级
     */
    public void setClazz(NoAssociationExtAttrClass clazz) {
        this.clazz = clazz;
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

    /**
     * 获取是否是代课老师
     *
     * @return 是否是代课老师
     */
    public boolean getIsSubstitute() {
        return this.isSubstitute;
    }

    /**
     * 设置是否是代课老师
     *
     * @param substitute 是否是代课老师
     */
    public void setIsSubstitute(boolean substitute) {
        this.isSubstitute = substitute;
    }

    /**
     * 获取所授科目
     *
     * @return 所授科目
     */
    public List<String> getSubject() {
        return this.subject;
    }

    /**
     * 设置所授科目
     *
     * @param subject 所授科目
     */
    public void setSubject(List<String> subject) {
        this.subject = subject;
    }

    /**
     * 获取教师
     *
     * @return 教师
     */
    public NoAssociationExtAttrTeacher getTeacher() {
        return this.teacher;
    }

    /**
     * 设置教师
     *
     * @param teacher 教师
     */
    public void setTeacher(NoAssociationExtAttrTeacher teacher) {
        this.teacher = teacher;
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "NoAssociationExtAttrClassTeacher{" +
                "clazz=" + this.clazz +
                ", isManage=" + this.isManage +
                ", isSubstitute=" + this.isSubstitute +
                ", subject=" + this.subject +
                ", teacher=" + this.teacher +
                '}';
    }
}
