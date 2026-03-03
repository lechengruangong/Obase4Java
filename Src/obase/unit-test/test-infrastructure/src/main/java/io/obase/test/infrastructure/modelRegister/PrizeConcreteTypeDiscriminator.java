package io.obase.test.infrastructure.modelRegister;

import io.obase.core.GlobalModelCache;
import io.obase.core.ObjectContext;
import io.obase.core.odm.IConcreteTypeDiscriminator;
import io.obase.core.odm.StructuralType;
import io.obase.test.domain.association.implement.InKindPrize;
import io.obase.test.domain.association.implement.LuckyRedEnvelope;
import io.obase.test.domain.association.implement.RedEnvelope;

import java.util.Objects;

/**
 * 奖品的具体类型选择器
 */
public class PrizeConcreteTypeDiscriminator implements IConcreteTypeDiscriminator {

    /**
     * 上下文类型
     */
    private final Class<? extends ObjectContext> contextType;

    /**
     * 初始化奖品的具体类型选择器
     *
     * @param contextType 上下文类型
     */
    public PrizeConcreteTypeDiscriminator(Class<? extends ObjectContext> contextType) {

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
        //这里我们规定1是InKindPrize 2是RedEnvelope 3是LuckyRedEnvelope

        //从模型里取具体的类型 此处获取模型的参数是此配置属于的上下文类型
        StructuralType KindPrizeType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(InKindPrize.class);
        StructuralType redEnvelopeType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(RedEnvelope.class);
        StructuralType luckyRedEnvelopeType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(LuckyRedEnvelope.class);

        //处理参数
        if (typeCode == null)
            throw new IllegalArgumentException("未能获取类型判别参数.");

        if (Objects.equals(typeCode.toString(), "1"))
            return KindPrizeType;

        if (Objects.equals(typeCode.toString(), "2"))
            return redEnvelopeType;

        if (Objects.equals(typeCode.toString(), "3"))
            return luckyRedEnvelopeType;

        throw new IllegalArgumentException("未知的类型判别参数值" + typeCode + ".");
    }
}
