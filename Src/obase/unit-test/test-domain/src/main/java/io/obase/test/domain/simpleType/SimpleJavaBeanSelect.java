package io.obase.test.domain.simpleType;

/**
 * 简单JAVABEAN类 符合Json命名标准
 */
public class SimpleJavaBeanSelect {

    /**
     * int类型数字
     */
    private final int intNumber;

    /**
     * 布尔值类型
     */
    private final boolean bool;

    /**
     * 构造简单JAVABEAN类
     *
     * @param b         布尔值
     * @param intNumber Int
     */
    public SimpleJavaBeanSelect(boolean b, int intNumber) {
        this.bool = b;
        this.intNumber = intNumber;
    }

    /**
     * 获取Int值
     *
     * @return Int值
     */
    public int getIntNumber() {
        return this.intNumber;
    }

    /**
     * 获取布尔值
     *
     * @return 布尔值
     */
    public boolean getBool() {
        return this.bool;
    }
}
