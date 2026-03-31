/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化数据传输对象.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-31 10:52:16
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.util.HashMap;
import java.util.List;

/**
 * 序列化数据传输对象
 * 用于在序列化过程中传输数据
 */
public class SerializationDataTransferObject {
    /**
     * 序列化过程中分配的唯一ID
     * 从$0开始递增
     */
    private String id;

    /**
     * 存储要序列化的对象的类型名称
     */
    private String typeName;

    /**
     * 此Dto存储的对象是否为根对象
     * 根对象指的是宿主对象直接引用的对象
     */
    private boolean isRoot;

    /**
     * 存储属性的字典
     * 存储需要存储的属性的名称和对应的值
     * 默认将需要序列化的对象中的Obase基元类型的属性访问器视为属性
     */
    private HashMap<String, Object> attributes;

    /**
     * 存储构造函数参数的字典
     * 存储需要存储的构造函数参数的索引和对应的值
     * 索引值从0开始 与构造函数配置的构造器参数参数索引一一对应
     */
    private HashMap<String, Object> constructorParameters;

    /**
     * 存储引用的其他序列化对象的字典
     * 存储引用属性的名称和序列号ID的集合
     * 默认将需要序列化的对象中的其他已配置为序列化模型的属性访问器视为引用
     */
    private HashMap<String, List<String>> references;

    /**
     * 初始化序列化数据传输对象
     */
    public SerializationDataTransferObject() {
        this.attributes = new HashMap<>();
        this.constructorParameters = new HashMap<>();
        this.references = new HashMap<>();
    }

    /**
     * 获取序列化过程中分配的唯一ID
     * 从$0开始递增
     *
     * @return 唯一ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * 设置序列化过程中分配的唯一ID
     * 从$0开始递增
     *
     * @param id 唯一ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取存储要序列化的对象的类型名称
     *
     * @return 存储要序列化的对象的类型名称
     */
    public String getTypeName() {
        return this.typeName;
    }

    /**
     * 设置存储要序列化的对象的类型名称
     *
     * @param typeName 存储要序列化的对象的类型名称
     */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 获取此Dto存储的对象是否为根对象
     * 根对象指的是宿主对象直接引用的对象
     *
     * @return 此Dto存储的对象是否为根对象
     */
    public boolean getIsRoot() {
        return this.isRoot;
    }

    /**
     * 设置此Dto存储的对象是否为根对象
     * 根对象指的是宿主对象直接引用的对象
     *
     * @param root 此Dto存储的对象是否为根对象
     */
    public void setIsRoot(boolean root) {
        this.isRoot = root;
    }

    /**
     * 获取存储属性的字典
     * 存储需要存储的属性的名称和对应的值
     * 默认将需要序列化的对象中的Obase基元类型的属性访问器视为属性
     *
     * @return 存储属性的字典
     */
    public HashMap<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * 设置存储属性的字典
     * 存储需要存储的属性的名称和对应的值
     * 默认将需要序列化的对象中的Obase基元类型的属性访问器视为属性
     *
     * @param attributes 存储属性的字典
     */
    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 获取存储构造函数参数的字典
     * 存储需要存储的构造函数参数的索引和对应的值
     * 索引值从0开始 与构造函数配置的构造器参数参数索引一一对应
     *
     * @return 存储构造函数参数的字典
     */
    public HashMap<String, Object> getConstructorParameters() {
        return this.constructorParameters;
    }

    /**
     * 设置存储构造函数参数的字典
     * 存储需要存储的构造函数参数的索引和对应的值
     * 索引值从0开始 与构造函数配置的构造器参数参数索引一一对应
     *
     * @param constructorParameters 存储构造函数参数的字典
     */
    public void setConstructorParameters(HashMap<String, Object> constructorParameters) {
        this.constructorParameters = constructorParameters;
    }

    /**
     * 获取存储引用的其他序列化对象的字典
     * 存储引用属性的名称和序列号ID的集合
     * 默认将需要序列化的对象中的其他已配置为序列化模型的属性访问器视为引用
     *
     * @return 存储引用的其他序列化对象的字典
     */
    public HashMap<String, List<String>> getReferences() {
        return this.references;
    }

    /**
     * 设置存储引用的其他序列化对象的字典
     * 存储引用属性的名称和序列号ID的集合
     * 默认将需要序列化的对象中的其他已配置为序列化模型的属性访问器视为引用
     *
     * @param references 存储引用的其他序列化对象的字典
     */
    public void setReferences(HashMap<String, List<String>> references) {
        this.references = references;
    }
}
