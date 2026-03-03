package io.obase.test.domain.functional.complexKeyValueWithVersion;

/**
 * 覆盖合并的复杂属性
 */
public class OverWriteCombineComplexKeyValue {
    /**
     * 键
     */
    private String key;

    /**
     * 值
     */
    private int value;

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
}
