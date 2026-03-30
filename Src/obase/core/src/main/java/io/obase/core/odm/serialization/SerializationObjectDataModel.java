/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型,此模型全局应只有一个.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 12:19:01
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import java.util.ArrayList;
import java.util.List;

/**
 * 序列化对象数据模型
 */
public class SerializationObjectDataModel {

    public List<SerializationEntity> getTypes() {
        return new ArrayList<>();
    }

    public void addType(SerializationEntity modelType) {

    }
}
