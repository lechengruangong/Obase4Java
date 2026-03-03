/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：引用元素,如关联端和关联引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 14:55:55
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;
import io.obase.core.IdentityArray;
import io.obase.core.expression.*;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.query.QueryOp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

/**
 * 表示为引用元素，即引用目标对象的元素，如关联引用、关联端。
 */
public abstract class ReferenceElement extends TypeElement {

    /**
     * 是否启用延迟加载，默认true
     */
    private boolean enableLazyLoading = true;
    /**
     * 指示是否已从数据库加载了引用
     */
    private boolean hasLoaded;
    /**
     * 指定关联或关联端的加载优先级，数值小者先加载。
     */
    private int loadingPriority;
    /**
     * 加载触发器集合
     */
    private List<IBehaviorTrigger> loadingTriggers = new ArrayList<>();

    /**
     * 创建TypeElement实例
     *
     * @param name        元素的名称
     * @param elementType 元素的类型
     */
    protected ReferenceElement(String name, EElementType elementType) {
        super(name, elementType);
    }

    /**
     * 获取指定关联或关联端的加载优先级，数值小者先加载
     *
     * @return 指定关联或关联端的加载优先级，数值小者先加载
     */
    public int getLoadingPriority() {
        return this.loadingPriority;
    }

    /**
     * 设置 指定关联或关联端的加载优先级，数值小者先加载
     *
     * @param loadingPriority 指定关联或关联端的加载优先级，数值小者先加载
     */
    public void setLoadingPriority(int loadingPriority) {
        this.loadingPriority = loadingPriority;
    }

    /**
     * 获取指示是否已从数据库加载了引用
     *
     * @return 指示是否已从数据库加载了引用
     */
    public boolean getHasLoaded() {
        return this.hasLoaded;
    }

    /**
     * 设置指示是否已从数据库加载了引用
     *
     * @param hasLoaded 指示是否已从数据库加载了引用
     */
    public void setHasLoaded(boolean hasLoaded) {
        this.hasLoaded = hasLoaded;
    }

    /**
     * 获取一个值，该值指示是否启用延迟加载，默认为true。
     *
     * @return 是否启用延迟加载
     */
    public boolean getEnableLazyLoading() {
        return this.enableLazyLoading;
    }

    /**
     * 设置 一个值，该值指示是否启用延迟加载，默认为true
     *
     * @param enableLazyLoading 是否启用延迟加载
     */
    public void setEnableLazyLoading(boolean enableLazyLoading) {

        this.enableLazyLoading = enableLazyLoading;
    }

    /**
     * 获取加载触发器集合
     *
     * @return 加载触发器集合
     */
    public List<IBehaviorTrigger> getLoadingTriggers() {
        return this.loadingTriggers;
    }

    /**
     * 设置加载触发器集合
     *
     * @param loadingTriggers 加载触发器集合
     */
    public void setLoadingTriggers(List<IBehaviorTrigger> loadingTriggers) {
        this.loadingTriggers = loadingTriggers;
    }

    /**
     * 获取引用元素的类型。当引用元素为关联引用时返回AssociationType；为关联端时返回EntityType。
     *
     * @return 引用元素的类型
     */
    @Deprecated
    public abstract ObjectType getReferenceType();

    /**
     * 获取引用元素所承载的对象导航行为
     *
     * @return 引用元素所承载的对象导航行为
     */
    public abstract ObjectNavigation getNavigation();

    /**
     * 获取引用元素在对象导航中承担的功能
     *
     * @return 引用元素在对象导航中承担的功能
     */
    public abstract ENavigationUse getNavigationUse();

    /**
     * 在基于当前引用元素实施关联导航的过程中，向前推进一步。
     *
     * @param sourceObj 本次导航步的出发地
     * @return 推进后的结果
     */
    public abstract Object[] navigationStep(Object sourceObj);

    /**
     * 验证延迟加载合法性，由派生类实现
     *
     * @param reason 返回不能启用延迟加载的原因
     * @return 如果可以启用延迟加载返回true，否则返回false，同时返回原因。
     */
    protected abstract boolean validateLazyLoading(ObjectReferencePack<String> reason);

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
    public abstract Attribute[] getReferringKey(boolean defineMissing);

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
    public abstract Attribute[] getReferringKey();

    /**
     * 获取引用元素的引用键，并将键属性投射到以引用目标类型为终极源的视图，返回对应的视图属性。
     * 将属性投射到视图，是指在视图上定义或搜索一个直观属性，该直观属性锚定于扩展树根节点，绑定于以该属性。
     * 终极源是指在发生视图嵌套的情形下，最里层视图的源。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param targetView    指定属性投射视图
     * @param defineMissing 指示是否自动定义缺失的属性
     * @return 属性
     */
    public Attribute[] getReferringKey(TypeView targetView, boolean defineMissing) {
        //先无参获取
        Attribute[] referringAttrs = this.getReferringKey(defineMissing);
        if (targetView == null)
            return referringAttrs;

        //最终结果
        List<Attribute> resultList = new ArrayList<>();

        Stack<TypeView> stack = targetView.getNestingStack();
        //挨个弹出
        while (stack.size() > 0) {
            TypeView poppedView = stack.pop();

            List<Attribute> attrList = new ArrayList<>();

            for (Attribute attribute : referringAttrs) {
                Attribute viewAttr = poppedView.getIntuitiveAttribute(attribute);
                if (viewAttr != null)
                    attrList.add(viewAttr);
            }
            resultList.addAll(attrList);
        }

        return resultList.toArray(new Attribute[0]);
    }

    /**
     * 获取引用元素的引用键，并将键属性投射到以引用目标类型为终极源的视图，返回对应的视图属性。
     * 将属性投射到视图，是指在视图上定义或搜索一个直观属性，该直观属性锚定于扩展树根节点，绑定于以该属性。
     * 终极源是指在发生视图嵌套的情形下，最里层视图的源。
     * 说明
     * defineMissing参数指示引用键属性缺失时的行为，如果其值为true，则自动定义缺失的属性，否则引发KeyAttributeLackException。
     * 但是，即使指示自动定义缺失的属性，也不保证能定义成功。将根据现实条件判定能否定义，如果不能定义则引发CannotDefiningAttributeException。
     *
     * @param targetView 指定属性投射视图
     * @return 属性
     */
    public Attribute[] getReferringKey(TypeView targetView) {
        return this.getReferringKey(targetView, false);
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
    public abstract Attribute[] getReferredKey(boolean defineMissing);

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
    public abstract Attribute[] getReferredKey();

    /**
     * 生成引用加载查询。
     * 引用加载是指从存储源取出引用元素所指向的对象，例如给定一个实体对象，取出该对象某一关联引用所指向的对象，或者给定一个关联对象，取出其某一关联端对象。
     * 引用加载查询是一个查询链，执行该查询链得到结果就可以完成引用加载。
     * 在对象系统中，对象内的引用元素（简记为R）是基于关联定义的，关联是对象间引用的根本依据。因此存在两类基本的引用加载。一类是给定关联端，加载关联实例；另一类是给定
     * 关联对象，加载某一关联端。
     * 如果R是从关联指向端（即从关联对象指向关联端或从关联的伴随端指向另一端），引用键为目标型的标识键，参考键为R的关联型在目标端上的外键。
     * 如果R是从端指向关联（即从端对象，简称源端，指向关联对象或关联的伴随端），引用键为R的关联型在源端上的外键，参考键为源端的标识键。
     * 如果R的关联是隐式独立关联，则目标型上不存在R的引用键，源类型上也不存在R的参考键。在这种情况下，如果要构造引用加载查询，可采用两步运算，首先查询关联，然后投影
     * 到目标型。这实质上是将R分解为两个引用R1和R2，R1为“从端指向关联”的引用，R2为“从关联指向端”的引用。可以将R的关联显式化（即定义一个关联类），在显式化
     * 的关联类上定义R1的引用键和R2的参考键。
     *
     * @param sourceObjs 引用源对象
     * @param nextOp     后续运算。将串联在引用加载查询之后
     * @return 引用加载查询
     */
    public QueryOp generateLoadingQuery(Object[] sourceObjs, QueryOp nextOp) {
        //类型
        ObjectType initialType = this.getLoadingQueryInitialType();
        //引用键
        Attribute[] referringKey = this.getReferringKey(true);
        //参考键
        Attribute[] referredKey = this.getReferredKey(false);

        //过滤器
        LambdaExpression expression = this.generateFilter(initialType, referringKey, referredKey, sourceObjs);
        //构造查询

        return QueryOp.where(expression, this.getHostType().getModel(), nextOp);
    }

    /**
     * 为引用加载查询生成过滤器（查询条件）
     *
     * @param targetType   要查询的目标类型
     * @param referringKey 引用键
     * @param referredKey  参考键
     * @param sourceObjs   引用源对象
     * @return 查询过滤器的表达式
     */
    private LambdaExpression generateFilter(ObjectType targetType, Attribute[] referringKey,
                                            Attribute[] referredKey, Object[] sourceObjs) {
        //重建类型
        Class<?> rebuildingType = targetType.getRebuildingType();
        ParameterExpression parameterExp = Expression.parameter("o", rebuildingType);

        Expression body = null;
        for (Object obj : sourceObjs) {
            Expression eachObj = null;
            for (Attribute referring : referringKey) {
                for (Attribute referred : referredKey) {
                    Method prop;
                    try {
                        prop = rebuildingType.getMethod("get" + referring.getName());
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("无法获取" + referring.getName() + "所代表的属性,请参考内部异常.", e);
                    }

                    //构造一个形如 引用键==参考键.值的表达式
                    MemberExpression left = Expression.member(parameterExp, prop, parameterExp, parameterExp.getType());
                    Object value = referred.getValue(obj);
                    ConstantExpression right = Expression.constant(value);
                    BinaryExpression segment = Expression.equal(left, right, boolean.class);
                    //拼接
                    eachObj = eachObj == null ? segment : Expression.and(eachObj, segment, boolean.class);
                }

                //拼接
                body = body == null ? eachObj : Expression.or(body, eachObj, boolean.class);
            }
        }

        return Expression.lambda(new ParameterExpression[]{parameterExp}, body);
    }

    /**
     * 获取引用加载查询的基点类型，即Where运算的SourceType。
     * 实施说明
     * 如果NavigationUse == DirectlyReference，基点类型为Navigation.TargetType；否则：
     * （1）如果NavigationUse == EmitReference，基点类型为Navigation.AssociationType；
     * （2）如果NavigationUse == ArrivingReference，基点类型为TargetType。
     *
     * @return 查询的基点类型
     */
    protected ObjectType getLoadingQueryInitialType() {
        switch (this.getNavigationUse()) {
            case DirectlyReference:
            case ArrivingReference:
                return this.getNavigation().getTargetType();
            case EmittingReference:
                return this.getNavigation().getAssociationType();
            default:
                throw new RuntimeException("未知的导航功能: " + this.getNavigationUse());
        }
    }

    /**
     * 从指定的对象集合中筛选出引用目标对象或以引用目标类型为终极源的视图的实例
     *
     * @param objects    引用目标对象的候选集
     * @param sourceObj  作为引用源的对象
     * @param targetView 以引用目标类型为终极源的视图
     * @return 筛选后的对象
     */
    public Object[] filterTarget(ObjectReferencePack<Object[]> objects, Object sourceObj, TypeView targetView, boolean removing) {
        List<Object> result = new ArrayList<>();

        //要比较的参考标识
        IdentityArray referred = this.getReferredId(sourceObj);

        //要移除的
        List<Object> removed = new ArrayList<>();

        if (targetView != null) {
            for (Object obj : objects.realValue) {

                if (obj == null) continue;

                //要比较的属性
                Attribute[] attributes = this.getReferringKey(targetView, false);

                //序列比较
                if (referred.size() == attributes.length) {
                    //如果序列内对位值不相等 此值为true
                    boolean flag = false;
                    for (int i = 0; i < referred.size(); i++) {
                        //引用值
                        Object referringValue = attributes[i].getValue(obj);
                        if (!Objects.equals(referred.get(i).toString(), referringValue.toString()))
                            flag = true;
                    }

                    if (flag)
                        continue;
                    result.add(obj);
                    removed.add(obj);
                }
            }
        } else {
            //没有目标视图 根据sourceObj筛选 去掉关联型对象
            //逐一处理
            for (Object obj : objects.realValue) {

                if (obj != sourceObj) {
                    if (this.getHostType() instanceof ObjectType) {
                        ObjectType objectType = (ObjectType) this.getHostType();
                        //如果这个空引用的关联型与目标集合中的关联类型有关系
                        //才移除掉
                        AssociationType targetAssociationType = this.getHostType().getModel().getAssociationType(obj.getClass());
                        for (ReferenceElement referenceElement : objectType.getReferenceElements()) {
                            if (referenceElement instanceof AssociationReference) {
                                AssociationReference associationReference = (AssociationReference) referenceElement;
                                if (associationReference.getAssociationType().equals(targetAssociationType)) {
                                    result.add(obj);
                                    removed.add(obj);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (removing) {
            List<Object> removedResult = new ArrayList<>();
            for (Object obj : objects.realValue) {
                if (!removed.contains(obj)) {
                    removedResult.add(obj);
                }
            }
            objects.realValue = removedResult.toArray();
        }

        return result.toArray();
    }

    /**
     * 获取引用元素在指定对象上的引用标识，即引用键属性在指定对象上的值构成的序列。
     *
     * @param targetObj 要从其获取引用标识的对象
     * @return 对象标识
     */
    public IdentityArray getReferringId(Object targetObj) {
        //引用键
        Attribute[] referring = this.getReferringKey(false);

        //对象标志
        IdentityArray identityArray = new IdentityArray();

        for (Attribute attribute : referring) identityArray.add(attribute.getValue(targetObj));

        return identityArray;
    }

    /**
     * 获取引用元素在指定对象上的参考标识，即参考键属性在指定对象上的值构成的序列。
     *
     * @param targetObj 要从其获取参考标识的对象
     * @return 对象标识
     */
    public IdentityArray getReferredId(Object targetObj) {
        //参考键
        Attribute[] referred = this.getReferredKey(false);
        //对象标志
        IdentityArray identityArray = new IdentityArray();

        for (Attribute attribute : referred) identityArray.add(attribute.getValue(targetObj));

        return identityArray;
    }
}
