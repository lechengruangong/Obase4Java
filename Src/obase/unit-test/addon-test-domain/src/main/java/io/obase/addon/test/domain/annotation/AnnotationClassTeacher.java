package io.obase.addon.test.domain.annotation;

import io.obase.odm.annotation.AssociationAttribute;
import io.obase.odm.annotation.AssociationEndAttribute;
import io.obase.odm.annotation.EndMappingAttribute;

import java.util.ArrayList;
import java.util.List;

/**
 * 标注建模测试用班级任课教师
 */
@AssociationAttribute
public class AnnotationClassTeacher {

    /**
     * 班级
     */
    private AnnotationClass clazz;

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
    private AnnotationTeacher teacher;

    /**
     * 教师ID
     */
    private long teacherId;

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
     * 获取教师
     *
     * @return 教师
     */
    @AssociationEndAttribute
    @EndMappingAttribute(keyAttribute = "TeacherId", targetField = "TeacherId")
    public AnnotationTeacher getTeacher() {
        return this.teacher;
    }

    /**
     * 设置教师
     *
     * @param teacher 教师
     */
    public void setTeacher(AnnotationTeacher teacher) {
        this.teacher = teacher;
    }

    /**
     * 获取班级
     *
     * @return 班级
     */
    @AssociationEndAttribute
    @EndMappingAttribute(keyAttribute = "ClassId", targetField = "ClassId")
    public AnnotationClass getClazz() {
        return this.clazz;
    }

    /**
     * 设置班级
     *
     * @param clazz 班级
     */
    public void setClazz(AnnotationClass clazz) {
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
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AnnotationClassTeacher{" +
                ", classId=" + this.classId +
                ", isManage=" + this.isManage +
                ", isSubstitute=" + this.isSubstitute +
                ", subject=" + this.subject +
                ", teacherId=" + this.teacherId +
                '}';
    }
}
