package io.obase.test.domain.functional.serialization;

/**
 * 某种分析器
 */
public abstract class Analyser implements IComponent {

    /**
     * 组件名称
     */
    private String name;

    /**
     * 下一个分析器
     */
    private Analyser next;

    /**
     * 初始化分析器
     *
     * @param name 组件名称
     * @param next 下一个分析器
     */
    protected Analyser(String name, Analyser next) {
        this.name = name;
        this.next = next;
    }

    /**
     * 组件名称
     *
     * @return 组件名称
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 组件名称
     *
     * @param name 组件名称
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * 下一个分析器
     *
     * @return 下一个分析器
     */
    public Analyser getNext() {
        return this.next;
    }

    /**
     * 下一个分析器
     *
     * @param next 下一个分析器
     */
    void setNext(Analyser next) {
        this.next = next;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Analyser{" +
                "name='" + this.name + '\'' +
                ", next=" + this.next +
                '}';
    }
}
