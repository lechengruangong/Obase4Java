/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：枚举查询运算名称.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-29 14:24:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.query;

/**
 * 枚举查询运算名称
 */
public enum EQueryOpName {

    /**
     * 累加运算
     */
    Accumulate(0),

    /**
     * 算术聚合运算
     */
    ArithAggregate(1),

    /**
     * All测定运算
     */
    All(2),

    /**
     * Any测定运算
     */
    Any(3),

    /**
     * 类型转换运算
     */
    Cast(4),

    /**
     * Contains测定运算
     */
    Contains(5),

    /**
     * 计数运算
     */
    Count(6),

    /**
     * 取默认值运算
     */
    DefaultIfEmpty(7),

    /**
     * 去重运算
     */
    Distinct(8),

    /**
     * 索引运算
     */
    ElementAt(9),

    /**
     * First索引运算
     */
    First(10),

    /**
     * 分组运算
     */
    Group(11),

    /**
     * 包含运算
     */
    Include(12),

    /**
     * 联接运算
     */
    Join(13),

    /**
     * Last索引运算
     */
    Last(14),

    /**
     * 类型筛选运算
     */
    OfType(15),

    /**
     * 排序运算
     */
    Order(16),

    /**
     * 反序运算
     */
    Reverse(17),

    /**
     * 投影运算
     */
    Select(18),

    /**
     * 顺序相等比较运算
     */
    SequenceEqual(19),

    /**
     * 集运算
     */
    Set(20),

    /**
     * 单值索引运算
     */
    Single(21),

    /**
     * 略过运算
     */
    Skip(22),

    /**
     * 条件略过运算
     */
    SkipWhile(23),

    /**
     * 提取运算
     */
    Take(24),

    /**
     * 条件提取运算
     */
    TakeWhile(25),

    /**
     * 筛选运算
     */
    Where(26),

    /**
     * 合并运算
     */
    Zip(27),

    /**
     * 无参数 全查询
     */
    Non(28);

    /**
     * 运算名称
     */
    private final int name;

    /**
     * 枚举查询运算名称
     *
     * @param name 名称
     */
    EQueryOpName(int name) {
        this.name = name;
    }

    /**
     * 获取运算名称
     *
     * @return 名称
     */
    public int getName() {
        return this.name;
    }
}
