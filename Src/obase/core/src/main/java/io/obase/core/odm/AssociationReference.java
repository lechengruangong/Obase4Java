/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：关联引用.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 12:29:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;
import io.obase.core.expression.Expression;
import io.obase.core.expression.LambdaExpression;
import io.obase.core.expression.MemberExpression;
import io.obase.core.expression.ParameterExpression;
import io.obase.core.query.QueryOp;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示关联引用
 */
public class AssociationReference extends ReferenceElement {

    /**
     * 关联型
     */
    private final AssociationType associationType;

    /**
     * 左端名
     */
    private final String leftEnd;

    /**
     * 聚合级别
     */
    private EAggregationLevel aggregationLevel;

    /**
     * 寄存 引用元素所承载的对象导航行为
     */
    private ObjectNavigation navigation;

    /**
     * 右端名
     */
    private String rightEnd;

    /**
     * 创建关联引用实例
     *
     * @param name            关联引用的名称
     * @param associationType 关联型
     * @param leftEnd         左端名
     * @param rightEnd        右端名
     */
    public AssociationReference(String name, AssociationType associationType, String leftEnd, String rightEnd) {
        super(name, EElementType.AssociationReference);

        this.leftEnd = leftEnd;
        this.rightEnd = rightEnd;
        this.associationType = associationType;
    }

    /**
     * 获取一个值，该值指示是否以左端表作为关联表
     *
     * @return 是否以左端表作为关联表
     */
    public boolean getLeftAsAssociationTable() {
        AssociationEnd associationend = this.associationType.getAssociationEnd(this.leftEnd);

        return associationend != null && this.associationType.isCompanionEnd(associationend);
    }

    /**
     * 获取一个值，该值指示是否以右端表作为关联表。
     *
     * @return 是否以右端表作为关联表
     */
    public boolean getRightAsAssociationTable() {
        AssociationEnd associationend = this.associationType.getAssociationEnd(this.rightEnd);
        return associationend != null && this.associationType.isCompanionEnd(associationend);
    }

    /**
     * 获取一个值，该值指示关联表是否独立。（表示有独立于左右端之外表示关系的表）
     *
     * @return 该值指示关联表是否独立
     */
    public boolean getIndependentAssociationTable() {
        return !(this.getLeftAsAssociationTable() || this.getRightAsAssociationTable());
    }

    /**
     * 获取聚合级别
     *
     * @return 聚合级别
     */
    public EAggregationLevel getAggregationLevel() {
        return this.aggregationLevel;
    }

    /**
     * 置聚合级别
     *
     * @param aggregationLevel 聚合级别
     */
    public void setAggregationLevel(EAggregationLevel aggregationLevel) {
        this.aggregationLevel = aggregationLevel;
    }

    /**
     * 获取左端名
     *
     * @return 左端名
     */
    public String getLeftEnd() {
        return this.leftEnd;
    }

    /**
     * 获取右端名
     *
     * @return 右端名
     */
    public String getRightEnd() {
        return this.rightEnd;
    }

    /**
     * 设置右端名
     *
     * @param rightEnd 右端名
     */
    public void setRightEnd(String rightEnd) {
        this.rightEnd = rightEnd;
    }

    /**
     * 获取关联型
     *
     * @return 获取关联型
     */
    public AssociationType getAssociationType() {
        return this.associationType;
    }

    /**
     * 获取引用元素的类型
     * 当引用元素为关联引用时返回AssociationType
     *
     * @return 引用元素的类型
     */
    @Override
    public ObjectType getReferenceType() {
        return this.associationType;
    }

    /**
     * 获取引用元素所承载的对象导航行为。
     * 实施说明：
     * 当关联型为隐式关联时为直接导航，根据LeftEnd和RightEnd可以推断源端和目标端。
     * 当关联型为显式关联时为间接导航，根据LeftEnd可推断源端，目标端不明确。
     *
     * @return 对象导航行为
     */
    @Override
    public ObjectNavigation getNavigation() {
        if (this.navigation == null) {
            String source = this.getLeftEnd(), target = this.getRightEnd();
            if (this.associationType.getVisible()) //显式关联
                target = null;
            this.navigation = new ObjectNavigation(this.associationType, source, target);
        }
        return this.navigation;
    }

    /**
     * 获取关联引用在对象导航中的用途
     *
     * @return 对象导航中的用途
     */
    @Override
    public ENavigationUse getNavigationUse() {
        if (this.associationType.getVisible()) return ENavigationUse.EmittingReference;
        return ENavigationUse.DirectlyReference;
    }

    /**
     * 获取元素值的类型
     *
     * @return 获取元素值的类型
     */
    @Override
    public TypeBase getValueType() {
        if (!this.associationType.getVisible())//隐式关联
            return this.gotRightEnd().getEntityType();
        return this.associationType;
    }

    /**
     * 在基于当前引用元素实施关联导航的过程中，向前推进一步。
     *
     * @param sourceObj 本次导航步的出发地
     * @return 推进后的结果
     */
    @Override
    public Object[] navigationStep(Object sourceObj) {
        List<Object> target = new ArrayList<>();
        //获取关联引用对象（取出的是实体对象集合:List<文章>）
        Object value = this.getValue(sourceObj);
        if (value instanceof Iterable) {
            Iterable<?> iEnumerable = (Iterable<Object>) value;
            for (Object o : iEnumerable) {
                target.add(o);
            }
        } else {
            target.add(value);
        }
        if (!this.getAssociationType().getVisible()) {
            AssociationType type = this.getAssociationType();
            List<Object> assocObjs = new ArrayList<>();
            //遍历关联对象引用
            for (Object item : target) {
                //左端对象 和 右端对象
                Map<String, Object> dic = new HashMap<>();
                dic.put(this.getLeftEnd(), sourceObj);
                dic.put(this.getRightEnd(), item);
                //创建隐式关联对象（构造的是隐式关联对象（ImplicitAssociation<分类,文章>））
                Object assObj = this.buildObject(type, dic);
                //找出左端
                Optional<AssociationEnd> leftEnd = type.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(this.getLeftEnd())).findFirst();
                leftEnd.ifPresent(associationEnd -> this.setEndMappingFiled(type, associationEnd, sourceObj, assObj));
                //找出右端
                Optional<AssociationEnd> rightEnd = type.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(this.getRightEnd())).findFirst();
                rightEnd.ifPresent(associationEnd -> this.setEndMappingFiled(type, associationEnd, item, assObj));
                //添加到关联对象集合
                assocObjs.add(assObj);
            }

            //隐式关联对象集合
            target = assocObjs;
        }

        //返回 显示关联直接就是引用对象 隐式则需要创建
        return target.toArray(new Object[0]);
    }


    /**
     * 构造对象
     *
     * @param type            目标对象的类型（实体型、关联型、复杂类型）
     * @param referredObjects 一个字典，存储目标对象的关联引用或关联端，键为元素名称，值为关联引用或关联端对象
     * @return 构造的关联型对象
     */
    private Object buildObject(StructuralType type, Map<String, Object> referredObjects) {
        //使用构造器构造对象
        Object target = type.getConstructor().construct(null);
        //遍历引用属性（关联端、关联引用）
        for (TypeElement re : type.getElements())
            if (referredObjects != null) {
                this.setValue(target, referredObjects.get(re.getName()));
            }
        return target;
    }

    /**
     * 为隐式关联型的关联映射属性设值
     *
     * @param type   关联型
     * @param end    关联端
     * @param endObj 端对象
     * @param assObj 关联对象
     */
    private void setEndMappingFiled(AssociationType type, AssociationEnd end, Object endObj, Object assObj) {
        //端的实体型
        EntityType endType = end.getEntityType();
        //端的键属性
        List<Attribute> endKeyAttrs = endType.getAttributes().stream().filter(p -> endType.getKeyAttributes().contains(p.getName())).collect(Collectors.toList());
        //在端内寻找符合条件的映射和属性
        for (Attribute endKeyAttr : endKeyAttrs) {
            for (AssociationEndMapping mapping : end.getMappings()) {
                //如果映射键属性和端对象键属性相同
                if (Objects.equals(endKeyAttr.getName(), mapping.getKeyAttribute())) {
                    //从端对象内取出键属性
                    Object keyValue = endKeyAttr.getValueGetter().getValue(endObj);
                    //为关联型内映射属性(映射属性在表内字段肯定和映射的目标属性相同)赋值
                    Optional<Attribute> attribute = type.getAttributes().stream().filter(p -> p.getTargetField().equalsIgnoreCase(mapping.getTargetField())).findFirst();
                    if (attribute.isPresent() && attribute.get().getValueSetter() != null) {
                        attribute.get().getValueSetter().setValue(assObj, keyValue);
                    }
                }
            }
        }
    }

    /**
     * 验证延迟加载合法性，由派生类实现
     *
     * @param reason 返回不能启用延迟加载的原因
     * @return 是否合法
     */
    @Override
    protected boolean validateLazyLoading(ObjectReferencePack<String> reason) {
        reason.realValue = "";
        if (this.associationType.getVisible()) return true;
        AssociationEnd leftEnd = this.associationType.getAssociationEnd(this.getLeftEnd());
        AssociationEnd rightEnd = this.associationType.getAssociationEnd(this.getRightEnd());

        for (AssociationEndMapping mapping : rightEnd.getMappings()) {
            Attribute attr = leftEnd.getEntityType().findAttributeByTargetField(mapping.getTargetField());

            if (attr == null) {
                reason.realValue = "当前对象（" + leftEnd.getEntityType().getNamespace() + leftEnd.getEntityType().getName() + "）没有关联引用（" + this.associationType.getName() + "）右端对象的标识属性";
                return false;
            }
        }

        return true;
    }

    /**
     * 获取关联引用的左端
     *
     * @return 关联引用的左端
     */
    public AssociationEnd gotLeftEnd() {
        return this.associationType.getAssociationEnd(this.leftEnd);
    }

    /**
     * 获取
     * 当关联引用的关联型为显式关联或多方关联时，关联引用不直接指向关联端，而是指向关联本身，这种情况下本方法返回null。
     *
     * @return 关联引用的右端
     */
    public AssociationEnd gotRightEnd() {
        if (this.associationType.getVisible() || this.associationType.getAssociationEnds().size() > 2)
            return null;

        return this.associationType.getAssociationEnd(this.rightEnd);
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
        List<Attribute> result = new ArrayList<>();

        //左端
        AssociationEnd leftEnd = this.gotLeftEnd();

        try {

            if (this.associationType.isCompanionEnd(leftEnd) && !this.associationType.getVisible()) {
                //右端实体型
                EntityType rightEntity = this.associationType.getAssociationEnd(this.rightEnd).getEntityType();
                result.addAll(rightEntity.getAttributes().stream().filter(p -> rightEntity.getKeyFields().contains(p.getName()))
                        .collect(Collectors.toList()));
            } else {
                result.addAll(Arrays.asList(leftEnd.getForeignKey(defineMissing)));
            }

        } catch (Exception ex) {
            throw new CannotDefiningAttributeException("无法定义缺失的参考键属性", ex);
        }

        return result.toArray(new Attribute[0]);
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
        List<Attribute> result = new ArrayList<>();

        //左端
        AssociationEnd leftEnd = this.gotLeftEnd();

        try {

            if (this.associationType.isCompanionEnd(leftEnd) && !this.associationType.getVisible()) {
                result.addAll(Arrays.asList(this.associationType.getAssociationEnd(this.rightEnd).getForeignKey(defineMissing)));
            } else {
                if (this.getHostType() instanceof EntityType) {
                    EntityType entityType = (EntityType) this.getHostType();
                    result.addAll(entityType.getAttributes().stream().filter(p -> entityType.getKeyFields().contains(p.getName())).collect(Collectors.toList()));
                }
            }

        } catch (Exception ex) {
            throw new CannotDefiningAttributeException("无法定义缺失的参考键属性", ex);
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

    /**
     * 生成引用加载查询
     *
     * @param sourceObjs 引用源对象
     * @param through    指示是否穿透隐式独立关联
     * @param nextOp     后续运算。将串联在引用加载查询之后
     * @return 引用加载查询
     */
    public QueryOp generateLoadingQuery(Object[] sourceObjs, boolean through, QueryOp nextOp) {

        //要穿透隐式关联
        if (!this.getAssociationType().getVisible() && this.getAssociationType().getIndependent() && through) {
            ParameterExpression paraExp = Expression.parameter("o", this.getAssociationType().getClrType());
            MemberExpression body;
            try {
                body = Expression.member(paraExp, this.getAssociationType().getClrType().getMethod("get" + this.rightEnd), paraExp, paraExp.getType());
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("无法获取关联型" + this.getAssociationType().getClrType().getName() + "的属性" + this.rightEnd + ",请参考内部异常.", e);
            }
            LambdaExpression selectionExp = Expression.lambda(new ParameterExpression[]{paraExp}, body);
            nextOp = QueryOp.select(selectionExp, this.getHostType().getModel(), nextOp);
        }

        return this.generateLoadingQuery(sourceObjs, nextOp);
    }


    /**
     * 获取引用加载查询的基点类型，即Where运算的SourceType。
     * 实施说明
     * 如果关联引用的关联型为隐式独立关联，基点类型为关联类型；否则，调用基实现。
     *
     * @return 基点类型
     */
    @Override
    protected ObjectType getLoadingQueryInitialType() {
        if (!this.getAssociationType().getVisible() && this.getAssociationType().getIndependent())
            return this.getAssociationType();
        return super.getLoadingQueryInitialType();
    }

    /**
     * 转换为字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "AssociationReference:{{Name-\"" + this.getName() + "\",AssociationType-\"" + this.associationType + "\"}}";
    }
}
