/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：引发异常冲突处理器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:16:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 冲突处理器工厂，该工厂创建的处理器用于执行“引发异常”策略。
 */
public class ThrowExceptionConflictHandlerFactory extends ConcurrentConflictHandlerFactory {
    /**
     * 创建“重复创建”冲突的处理器
     *
     * @return “重复创建”冲突的处理器
     */
    @Override
    public IRepeatCreationHandler createRepeatCreationHandler() {
        return new ThrowExceptionConflictHandler(this.getModel(), this.getInnerException(), this.getAttributeOriginalValueGetter());
    }

    /**
     * 创建版本冲突的处理器
     *
     * @return 版本冲突的处理器
     */
    @Override
    public IVersionConflictHandler createVersionConflictHandler() {
        return new ThrowExceptionConflictHandler(this.getModel(), this.getInnerException(), this.getAttributeOriginalValueGetter());
    }

    /**
     * 创建“更新幻影”冲突的处理器
     *
     * @return “更新幻影”冲突的处理器
     */
    @Override
    public IUpdatingPhantomHandler createUpdatingPhantomHandler() {
        return new ThrowExceptionConflictHandler(this.getModel(), this.getInnerException(), this.getAttributeOriginalValueGetter());
    }
}
