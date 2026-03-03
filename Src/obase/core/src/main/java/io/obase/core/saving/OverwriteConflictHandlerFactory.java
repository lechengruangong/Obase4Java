/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：强制覆盖冲突处理器工厂.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:18:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

/**
 * 冲突处理器工厂，该工厂创建的处理器用于执行“强制覆盖”策略
 */
public class OverwriteConflictHandlerFactory extends ConcurrentConflictHandlerFactory {
    /**
     * 创建“重复创建”冲突的处理器
     *
     * @return “重复创建”冲突的处理器
     */
    @Override
    public IRepeatCreationHandler createRepeatCreationHandler() {
        return new OverwriteConflictHandler(this.getModel(), this.getStorageProvider(), this.getAttributeHasChanged());
    }

    /**
     * 创建版本冲突的处理器
     *
     * @return 版本冲突的处理器
     */
    @Override
    public IVersionConflictHandler createVersionConflictHandler() {
        return new OverwriteConflictHandler(this.getModel(), this.getStorageProvider(), this.getAttributeHasChanged());
    }

    /**
     * 创建“更新幻影”冲突的处理器
     *
     * @return “更新幻影”冲突的处理器
     */
    @Override
    public IUpdatingPhantomHandler createUpdatingPhantomHandler() {
        throw new IllegalArgumentException("发生“更新幻影”冲突时不能适用“强制覆盖”处理策略");
    }
}
