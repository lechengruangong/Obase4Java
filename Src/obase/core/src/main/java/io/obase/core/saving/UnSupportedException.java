/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示“数据源不支持”的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-17 17:28:00
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectType;

/**
 * 表示“数据源不支持”的异常
 */
public class UnSupportedException extends ConcurrentConflictException {

    /**
     * 内部异常
     */
    private final RepeatInsertionException repeatInsertionException;

    /**
     * 创建ConcurrentConflictException实例
     *
     * @param obj     发生并发冲突的对象
     * @param objType 发生并发冲突的对象的类型
     */
    public UnSupportedException(Object obj, ObjectType objType, RepeatInsertionException repeatInsertionException) {
        super(obj, objType, repeatInsertionException);
        this.repeatInsertionException = repeatInsertionException;
    }

    @Override
    public String getMessage() {
        return "不支持当前" + this.getObjectType().getName() + "的并发冲突策略-" + this.getObjectType().getConcurrentConflictHandlingStrategy() + ":" + this.repeatInsertionException.getUnSupportMessage();
    }

    /**
     * 获取内部异常
     *
     * @return 内部异常
     */
    public RepeatInsertionException getRepeatInsertionException() {
        return this.repeatInsertionException;
    }
}
