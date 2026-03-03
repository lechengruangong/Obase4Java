/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化程序接口,为序列化程序定义调用规范.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-4 16:34:09
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.common;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化程序接口，为序列化程序定义调用规范。
 */
public interface ISerializer {

    /**
     * 对给定的数据实施反序列化，以重建对象（图）。
     *
     * @param serializationStream 提供序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     * @param objType             反序列化的对象的类型
     * @return 对象
     */
    Object deserialize(InputStream serializationStream, Class<?> objType);

    /**
     * 对指定的对象或以该对象为根的对象图实施序列化
     *
     * @param obj                 要序列化的对象
     * @param serializationStream 存储序列化数据的流，它可以指代多种后备存储区，如内存、文件、网络等
     */
    void serialize(Object obj, OutputStream serializationStream);
}
