/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：为文本序列化程序提供基础实现.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-24 15:32:21
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 为文本序列化程序提供基础实现
 */
public abstract class TextSerializer implements ITextSerializer {
    /**
     * 对给定的数据实施反序列化，以重建对象（图）。
     *
     * @param serializationStream 提供序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     * @param objType             反序列化的对象的类型
     * @return 对象
     */
    @Override
    public Object deserialize(InputStream serializationStream, Class<?> objType) {
        //读取流 调用DoDeserialize
        return this.doDeserialize(Utils.readUtf8StringFromInputStream(serializationStream), objType);
    }

    /**
     * 对指定的对象或以该对象为根的对象图实施序列化
     *
     * @param obj                 要序列化的对象
     * @param serializationStream 存储序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     */
    @Override
    public void serialize(Object obj, OutputStream serializationStream) {
        //调用DoSerialize 写入流
        Utils.writeUtf8StringToOutputStream(this.doSerialize(obj), serializationStream);
    }

    /**
     * 对给定的文本（以UTF-8编码）实施反序列化，以重建对象（图）。
     *
     * @param serializationText 序列化文本。
     * @param objType           要反序列化的对象的类型。
     * @return 重建对象
     */
    @Override
    public Object deserialize(String serializationText, Class<?> objType) {
        //调用DoDeserialize
        return this.doDeserialize(serializationText, objType);
    }

    /**
     * 对指定的对象或以该对象为根的对象图实施文本序列化（以UTF-8编码）。
     *
     * @param obj 要序列化的对象
     * @return 文本序列化
     */
    @Override
    public String serialize(Object obj) {
        //调用DoSerialize
        return this.doSerialize(obj);
    }

    /**
     * 对给定的文本（以UTF-8编码）实施反序列化，以重建对象（图）
     * 重写此方法进行反序列化
     *
     * @param serializationText 序列化文本
     * @param objType           要反序列化的对象的类型
     * @return 反序列化的对象
     */
    public abstract Object doDeserialize(String serializationText, Class<?> objType);

    /**
     * 对指定的对象或以该对象为根的对象图实施文本序列化（以UTF-8编码）
     * 重写此方法进行序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化的结果
     */
    public abstract String doSerialize(Object obj);
}
