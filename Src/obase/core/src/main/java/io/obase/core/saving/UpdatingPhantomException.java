/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示“更新幻影”冲突的异常.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:15:58
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.odm.ObjectType;

/**
 * 表示“更新幻影”冲突的异常。
 */
public class UpdatingPhantomException extends ConcurrentConflictException {
    /**
     * 创建ConcurrentConflictException实例
     *
     * @param obj       发生并发冲突的对象
     * @param objType   发生并发冲突的对象的类型
     * @param exception 内部异常
     */
    public UpdatingPhantomException(Object obj, ObjectType objType, Exception exception) {
        super(obj, objType, exception);
    }

    /**
     * 返回异常消息
     *
     * @return 异常消息
     */
    @Override
    public String getMessage() {
        return String.format("发生了并发冲突，更新对象时发现目标对象已被删除，对象标识为[%s]", this.getObjectKey());
    }
}
