/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：序列化对象数据模型对象序列化器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-3-30 14:03:11
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.serialization;

import io.obase.core.common.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 序列化对象数据模型对象序列化器
 */
public class SerializationObjectDataModelSerializer {

    /**
     * 序列化对象数据模型对象
     */
    private final SerializationObjectDataModel model;

    /**
     * 本次序列化过程中已经序列化的对象字典 key为对象的HashCode value为对象dto
     */
    private final HashMap<Integer, SerializationDataTransferObject> serializedDto = new HashMap<>();

    /**
     * 本次序列化过程中已经序列化的对象字典 key为对象的HashCode value为对象在本次序列化中分配的ID
     */
    private final HashMap<Integer, String> serializedObjects = new HashMap<>();

    /**
     * ID计数器 用于在序列化过程中为每个对象分配一个唯一ID 从$0开始递增
     */
    private int id;

    /**
     * 初始化序列化对象数据模型对象序列化器
     *
     * @param model 序列化对象数据模型对象
     */
    public SerializationObjectDataModelSerializer(SerializationObjectDataModel model) {
        this.model = model;
    }

    /**
     * 序列化对象数据模型的序列化方法
     * 最终返回Dto的包装对象
     *
     * @param list 要序列化的对象 无论是单值还是多值 都处理为List传入
     * @return Dto的包装对象
     */
    public SerializationDataTransferObjectWrapper serialize(List<Object> list) {
        for (Object obj : list) {
            if (obj != null) {
                //获取对象的模型类型 如果模型中没有定义这个类型 则不处理
                SerializationEntity type = this.model.getTypeOrNull(obj.getClass());
                if (type != null)
                    //处理对象的序列化
                    this.serialize(obj, true);
            }
        }

        //放入包装类型内
        SerializationDataTransferObjectWrapper wrapper = new SerializationDataTransferObjectWrapper(new ArrayList<>(this.serializedDto.values()));
        wrapper.setModifiedTime(LocalDateTime.now());
        //返回包装对象
        return wrapper;
    }

    /**
     * 某个具体对象的序列化方法
     *
     * @param obj    对象
     * @param isRoot 是否为根对象
     * @return dto
     */
    private List<String> serialize(Object obj, boolean isRoot) {
        //当前对象的类型
        Class<?> currentType = obj.getClass();

        //本层处理过的dto ID集合
        List<String> result = new ArrayList<>();

        //获取对象的模型类型 如果模型中没有定义这个类型 则不处理
        SerializationEntity type = this.model.getTypeOrNull(currentType);

        if (type != null) {
            SerializationDataTransferObject dto;
            //如果已经处理过 直接取之前的dto 否则新建一个dto进行处理
            if (!this.serializedObjects.containsKey(obj.hashCode())) {
                //分配ID
                String id = "$" + this.id++;
                //构造dto
                dto = new SerializationDataTransferObject();
                //dto的类型
                dto.setTypeName(currentType.getName());
                //为dto分配一个唯一ID
                dto.setId(id);
                //是否为根对象
                dto.setIsRoot(isRoot);
            } else {
                //从已有的集合中取出之前处理过的dto 并更新是否为根对象
                dto = this.serializedDto.get(obj.hashCode());
                dto.setIsRoot(isRoot);
            }

            //根据模型处理
            //处理构造函数参数
            for (SerializationConstructorParameter parameter : type.getConstructorParameters()) {
                //需要存储的构造函数参数 调用取值器获取值 进行存储
                if (parameter.getNeedStorage()) {
                    Object value = parameter.getValue(obj);
                    if (value != null && value.getClass() != parameter.getValueType() && Utils.isWrapperOrPrimitive(value.getClass(), parameter.getValueType()))
                        throw new IllegalArgumentException("序列化" + type.getClrType() + "的构造函数参数" + parameter.getIndex() + "时出错,配置的值类型为" + parameter.getValueType() + ",实际取到的为" + value.getClass() + ".");
                    dto.getConstructorParameters().put(parameter.getIndex(), Utils.convertSerializationValue(value));
                }
            }

            //处理属性
            for (SerializationAttribute attribute : type.getAttributes()) {
                Object value = attribute.getValue(obj);
                if (value != null && value.getClass() != attribute.getValueType() && Utils.isWrapperOrPrimitive(value.getClass(), attribute.getValueType()))
                    throw new IllegalArgumentException("序列化" + type.getClrType() + "的属性" + attribute.getName() + "时出错,配置的值类型为" + attribute.getValueType() + ",实际取到的为" + value.getClass() + ".");
                dto.getAttributes().put(attribute.getName(), Utils.convertSerializationValue(value));
            }


            //加入已处理的集合
            this.serializedObjects.put(obj.hashCode(), dto.getId());
            this.serializedDto.put(obj.hashCode(), dto);

            //加入结果集合
            result.add(dto.getId());

            //处理引用
            for (SerializationReference reference : type.getReferences()) {
                //取引用的值 无论单值还是集合 都以集合的形式进行处理
                Object value = reference.getValue(obj);
                List<Object> targets = Utils.getObjectList(value);

                //此引用的下层ID集合
                HashSet<String> idList = new HashSet<>();
                for (Object target : targets) {
                    if (target != null && this.model.getTypeOrNull(target.getClass()) != null) {
                        //如果没有处理过 进行处理
                        if (!this.serializedObjects.containsKey(target.hashCode())) {
                            //处理对象的序列化
                            List<String> nextIds = this.serialize(target, false);
                            //加入下一层的集合
                            if (nextIds.size() > 0) {
                                idList.addAll(nextIds);
                            }

                        }
                        //否则 只需要保存之前的ID
                        else {
                            idList.add(this.serializedObjects.get(target.hashCode()));
                        }
                    }
                }

                //赋值引用的ID集合
                if (idList.size() > 0)
                    dto.getReferences().put(reference.getName(), new ArrayList<>(idList));
            }
        }

        return result;
    }
}
