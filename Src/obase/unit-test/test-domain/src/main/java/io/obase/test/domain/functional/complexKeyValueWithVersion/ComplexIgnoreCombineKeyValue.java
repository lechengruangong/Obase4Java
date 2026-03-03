package io.obase.test.domain.functional.complexKeyValueWithVersion;

/**
 * 忽略合并策略的简单类
 */
public class ComplexIgnoreCombineKeyValue {
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
    private IgnoreCombineComplexKeyValue keyValue;

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
    public IgnoreCombineComplexKeyValue getKeyValue() {
        return this.keyValue;
    }

    /**
     * 设置键值对
     *
     * @param keyValue 键值对
     */
    public void setKeyValue(IgnoreCombineComplexKeyValue keyValue) {
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
