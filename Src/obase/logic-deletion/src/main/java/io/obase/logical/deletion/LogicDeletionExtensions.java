/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：逻辑删除工具类.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-15 11:11:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.logical.deletion;

import io.obase.core.ObjectContext;
import io.obase.core.ObjectSet;
import io.obase.core.common.Utils;
import io.obase.core.expression.SerializedPredicate;
import io.obase.core.odm.Attribute;
import io.obase.core.odm.StructuralType;

import java.util.HashMap;

/**
 * 逻辑删除工具类
 */
public class LogicDeletionExtensions {

    /**
     * 逻辑删除方法
     *
     * @param objectSet 对象集
     * @param obj       对象
     * @param <T>       对象类型
     */
    public static <T> void removeLogically(ObjectSet<T> objectSet, T obj) {
        setLogicalDeletion(objectSet, obj, true);
    }

    /**
     * 逻辑删除恢复方法
     *
     * @param objectSet 对象集
     * @param obj       对象
     * @param <T>       对象类型
     */
    public static <T> void recoveryLogically(ObjectSet<T> objectSet, T obj) {
        setLogicalDeletion(objectSet, obj, false);
    }

    /**
     * 设置对象的逻辑删除状态
     *
     * @param objectSet 对象集
     * @param obj       对象
     * @param value     值
     * @param <T>       对象类型
     */
    private static <T> void setLogicalDeletion(ObjectSet<T> objectSet, T obj, boolean value) {

        if (obj == null)
            throw new IllegalArgumentException("无法处理逻辑删除,传入的对象为空.");

        StructuralType structuralType = objectSet.getObjectContext().getModel().getObjectType(obj.getClass());
        if (structuralType == null)
            throw new IllegalArgumentException(obj.getClass() + "未注册.");
        LogicDeletionExtension ext = (LogicDeletionExtension) structuralType.getExtension(LogicDeletionExtension.class);
        if (ext == null)
            throw new IllegalArgumentException(obj.getClass() + "未进行逻辑删除配置");

        //如果此类型的DeletionMark没有配置(说明此类型是没有在类内定义属性的逻辑删除) 且 当前传入的对象类型是原始类型
        if (Utils.getStringIsEmpty(ext.getDeletionMark()) && obj.getClass() == structuralType.getClrType())
            //原始类型没有办法直接进行逻辑删除
            throw new IllegalArgumentException("无法处理" + obj.getClass() + "对象的逻辑删除,未定义逻辑删属性的情况仅能对上下文查出的对象使用RemoveLogically和RecoveryLogically,对于新对象请使用DeleteLogically和RecoveryLogically处理.");

        Attribute attr = (ext.getDeletionMark() == null || ext.getDeletionMark().isEmpty()) ? structuralType.getAttribute("Obase_gen_deletionMark") : structuralType.getAttribute(ext.getDeletionMark());

        try {
            attr.setValue(obj, value);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法将" + obj.getClass() + "逻辑删除,请检查此对象是否为上下文查出的对象", e);
        }
    }

    /**
     * 逻辑直接删除
     *
     * @param objectSet        对象集
     * @param filterExpression 筛选表达式
     * @param clazz            对象类型
     * @param <T>              对象类型
     * @return 受影响的行数
     */
    public static <T> int deleteLogically(ObjectSet<T> objectSet, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return setLogicalDeletion(objectSet, filterExpression, clazz, true);
    }

    /**
     * 逻辑直接删除恢复
     *
     * @param objectSet        对象集
     * @param filterExpression 筛选表达式
     * @param clazz            对象类型
     * @param <T>              对象类型
     * @return 受影响的行数
     */
    public static <T> int recoveryLogically(ObjectSet<T> objectSet, SerializedPredicate<T> filterExpression, Class<T> clazz) {
        return setLogicalDeletion(objectSet, filterExpression, clazz, false);
    }

    /**
     * 设置直接的逻辑删除状态
     *
     * @param objectSet        对象集
     * @param filterExpression 筛选表达式
     * @param clazz            对象类型
     * @param value            值
     * @param <T>              对象类型
     * @return 受影响的行数
     */
    private static <T> int setLogicalDeletion(ObjectSet<T> objectSet, SerializedPredicate<T> filterExpression, Class<T> clazz, boolean value) {
        StructuralType structuralType = objectSet.getObjectContext().getModel().getStructuralType(clazz);
        if (structuralType == null)
            throw new IllegalArgumentException(clazz + "未注册.");
        LogicDeletionExtension ext = (LogicDeletionExtension) structuralType.getExtension(LogicDeletionExtension.class);
        if (ext == null)
            throw new IllegalArgumentException(clazz + "未进行逻辑删除配置");

        String deletionField = (ext.getDeletionField() == null || ext.getDeletionField().isEmpty())
                ? structuralType.getAttribute(ext.getDeletionMark()).getTargetField()
                : ext.getDeletionField();
        HashMap<String, Object> map = new HashMap<>();
        map.put(deletionField, value);
        return objectSet.setAttributes(map, filterExpression, clazz);
    }

    /**
     * 启用逻辑删除
     *
     * @param context 上下文
     */
    public static void enableLogicDeletion(ObjectContext context) {
        context.registerModule(new LogicDeletionModule());
    }
}
