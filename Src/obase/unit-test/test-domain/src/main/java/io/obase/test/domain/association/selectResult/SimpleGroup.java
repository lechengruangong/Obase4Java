package io.obase.test.domain.association.selectResult;

/**
 * 简单分组投影对象
 *
 * @param id  ID
 * @param agg 聚合结果
 */
public record SimpleGroup(long id, long agg) {

    /**
     * 初始化简单分组投影对象
     *
     * @param id  ID
     * @param agg 聚合结果
     */
    public SimpleGroup {
    }

    /**
     * 获取聚合结果
     *
     * @return 聚合结果
     */
    @Override
    public long agg() {
        return this.agg;
    }

    /**
     * 获取ID
     *
     * @return ID
     */
    @Override
    public long id() {
        return this.id;
    }
}
