package io.obase.test.domain.functional.serialization;

import java.util.List;

/**
 * 组件类 用于测试循环引用的序列化和反序列化
 */
public class Component implements IComponent {

    /**
     * 组件名称
     */
    private String name;


    /**
     * 组件
     */
    private List<Component> components;

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
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 组件
     *
     * @return 组件
     */
    public List<Component> getComponents() {
        return this.components;
    }

    /**
     * 组件
     *
     * @param components 组件
     */
    public void setComponents(List<Component> components) {
        this.components = components;
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "Component{" +
                "name='" + this.name + '\'' +
                '}';
    }
}
