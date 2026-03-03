/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：视图引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 14:40:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.common.ObaseIntrospector;
import io.obase.core.common.Property;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 视图引用。来源于源（或源扩展）的一个引用元素。
 */
public class ViewReference extends ReferenceElement implements ITypeViewElement {

    /**
     * 视图引用的锚（或称锚点）。
     * 锚点是源扩展树上的一个节点，视图引用即来源于该节点代表类型的某个引用元素。
     */
    private final AssociationTreeNode anchor;

    /**
     * 视图引用绑定。
     * 绑定是一个视图引用锚所代表类型的一个引用元素，它是视图引用的来源。
     */
    private final ReferenceElement binding;

    /**
     * 影子元素
     */
    private ViewReference shadow;

    /**
     * 创建ViewReference实例
     *
     * @param binding 视图引用的绑定
     * @param name    元素名称
     * @param anchor  视图引用的锚
     */
    public ViewReference(ReferenceElement binding, String name, AssociationTreeNode anchor) {
        super(name, binding.getElementType());
        super.setIsMultiple(binding.getIsMultiple());
        this.binding = binding;
        this.anchor = anchor;
    }

    /**
     * 创建ViewReference实例
     *
     * @param binding 视图引用的绑定
     * @param anchor  视图引用的锚
     */
    public ViewReference(ReferenceElement binding, AssociationTreeNode anchor) {
        super(binding.getName(), binding.getElementType());
        super.setIsMultiple(binding.getIsMultiple());
        this.binding = binding;
        this.anchor = anchor;
    }

    /**
     * 获取视图引用的锚（或称锚点）
     *
     * @return 获取视图引用的锚（或称锚点）
     */
    public AssociationTreeNode getAnchor() {
        return this.anchor;
    }

    /**
     * 获取视图引用绑定
     *
     * @return 获取视图引用绑定
     */
    public ReferenceElement getBinding() {
        return this.binding;
    }

    /**
     * 获取引用元素的类型。当引用元素为关联引用时返回AssociationType；为关联端时返回EntityType。
     *
     * @return 引用元素的类型
     */
    @Override
    public ObjectType getReferenceType() {
        return this.binding.getReferenceType();
    }

    /**
     * 获取视图引用的影子元素
     *
     * @return 影子元素
     */
    public ViewReference getShadow() {
        return this.shadow;
    }

    /**
     * 设置视图引用的影子元素
     *
     * @param shadow 影子元素
     */
    public void setShadow(ITypeViewElement shadow) {
        this.shadow = (ViewReference) shadow;
    }

    /**
     * 获取引用元素所承载的对象导航行为
     *
     * @return 引用元素所承载的对象导航行为
     */
    @Override
    public ObjectNavigation getNavigation() {
        return this.binding.getNavigation();
    }

    /**
     * 获取引用元素在对象导航中承担的功能
     *
     * @return 引用元素在对象导航中承担的功能
     */
    @Override
    public ENavigationUse getNavigationUse() {
        return this.binding.getNavigationUse();
    }

    /**
     * 获取元素值的类型
     *
     * @return 元素值的类型
     */
    @Override
    public TypeBase getValueType() {
        switch (this.getNavigationUse()) {
            case ArrivingReference:
            case DirectlyReference:
                return this.getNavigation().getTargetType();
            case EmittingReference:
                return this.getNavigation().getAssociationType();
            default:
                throw new IllegalArgumentException("未知的导航类型");
        }
    }

    /**
     * 生成在视图表达式中定义当前元素的表达式，它规定了该元素的锚点和绑定。
     *
     * @param sourcePara           代表视图源的形参
     * @param flatteningParaGetter 一个委托，用于获取代表指定平展点的形参
     */
    @Override
    public Expression generateExpression(ParameterExpression sourcePara, FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter) {
        AssociationExpressionGenerator typeExpGenerator = new AssociationExpressionGenerator(sourcePara, flatteningParaGetter);
        LambdaExpression hostExp = this.anchor.asTree().accept(typeExpGenerator);
        Class<?> hostType = hostExp.getParameters()[0].getType();
        if (this.anchor instanceof ObjectTypeNode) {
            ObjectTypeNode node = (ObjectTypeNode) this.anchor;
            //使用属性表达式生成
            AttributeExpressionGenerator generator = new AttributeExpressionGenerator(hostExp);
            AttributeTreeNode attrNode;
            Property property = ObaseIntrospector.getObaseBeanProperties(hostType).stream().filter(p -> Objects.equals(p.getName(), node.getElementName())).findFirst().orElse(null);
            if (property != null) {
                attrNode = new SimpleAttributeNode(new Attribute(hostType, property.getName()));
                attrNode.asTree().accept(generator);
            }

            return generator.getResult();
        }

        throw new IllegalArgumentException("无法为视图锚点生成表达式");
    }

    /**
     * 穿透内嵌视图，获取视图引用的终极绑定。
     *
     * @return 视图引用的终极绑定
     */
    public ReferenceElement getFinalBinding() {
        //如果视图引用的绑定仍然是视图引用，则获取后者的绑定，依此规则递归调用，直到获取最终绑定的引用元素。
        if (this.getBinding() instanceof ViewReference) {
            return ((ViewReference) this.getBinding()).getFinalBinding();
        }
        return this.getBinding();
    }

    /**
     * 在基于当前引用元素实施关联导航的过程中，向前推进一步。
     *
     * @param sourceObj 本次导航步的出发地
     * @return 推进后的结果
     */
    @Override
    public Object[] navigationStep(Object sourceObj) {
        //返回绑定元素的导航结果
        return this.binding == null ? new Object[0] : this.binding.navigationStep(sourceObj);
    }

    /**
     * 验证延迟加载合法性，由派生类实现
     *
     * @param reason 返回不能启用延迟加载的原因
     * @return 如果可以启用延迟加载返回true，否则返回false，同时返回原因。
     */
    @Override
    protected boolean validateLazyLoading(ObjectReferencePack<String> reason) {
        reason.realValue = "视图引用无延迟加载";
        return false;
    }

    /**
     * 获取引用元素的引用键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param defineMissing 指示是否自动定义缺失的属性
     * @return 引用元素的引用键
     */
    @Override
    public Attribute[] getReferringKey(boolean defineMissing) {
        //获取绑定的引用元素
        ReferenceElement refElement = this.getFinalBinding();

        return refElement.getReferringKey(defineMissing);
    }

    /**
     * 获取引用元素的引用键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @return 引用元素的引用键
     */
    @Override
    public Attribute[] getReferringKey() {
        return this.getReferringKey(false);
    }

    /**
     * 获取引用元素的参考键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示参考键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param defineMissing 指示是否自动定义缺失的属性
     * @return 引用元素的参考键
     */
    @Override
    public Attribute[] getReferredKey(boolean defineMissing) {
        //获取绑定的引用元素
        ReferenceElement refElement = this.getFinalBinding();

        Attribute[] attributes;
        try {
            attributes = refElement.getReferredKey(false);
        } catch (KeyAttributeLackException ex) {
            if (defineMissing) throw new CannotDefiningAttributeException("无法定义缺失的参考键属性", ex);

            throw ex;
        }
        List<Attribute> result = new ArrayList<>();
        if (this.getHostType() instanceof TypeView) {
            TypeView typeView = (TypeView) this.getHostType();
            //获取锚点
            AssociationTreeNode anchor = this.getAnchor();
            for (Attribute attribute : attributes) {
                ViewAttribute keyAttribute = typeView.getIntuitiveAttribute(attribute, anchor);
                if (keyAttribute == null) {
                    if (defineMissing)
                        throw new CannotDefiningAttributeException("无法定义缺失的参考键属性", null);
                    throw new KeyAttributeLackException("引用键属性没有定义");
                }
                result.add(attribute);
            }
        }

        return result.toArray(new Attribute[0]);
    }

    /**
     * 获取引用元素的参考键。
     * 设S(s1, s2, ..., sn)为对象O的属性序列，R为O的一个引用元素，该引用的目标型RT存在一个属性序列T(t1, t2, ...,
     * tn)，将S与T的元素一一配对，即ti -> si，然后以ti为依据、以si在O上的取值为参考值构建过滤器，即
     * ∩ ti = vi (i = 1, 2, ..., n)，其中vi为属性si在对象O上的取值，
     * 以该过滤器作用于RT的实例集，如果所得到的对象集刚好为R的值，则称T为R的引用键，ti为引用属性，S为参考键，si为参考属性。
     * 引用键和参考键均不是必须的，如果没有不影响引用的加载，例如在关系数据库中可以通过联表的方式加载。
     * 说明
     * defineMissing参数指示参考键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @return 引用元素的参考键
     */
    @Override
    public Attribute[] getReferredKey() {
        return this.getReferredKey(false);
    }
}
