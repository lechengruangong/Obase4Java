package io.obase.test.infrastructure.modelRegister;

import io.obase.core.GlobalModelCache;
import io.obase.core.ObjectContext;
import io.obase.core.odm.IConcreteTypeDiscriminator;
import io.obase.core.odm.StructuralType;
import io.obase.test.domain.association.implement.CustomerDialogue;
import io.obase.test.domain.association.implement.Dialogue;

import java.util.Objects;

/**
 * 对话的具体类型选择器
 */
public class DialogueConcreteTypeDiscriminator implements IConcreteTypeDiscriminator {

    /**
     * 上下文类型
     */
    private final Class<? extends ObjectContext> contextType;

    /**
     * 初始化对话的具体类型选择器
     *
     * @param contextType 上下文类型
     */
    public DialogueConcreteTypeDiscriminator(Class<? extends ObjectContext> contextType) {

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
        //这里我们规定1是Bike 2是MyBikeA 3是myBikeB 4是MyBikeC

        //从模型里取具体的类型 此处获取模型的参数是此配置属于的上下文类型
        StructuralType dialogueType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(Dialogue.class);
        StructuralType customerDialogueType = GlobalModelCache.getInstance().getModel(this.contextType).getStructuralType(CustomerDialogue.class);

        //处理参数
        if (typeCode == null)
            throw new IllegalArgumentException("未能获取类型判别参数.");

        if (Objects.equals(typeCode.toString(), "1"))
            return dialogueType;

        if (Objects.equals(typeCode.toString(), "2"))
            return customerDialogueType;

        throw new IllegalArgumentException("未知的类型判别参数值" + typeCode + ".");
    }
}
