package io.obase.test.domain.functional.complexKeyValueWithVersion;

/**
 * 用于测试忽略并发策略的复杂属性类
 */
public class ComplexIgnoreKeyValue {

    /**
     * 唯一标识
     */
    private int id;

    /**
     * 版本键
     */
    private int versionKey;

    /**
     * 键值对
     */
    private ComplexKeyValue keyValue;

    /**
     * 获取唯一标识
     *
     * @return 唯一标识
     */
    public int getId() {
        return this.id;
    }

    /**
     * 设置唯一标识
     *
     * @param id 唯一标识
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 获取版本键
     *
     * @return 版本键
     */
    public int getVersionKey() {
        return this.versionKey;
    }

    /**
     * 设置版本键
     *
     * @param versionKey 版本键
     */
    public void setVersionKey(int versionKey) {
        this.versionKey = versionKey;
    }

    /**
     * 获取键值对
     *
     * @return 键值对
     */
    public ComplexKeyValue getKeyValue() {
        return this.keyValue;
    }

    /**
     * 设置键值对
     *
     * @param keyValue 键值对
     */
    public void setKeyValue(ComplexKeyValue keyValue) {
        this.keyValue = keyValue;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "ComplexIgnoreKeyValue{" +
                "id=" + this.id +
                ", versionKey=" + this.versionKey +
                '}';
    }
}
