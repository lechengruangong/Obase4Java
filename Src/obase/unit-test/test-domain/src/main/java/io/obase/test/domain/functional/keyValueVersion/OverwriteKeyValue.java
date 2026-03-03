package io.obase.test.domain.functional.keyValueVersion;

/**
 * 用于测试覆盖并发策略的简单属性类
 */
public class OverwriteKeyValue {
    /**
     * 唯一标识
     */
    private int id;

    /**
     * 键
     */
    private String key;

    /**
     * 值
     */
    private int value;

    /**
     * 版本键
     */
    private int versionKey;

    /**
     * 获取键
     *
     * @return 键
     */
    public String getKey() {
        return this.key;
    }

    /**
     * 设置键
     *
     * @param key 键
     */
    public void setKey(String key) {
        this.key = key;
    }

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
     * 获取值
     *
     * @return 值
     */
    public int getValue() {
        return this.value;
    }

    /**
     * 设置值
     *
     * @param value 值
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * 字符串表示形式
     *
     * @return 字符串
     */
    @Override
    public String toString() {
        return "KeyValueWithVersion{" +
                "id=" + this.id +
                ", key='" + this.key + '\'' +
                ", value=" + this.value +
                ", versionKey=" + this.versionKey +
                '}';
    }
}
