/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：重建对象冲突处理器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-30 15:02:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 冲突处理器工厂，该工厂创建的处理器用于执行“重建对象”策略。
 */
public class ReconstructConflictHandlerFactory extends ConcurrentConflictHandlerFactory {
    /**
     * 创建“重复创建”冲突的处理器
     *
     * @return “重复创建”冲突的处理器
     */
    @Override
    public IRepeatCreationHandler createRepeatCreationHandler() {
        throw new IllegalArgumentException("发生“重复创建”冲突时不能适用“重建对象”处理策略");
    }

    /**
     * 创建版本冲突的处理器
     *
     * @return 版本冲突的处理器
     */
    @Override
    public IVersionConflictHandler createVersionConflictHandler() {
        throw new NothingUpdatedException();
    }

    /**
     * 创建“更新幻影”冲突的处理器
     *
     * @return “更新幻影”冲突的处理器
     */
    @Override
    public IUpdatingPhantomHandler createUpdatingPhantomHandler() {
        return new ReconstructConflictHandler(this.getModel(), this.getStorageProvider());
    }
}