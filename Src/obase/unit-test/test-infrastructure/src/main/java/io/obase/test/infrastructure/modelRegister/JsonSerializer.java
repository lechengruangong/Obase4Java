package io.obase.test.infrastructure.modelRegister;

import com.alibaba.fastjson2.JSON;
import io.obase.core.common.TextSerializer;

/**
 * 普通的JSON序列化器
 */
public class JsonSerializer extends TextSerializer {
    /**
     * 对给定的文本（以UTF-8编码）实施反序列化，以重建对象（图）
     * 重写此方法进行反序列化
     *
     * @param serializationText 序列化文本
     * @param objType           要反序列化的对象的类型
     * @return 反序列化的对象
     */
    @Override
    public Object doDeserialize(String serializationText, Class<?> objType) {
        return JSON.parseObject(serializationText, objType);
    }

    /**
     * 对指定的对象或以该对象为根的对象图实施文本序列化（以UTF-8编码）
     * 重写此方法进行序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化的结果
     */
    @Override
    public String doSerialize(Object obj) {
        return JSON.toJSONString(obj);
    }
}
