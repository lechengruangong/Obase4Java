/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：定义配置关联引用的规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-25 16:03:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.builder;

import io.obase.core.odm.EAggregationLevel;

/**
 * 定义配置关联引用的规范
 */
public interface IAssociationReferenceConfigurator extends IReferenceElementConfigurator {

    /**
     * 设置聚合级别(覆盖现有配置)
     *
     * @param level 级别
     */
    void hasAggregationLevelI(EAggregationLevel level);

    /**
     * 设置聚合级别
     *
     * @param level    级别
     * @param override 是否覆盖既有配置
     */
    void hasAggregationLevelI(EAggregationLevel level, boolean override);

    /**
     * 设置左端名(覆盖现有配置)
     *
     * @param leftEnd 左端名
     */
    void hasLeftEndI(String leftEnd);

    /**
     * 设置左端名
     *
     * @param leftEnd  左端名
     * @param override 是否覆盖既有配置
     */
    void hasLeftEndI(String leftEnd, boolean override);

    /**
     * 设置右端名(覆盖现有配置)
     *
     * @param rightEnd 右端名
     */
    void hasRightEndI(String rightEnd);

    /**
     * 设置右端名
     *
     * @param rightEnd 右端名
     * @param override 是否覆盖既有配置
     */
    void hasRightEndI(String rightEnd, boolean override);
}
