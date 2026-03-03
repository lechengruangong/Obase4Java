/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：文本序列化程序接口,为文本序列化程序定义调用规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:34:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

/**
 * 文本序列化程序接口，为文本序列化程序定义调用规范。
 * 文本序列化是将对象表示成文本形式，如Json序列化、Xml序列化。
 */
public interface ITextSerializer extends ISerializer {

    /**
     * 对给定的文本（以UTF-8编码）实施反序列化，以重建对象（图）。
     *
     * @param serializationText 序列化文本。
     * @param objType           要反序列化的对象的类型。
     * @return 重建对象
     */
    Object deserialize(String serializationText, Class<?> objType);

    /**
     * 对指定的对象或以该对象为根的对象图实施文本序列化（以UTF-8编码）。
     *
     * @param obj 要序列化的对象
     * @return 文本序列化
     */
    String serialize(Object obj);
}
