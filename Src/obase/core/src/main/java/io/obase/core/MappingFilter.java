/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：映射筛选器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:34:41
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ActionWithOneArg;
import io.obase.common.ActionWithTwoArg;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示映射筛选器。
 * 映射筛选器用于从存储源选择对象，工作流仅作用于被选中的对象。
 */
public class MappingFilter {

    /**
     * 一个委托，代表映射筛选器制作完成时回调的方法
     */
    private final ActionWithOneArg<ELogicalOperator> filterReady;

    /**
     * 指示当前筛选器与已存在的筛选器执行逻辑“与”还是“或”运算。
     */
    private final ELogicalOperator logicOperator;

    /**
     * 一个委托，代表映射筛选器片段制作完成时回调的方法。
     * 该方法的第一个参数表示筛选器的依据域，第二个字段表示参考值。
     */
    private final ActionWithTwoArg<String, Object> segmentReady;

    /**
     * 适用筛选器的工作流
     */
    private final IMappingWorkflow workflow;

    /**
     * 映射筛选器片段
     */
    private List<FilterSegment> filterSegments;

    /**
     * 创建MappingFilter实例
     *
     * @param workflow      适用筛选器的工作流
     * @param logicOperator 指示当前筛选器与已存在的筛选器执行逻辑
     * @param filterReady   一个委托，代表映射筛选器制作完成时回调的方法
     * @param segmentReady  一个委托，代表映射筛选器片段制作完成时回调的方法
     */
    public MappingFilter(IMappingWorkflow workflow, ELogicalOperator logicOperator,
                         ActionWithOneArg<ELogicalOperator> filterReady, ActionWithTwoArg<String, Object> segmentReady) {
        this.workflow = workflow;
        this.logicOperator = logicOperator;
        this.filterReady = filterReady;
        this.segmentReady = segmentReady;
    }

    /**
     * 获取映射筛选器片段
     *
     * @return 映射筛选器片段
     */
    public List<FilterSegment> getFilterSegments() {
        return this.filterSegments;
    }

    /**
     * 在映射筛选器中追加一个片段
     *
     * @return 新增的筛选器片段
     */
    public FilterSegment addSegment() {
        if (this.filterSegments == null)
            this.filterSegments = new ArrayList<>();

        //构造一个新的片段
        FilterSegment filter = new FilterSegment(this, this.segmentReady);
        this.filterSegments.add(filter);

        return filter;
    }

    /**
     * 通知映射筛选器制作过程已完成
     *
     * @return 适用当前筛选器的映射工作流
     */
    public IMappingWorkflow end() {
        this.filterReady.invoke(this.logicOperator);
        return this.workflow;
    }
}

