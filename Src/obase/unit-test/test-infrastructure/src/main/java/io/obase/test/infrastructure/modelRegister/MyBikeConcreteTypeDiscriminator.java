package io.obase.test.infrastructure.modelRegister;

import io.obase.core.GlobalModelCache;
import io.obase.core.ObjectContext;
import io.obase.core.odm.IConcreteTypeDiscriminator;
import io.obase.core.odm.StructuralType;
import io.obase.test.domain.association.implement.MyBikeA;
import io.obase.test.domain.association.implement.MyBikeC;

import java.util.Objects;

/**
 * 我的自行车类型选择器
 */
public class MyBikeConcreteTypeDiscriminator implements IConcreteTypeDiscriminator {

    /**
     * 上下文类型
     */
    private final Class<? extends ObjectContext> contextType;

    /**
     * 初始化我的自行车类型选择器
     *
     * @param contextType 上下文类型
     */
    public MyBikeConcreteTypeDiscriminator(Class<? extends ObjectContext> contextType) {

        this.contextType = contextType;
    }

    /**
     * 根据类型代码选择一个具体类型
     *
     * @param typeCode 类型代码
     * @return 具体的结构化类型
     */
    @Override
    public StructuralType discriminate(Object typeCode) {
        //这里的类型代码typeCode就是获取到的用于判别类型的值
        //这里我们规定2是MyBikeA 4是MyBikeC

        //从模型里取具体的类型 此处获取模型的参数是此配置属于的上下文类型
        StructuralType myBikeAType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(MyBikeA.class);
        StructuralType myBikeCType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(MyBikeC.class);

        //处理参数
        if (typeCode == null)
            throw new IllegalArgumentException("未能获取类型判别参数.");

        if (Objects.equals(typeCode.toString(), "2"))
            return myBikeAType;

        if (Objects.equals(typeCode.toString(), "4"))
            return myBikeCType;

        throw new IllegalArgumentException("未知的类型判别参数值" + typeCode + ".");
    }
}
