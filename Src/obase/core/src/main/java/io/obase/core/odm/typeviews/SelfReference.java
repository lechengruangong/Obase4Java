/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：反身引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-8 15:21:18
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.typeviews;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTreeNode;

/**
 * 反身引用，即视图对自已的源的引用。
 */
public class SelfReference extends ReferenceElement implements ITypeViewElement {

    /**
     * 影子元素
     */
    private SelfReference shadow;

    /**
     * 创建SelfReference实例
     *
     * @param name 引用元素的名称
     */
    public SelfReference(String name) {
        super(name, EElementType.AssociationReference);
    }

    /**
     * 获取影子元素
     *
     * @return 影子元素
     */
    @Override
    public SelfReference getShadow() {
        return this.shadow;
    }

    /**
     * 设置影子元素
     *
     * @param shadow 影子元素
     */
    @Override
    public void setShadow(ITypeViewElement shadow) {
        if (shadow instanceof SelfReference) {
            this.shadow = (SelfReference) shadow;
        }
    }

    /**
     * 获取引用元素的类型。当引用元素为关联引用时返回AssociationType；为关联端时返回EntityType。
     *
     * @return 引用元素的类型
     */
    @Override
    public ObjectType getReferenceType() {
        return null;
    }

    /**
     * 获取引用元素所承载的对象导航行为
     *
     * @return 引用元素所承载的对象导航行为
     */
    @Override
    public ObjectNavigation getNavigation() {
        return null;
    }

    /**
     * 获取引用元素在对象导航中承担的功能
     *
     * @return 引用元素在对象导航中承担的功能
     */
    @Override
    public ENavigationUse getNavigationUse() {
        return ENavigationUse.DirectlyReference;
    }

    /**
     * 获取反身引用的值的类型。总返回源的类型，如果源不为ObjectType，返回null。
     *
     * @return 反身引用的值的类型
     */
    @Override
    public TypeBase getValueType() {
        if (this.getHostType() instanceof ObjectType) return this.getHostType();
        return null;
    }

    /**
     * 生成在视图表达式中定义反身引用的表达式，它规定了属性的锚点和绑定。
     *
     * @param sourcePara           代表视图源的形参
     * @param flatteningParaGetter 一个委托，用于获取代表指定平展点的形参。
     * @return 定义当前反身引用的表达式
     */
    public Expression generateExpression(ParameterExpression sourcePara,
                                         FunctionWithOneArg<AssociationTreeNode, ParameterExpression> flatteningParaGetter) {
        return Expression.lambda(null, Expression.constant(this));
    }

    /**
     * 在基于当前引用元素实施关联导航的过程中，向前推进一步。
     *
     * @param sourceObj 本次导航步的出发地
     * @return 推进后的结果
     */
    @Override
    public Object[] navigationStep(Object sourceObj) {
        //反身引用 自己导航自己
        return new Object[]{sourceObj};
    }

    /**
     * 无延迟加载
     *
     * @param reason 返回不能启用延迟加载的原因
     * @return 如果可以启用延迟加载返回true，否则返回false，同时返回原因。
     */
    @Override
    protected boolean validateLazyLoading(ObjectReferencePack<String> reason) {
        reason.realValue = "反身引用无延迟加载";
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
        //反身引用 自己的键
        if (this.getReferenceType() != null)
            return this.getReferenceType().getAttributes().stream().filter(p -> this.getReferenceType().getKeyFields().contains(p.getName())).toArray(Attribute[]::new);
        return new Attribute[0];
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
        //反身引用 自己的键
        if (this.getReferenceType() != null)
            return this.getReferenceType().getAttributes().stream().filter(p -> this.getReferenceType().getKeyFields().contains(p.getName())).toArray(Attribute[]::new);
        return new Attribute[0];
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
