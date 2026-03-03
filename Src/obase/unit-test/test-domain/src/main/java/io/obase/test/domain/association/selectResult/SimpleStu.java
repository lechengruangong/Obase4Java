package io.obase.test.domain.association.selectResult;

/**
 * 简单投影对象
 *
 * @param id   ID
 * @param name 名称
 */
public record SimpleStu(long id, String name) {

    /**
     * 简单投影对象
     *
     * @param id   ID
     * @param name 名称
     */
    public SimpleStu {
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

    /**
     * 获取名称
     *
     * @return 名称
     */
    @Override
    public String name() {
        return this.name;
    }
}
