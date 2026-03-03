package io.obase.test.infrastructure.modelRegister;

import io.obase.core.common.ITextSerializer;
import io.obase.core.common.Utils;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 逗号分隔的序列化器
 */
public class CommaSplitSerializer implements ITextSerializer {
    /**
     * 对给定的数据实施反序列化，以重建对象（图）。
     *
     * @param serializationStream 提供序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     * @param objType             反序列化的对象的类型
     * @return 对象
     */
    @Override
    public Object deserialize(InputStream serializationStream, Class<?> objType) {
        return Utils.readUtf8StringFromInputStream(serializationStream).split(",");
    }

    /**
     * 对指定的对象或以该对象为根的对象图实施序列化
     *
     * @param obj                 要序列化的对象
     * @param serializationStream 存储序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     */
    @Override
    public void serialize(Object obj, OutputStream serializationStream) {
        //此处传入的Obj肯定为string[]
        Utils.writeUtf8StringToOutputStream(String.join(",", (String[]) obj), serializationStream);
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
        return serializationText.split(",");
    }

    /**
     * 对指定的对象或以该对象为根的对象图实施文本序列化（以UTF-8编码）。
     *
     * @param obj 要序列化的对象
     * @return 文本序列化
     */
    @Override
    public String serialize(Object obj) {
        return String.join(",", (String[]) obj);
    }
}

