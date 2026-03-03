package io.obase.test.domain.association;

import java.util.ArrayList;
import java.util.List;

/**
 * 班级任课教师
 */
public class ClassTeacher {

    /**
     * 班级
     */
    private Class clazz;

    /**
     * 班级ID
     */
    private long classId;

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
    private Teacher teacher;

    /**
     * 教师ID
     */
    private long teacherId;

    /**
     * 普通构造函数
     *
     * @param clazz   班级
     * @param teacher 教师
     */
    public ClassTeacher(Class clazz, Teacher teacher) {
        this.clazz = clazz;
        this.teacher = teacher;
        this.classId = clazz.getClassId();
        this.teacherId = teacher.getTeacherId();
    }

    /**
     * 新实例构造函数
     *
     * @param classId      班级ID
     * @param teacherId    教师ID
     * @param isManage     是否为班主任
     * @param isSubstitute 是否为代课
     * @param subject      教授科目
     */
    public ClassTeacher(long classId, long teacherId, boolean isManage, boolean isSubstitute, List<String> subject) {
        this.classId = classId;
        this.teacherId = teacherId;
        this.isManage = isManage;
        this.isSubstitute = isSubstitute;
        this.subject = subject;
    }

    /**
     * 反序列化构造函数
     *
     * @param classId   班级ID
     * @param teacherId 教师ID
     */
    protected ClassTeacher(long classId, long teacherId) {
        this.classId = classId;
        this.teacherId = teacherId;
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
     * 设置师ID
     *
     * @param teacherId 教师ID
     */
    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    /**
     * 获取班级
     *
     * @return 班级
     */
    public Class getClazz() {
        return this.clazz;
    }

    /**
     * 设置班级
     *
     * @param clazz 班级
     */
    public void setClazz(Class clazz) {
        this.clazz = clazz;
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
     * 获取班级ID
     *
     * @return 班级ID
     */
    public long getClassId() {
        return this.classId > 0 ? this.classId : this.clazz != null ? this.clazz.getClassId() : 0;
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
     * 获取教师
     *
     * @return 教师
     */
    public Teacher getTeacher() {
        return this.teacher;
    }

    /**
     * 设置教师
     *
     * @param teacher 教师
     */
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "ClassTeacher{" +
                "clazz=" + this.clazz +
                ", classId=" + this.classId +
                ", isManage=" + this.isManage +
                ", isSubstitute=" + this.isSubstitute +
                ", subject=" + this.subject +
                ", teacher=" + this.teacher +
                ", teacherId=" + this.teacherId +
                '}';
    }
}
