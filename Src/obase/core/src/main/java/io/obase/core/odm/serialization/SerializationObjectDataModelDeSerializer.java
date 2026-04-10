/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型对象反序列化器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:16:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import io.obase.core.common.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 序列化对象数据模型对象反序列化器
 */
public class SerializationObjectDataModelDeSerializer {

    /**
     * 本次反序列化过程中已经反序列化的对象字典 key为对象在本次反序列化中分配的ID value为反序列化后的对象
     */
    private final HashMap<String, Object> deSerializedObject = new HashMap<>();

    /**
     * 序列化对象数据模型对象
     */
    private final SerializationObjectDataModel model;

    /**
     * 初始化序列化对象数据模型对象反序列化器
     *
     * @param model 模型
     */
    public SerializationObjectDataModelDeSerializer(SerializationObjectDataModel model) {
        this.model = model;
    }

    /**
     * 序列化对象数据模型的反序列化方法
     * 最终返回反序列化后的对象集合 其顺序与传入的Dto集合中根对象的顺序一致
     *
     * @param wrapper Dto的包装对象
     * @return 反序列化后的对象集合
     */
    public List<Object> deSerialize(SerializationDataTransferObjectWrapper wrapper) {
        //对象集合
        for (SerializationDataTransferObject dto : wrapper.getDto()) {
            //当前对象的类型
            Class<?> currentType = null;
            if (!Utils.getStringIsEmpty(dto.getTypeName())) {
                try {
                    currentType = Class.forName(dto.getTypeName());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("无法加载要反序列化的类" + dto.getTypeName(), e);
                }
            }

            //获取对象的模型类型 如果模型中没有定义这个类型 则不处理
            SerializationEntity type = this.model.getTypeOrNull(currentType);
            if (type != null) {
                List<Object> parameterValues = new ArrayList<>();
                //处理构造函数
                List<SerializationConstructorParameter> parameters = type.getConstructorParameters();
                //按照Index排序
                parameters.sort((o1, o2) -> {
                    //根据具体的类型进行比较
                    int xCode = Integer.parseInt(o1.getIndex().replace("#", ""));
                    int yCode = Integer.parseInt(o2.getIndex().replace("#", ""));
                    return xCode - yCode;
                });
                for (SerializationConstructorParameter parameter : parameters) {
                    Object value = null;
                    //如果是需要存储 则从dto的构造函数参数字典中取出对应索引的值
                    if (parameter.getNeedStorage()) {
                        if (dto.getConstructorParameters().containsKey(parameter.getIndex())) {
                            Object constructorParameter = dto.getConstructorParameters().get(parameter.getIndex());
                            try {
                                //进行一次通用的转换
                                value = Utils.convertDbValue(constructorParameter, parameter.getValueType());
                            } catch (Exception exception) {
                                throw new IllegalArgumentException("无法转换" + type.getName() + "的" + parameter.getIndex() + "构造函数参数,如果是时间类型请不设置序列化格式或者设置为yyyy-MM-dd HH:mm:ss.SSS");
                            }
                        }
                    } else {
                        //否则使用取值器获取 注意此时固定传参为null
                        value = parameter.getValue(null);
                    }
                    //统一进行一次类型检查 如果不为null且类型不匹配 则抛出异常
                    if (value != null && value.getClass() != parameter.getValueType() && Utils.isWrapperOrPrimitive(value.getClass(), parameter.getValueType()))
                        throw new IllegalArgumentException("反序列化" + type.getClrType() + "的构造函数参数" + parameter.getIndex() + "时出错,配置的值类型为" + parameter.getValueType() + ",实际取到的为" + value.getClass() + ".");
                    parameterValues.add(value);
                }

                Object obj = type.getConstructor().construct(parameterValues.toArray());

                //处理属性
                for (SerializationAttribute attribute : type.getAttributes()) {
                    try {
                        Object value = Utils.convertDbValue(dto.getAttributes().get(attribute.getName()), attribute.getValueType());
                        if (value != null)
                            attribute.setValue(obj, value);
                    } catch (Exception exception) {
                        throw new IllegalArgumentException("无法转换" + type.getName() + "的" + attribute.getName() + "属性,如果是时间类型请不设置序列化格式或者设置为yyyy-MM-dd HH:mm:ss.SSS");
                    }

                }

                //加入已处理的集合
                this.deSerializedObject.put(dto.getId(), obj);
            }
        }

        //处理引用
        //取出其中的根对象 其余对象都是从根对象出发被引用的
        List<String> rootIds = wrapper.getDto().stream().filter(SerializationDataTransferObject::getIsRoot).map(SerializationDataTransferObject::getId).collect(Collectors.toList());
        //递归的设置引用
        this.setReferences(rootIds, wrapper.getDto(), new HashSet<>());

        //返回根对象集合
        List<Object> result = new ArrayList<>();
        for (String objKey : this.deSerializedObject.keySet()) {
            if (rootIds.contains(objKey)) {
                result.add(this.deSerializedObject.get(objKey));
            }
        }
        return result;
    }

    /**
     * 设置引用
     *
     * @param ids       序列化ID结合
     * @param dtoS      dto
     * @param hasSetIds 已经处理过的ID
     */
    private void setReferences(List<String> ids, List<SerializationDataTransferObject> dtoS,
                               HashSet<String> hasSetIds) {
        for (String id : ids) {

            if (!this.deSerializedObject.containsKey(id))
                continue;
            //取出对象
            Object obj = this.deSerializedObject.get(id);
            //对象的类型
            Class<?> currentType = obj.getClass();
            //获取对象的模型类型 如果模型中没有定义这个类型 则不处理
            SerializationEntity type = this.model.getTypeOrNull(currentType);
            if (type != null) {
                //取出对象的Dto
                SerializationDataTransferObject dto = dtoS.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
                if (dto != null) {
                    //根据dto的引用字典处理
                    for (String referenceKey : dto.getReferences().keySet()) {
                        if (!hasSetIds.contains(dto.getId())) {
                            SerializationReference refElement = type.getReferences().stream().filter(p -> p.getName().equals(referenceKey)).findFirst().orElse(null);
                            if (refElement != null) {
                                //保存至已处理的集合中 避免下一层循环引用时重复处理
                                hasSetIds.add(dto.getId());
                                //下层的结果
                                List<Object> results = new ArrayList<>();
                                for (String referenceId : dto.getReferences().get(referenceKey)) {
                                    //为下一层设置引用
                                    this.setReferences(dto.getReferences().get(referenceKey), dtoS, hasSetIds);
                                    //取出当前层的引用
                                    if (this.deSerializedObject.containsKey(referenceId)) {
                                        results.add(this.deSerializedObject.get(referenceId));
                                    }
                                }

                                //设置值
                                if (results.size() > 0)
                                    //如果是多值的属性 直接设置 否则 设置首个
                                    refElement.setValue(obj, refElement.getMultiple() ? results : results.get(0));
                            }
                        }
                    }
                }
            }
        }
    }
}
