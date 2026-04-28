/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示对象仓.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-31 17:10:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Property;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import io.obase.core.query.QueryOp;
import io.obase.core.query.QueryProvider;
import io.obase.core.saving.EObjectStatus;
import io.obase.core.saving.ObjectSystemVisitor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表示对象仓。
 * 对象仓是对象的生存环境，主要负责四个方面的职责：（1）记录对象类型、标识等信息；（2）维护对象状态；（3）更改跟踪；（4）延迟加载。
 * 对象仓提供两种方式来跟踪对象的更改。如果对象实现了IIntervene接口，对象仓将作为介入者（实现IIntervener接口）介入到对象属性的修改流程从而监视
 * 对象的属性修改行为。如果对象未实现IIntervene接口，对象仓将以属性快照方式跟踪更改。
 * 当对象类型包含至少一个设置了修改触发器的属性时，Obase将自动为对象类生成实现IIntervene接口的代理类型。只有当应用程序使用ObjectSet{T}.Create方法创建对象实例时才会应用此代理类型。
 * 对象仓始终使用快照方式跟踪关联引用变更。
 */
public class ObjectHouse implements IIntervener {

    /**
     * 属性变更集合
     */
    private final Set<String> changedAttributes = new HashSet<>();

    /**
     * 对象上下文
     */
    private final ObjectContext objectContext;

    /**
     * 属性快照字典
     */
    private final Map<String, Object> propDic = new HashMap<>();

    /**
     * 指示对象仓中的对象是否作为上下文的根对象
     */
    private boolean asRoot;

    /**
     * 在对象变更探测过程中，指示对象是否被标记为“保留”。对于新对象，默认值为true，对于旧对象默认值为false
     */
    private boolean isRetained;

    /**
     * 对象仓中放置的对象
     */
    private Object object;

    /**
     * 对象仓中放置的对象的标识
     */
    private ObjectKey objectKey;

    /**
     * 对象仓中放置的对象的类型
     */
    private StructuralType objectType;

    /**
     * 对象仓中放置的对象的状态
     */
    private EObjectStatus status;

    /**
     * 创建对象仓实例
     *
     * @param hostContext 对象仓所属的对象上下文
     */
    public ObjectHouse(ObjectContext hostContext) {
        this.objectContext = hostContext;
    }

    /**
     * 指示对象仓中的对象是否作为上下文的根对象
     *
     * @return 指示对象仓中的对象是否作为上下文的根对象
     */
    public boolean getAsRoot() {
        return this.asRoot;
    }

    /**
     * 获取对象仓中放置的对象的状态
     *
     * @return 获取对象仓中放置的对象的状态
     */
    public EObjectStatus getStatus() {
        return this.status;
    }

    /**
     * 获取一个值，该值在对象变更探测过程中，指示对象是否被标记为“保留”。对于新对象，默认值为true，对于旧对象默认值为false。
     *
     * @return 是否保留
     */
    public boolean getIsRetained() {
        return this.isRetained;
    }

    /**
     * 设置一个值，该值在对象变更探测过程中，指示对象是否被标记为“保留”。对于新对象，默认值为true，对于旧对象默认值为false。
     *
     * @param retained 是否保留
     */
    public void setIsRetained(boolean retained) {
        if (retained && this.status == EObjectStatus.Deleted)
            return;
        this.isRetained = retained;
    }

    /**
     * 获取对象仓所属的的上下文
     *
     * @return 获取对象仓所属的的上下文
     */
    public ObjectContext getHostContext() {
        return this.objectContext;
    }

    /**
     * 获取一个值，该值指示对象仓中放置的对象是否为新创建的
     *
     * @return 对象仓中放置的对象是否为新创建的
     */
    public boolean getIsNew() {
        return this.status == EObjectStatus.Added;
    }

    /**
     * 获取一个值，该值指示对象仓中放置的对象是否被标记为已删除
     *
     * @return 对象仓中放置的对象是否被标记为已删除
     */
    public boolean getIsRemoved() {
        return this.status == EObjectStatus.Deleted;
    }

    /**
     * 获取对象仓中放置的对象
     *
     * @return 获取对象仓中放置的对象
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * 获取对象仓中放置的对象的标识
     *
     * @return 对象的标识
     */
    public ObjectKey getObjectKey() {
        if (this.objectKey == null)
            return ObjectSystemVisitor.getObjectKey(this.object, this.objectType);
        return this.objectKey;
    }

    /**
     * 获取对象仓中放置的对象的类型
     *
     * @return 对象的类型
     */
    public ObjectType getObjectType() {
        return (ObjectType) this.objectType;
    }

    /**
     * 如果对象不是根对象，将其标记为根对象
     */
    void overwriteRootTag() {
        this.asRoot = true;
    }

    /**
     * 通知介入者属性已更改。
     *
     * @param obj      发生属性更改的对象
     * @param attrName 发生更改的属性
     */
    @Override
    public void attributeChanged(Object obj, String attrName) {
        //无需实现
    }

    /**
     * 请求介入者加载关联
     * 对于实体对象，本方法将加载关联引用；对于关联对象则加载关联端
     *
     * @param obj           要加载关联的对象
     * @param referenceName 要加载的关联引用或关联端的名称
     */
    @Override
    public void loadAssociation(Object obj, String referenceName) {
        /////////
        //延迟加载关联引用（代理类重写属性的Get访问器实现，第一次访问关联引用属性或关联端的Get方法时执行本方法）
        /////////

        ReferenceElement refElement = this.getObjectType().getReferenceElement(referenceName);
        Object refValue = refElement.getValue(obj);
        if (refValue instanceof Iterable) {
            //如果没有值是个空集合 需要进行加载
            Iterable<Object> values = (Iterable<Object>) refValue;
            if (!values.iterator().hasNext())
                refValue = null;
        }
        if (refValue != null && refValue.getClass().isArray()) {
            // 获取数组长度 如果是0 则需要进行加载
            int length = java.lang.reflect.Array.getLength(refValue);
            if (length == 0)
                refValue = null;
        }

        if (refValue == null) {
            ObjectContext context = this.getHostContext();

            QueryOp query;
            Object[] objects = new Object[1];
            objects[0] = obj;
            if (refElement instanceof AssociationReference) {
                AssociationReference associationReference = (AssociationReference) refElement;
                query = associationReference.generateLoadingQuery(new Object[]{obj}, true, null);
                //拼接包含操作
                query = this.combineInclude(query, associationReference, obj);
            } else
                query = refElement.generateLoadingQuery(objects, null);

            QueryProvider queryProvider = context.getConfigProvider().getQueryProvider();


            Object refObjs = queryProvider.execute(query, null);
            if (refElement.getIsMultiple()) {
                Iterable<Object> objs = (Iterable<Object>) refObjs;
                List<Object> list = new ArrayList<>();
                for (Object o : objs) {
                    list.add(o);
                }
                refElement.setValue(obj, list);
            } else {
                for (Object o : (Iterable<Object>) refObjs) {
                    refElement.setValue(obj, o);
                }
            }
        }
    }

    /**
     * 拼接包含操作
     * 如果是使用了显式化的隐式关联型 或者 隐式多方关联 这种用取值器和设值器包装的 需要拼接一个包含操作
     *
     * @param queryOp              之前拼接的查询
     * @param associationReference 当前要加载的关联引用
     * @param host                 宿主对象
     * @return 合并后的运算
     */
    private QueryOp combineInclude(QueryOp queryOp, AssociationReference associationReference, Object host) {
        //是否需要增加包含操作
        Property prop = Utils.getProperty(host.getClass(), associationReference.getName());

        ObjectReferencePack<Class<?>> type = new ObjectReferencePack<>();
        Utils.getIsMultiple(prop, type);
        //如果定义的类型不是关联引用属性的类型 且 是一个显式关联型
        if (!type.realValue.equals(associationReference.getAssociationType().getClrType()) && associationReference.getAssociationType().getVisible()) {
            //取出不是自己的关联端
            List<AssociationEnd> ends = associationReference.getAssociationType().getAssociationEnds().stream()
                    .filter(p -> !p.getName().equalsIgnoreCase(associationReference.getLeftEnd())).collect(Collectors.toList());

            QueryOp includeOp = null;
            for (int i = 0; i < ends.size(); i++) {
                includeOp = QueryOp.include(ends.get(i).getName(), queryOp.getSourceType(), queryOp.getModel(), i == 0 ? queryOp : includeOp);
            }
            //拼接过 返回
            if (includeOp != null)
                return includeOp;
            return queryOp;
        }

        //相同 直接处理即可
        return queryOp;
    }

    /**
     * 接受针对对象所做的所有更改，将对象状态置为“未修改”，并重新对属性和关联引用建立快照。
     */
    public void acceptChanges() {

        //清空属性变更集合
        this.changedAttributes.clear();
        //清空属性快照
        this.propDic.clear();
        //代理对象会实现该接口
        if (this.object instanceof IIntervene) {
            IIntervene inter = (IIntervene) this.object;
            if (this.status.equals(EObjectStatus.Added)) inter.registerIntervener(this);
        }
        //将是否保留设置为False
        this.isRetained = false;
        //状态设置为未改变
        this.status = EObjectStatus.Unchanged;
        //暂时去掉不是代理对象的限制
        //if (!(object instanceof IIntervene))
        this.snapshotAttribute();

        //重新获取ObjectKey
        this.objectKey = ObjectSystemVisitor.getObjectKey(this.object, this.objectType);
    }

    /**
     * 属性变更探测，将属性的当前值与快照副本进行比对以确定该属性是否已修改。本方法适用于未实现IIntervene的接口。（不是代理类）
     */
    public void detectAttributesChange() {
        //不是代理对象并是未修改状态
        if (this.status == EObjectStatus.Unchanged) {
            //遍历属性和快照值对比
            for (Attribute attr : this.objectType.getAttributes()) {
                if (attr.getIsForeignKeyDefineMissing())
                    continue;
                //对象值
                Object obj = ObjectSystemVisitor.getValue(this.object, this.objectType, attr.getName());
                //和快照值对比
                if (!this.propDic.containsKey(attr.getName()) || !Objects.equals(this.propDic.get(attr.getName()), obj))
                    this.changedAttributes.add(attr.getName());
            }

            //有修改的属性
            if (!this.changedAttributes.isEmpty()) this.status = EObjectStatus.Modified;
        }
    }

    /**
     * 判定指定的属性是否已修改
     *
     * @param attrName 要判定的属性的名称
     * @return 是否已修改
     */
    public boolean judgeAttributeChange(String attrName) {
        //属性是否在修改属性集合中
        return this.changedAttributes.contains(attrName);
    }

    /**
     * 将对象放入对象仓
     *
     * @param obj        要放入对象仓的对象
     * @param objectType 对象的类型
     * @param isNew      指示对象是否为新创建的对象
     * @param asRoot     是否是根对象
     */
    public void putIn(Object obj, ObjectType objectType, boolean isNew, boolean asRoot) {
        this.asRoot = asRoot;
        this.object = obj;
        this.objectType = objectType;
        this.objectKey = ObjectSystemVisitor.getObjectKey(obj, objectType);

        if (isNew) {
            this.status = EObjectStatus.Added;
            this.isRetained = true;
            //判断是否为代理对象
            if (obj instanceof IIntervene) {
                ((IIntervene) obj).registerIntervener(this);
            }
            //新实体对象，标识属性是自增的 对象键置空
            if (objectType instanceof EntityType) {
                EntityType entityType = (EntityType) objectType;
                if (entityType.getKeyIsSelfIncreased()) {
                    this.objectKey = null;
                }
            }

            //新关联对象，任意一端的标识属性是自增的 对象键置空
            if (objectType instanceof AssociationType) {
                AssociationType associationType = (AssociationType) objectType;
                if (associationType.getAssociationEnds().stream().anyMatch(p -> p.getEntityType().getKeyIsSelfIncreased())) {
                    this.objectKey = null;
                }
            }

        } else {
            this.isRetained = false;
            this.status = EObjectStatus.Unchanged;
            //判断是否为代理对象
            if (obj instanceof IIntervene) {
                ((IIntervene) obj).registerIntervener(this);
            }
            //建立属性快照
            this.snapshotAttribute();
        }
    }

    /**
     * 将对象仓中的对象标记为已删除
     */
    public void remove() {
        this.isRetained = false;
        this.status = EObjectStatus.Deleted;

        if (this.objectType instanceof AssociationType) {
            AssociationType associationType = (AssociationType) this.objectType;
            //对于关联型 级联删除聚合的关联端
            this.cascadeDeleteAggregatedEnds(associationType);
        }
    }

    /**
     * 级联删除聚合的关联端
     *
     * @param associationType 关联型
     */
    private void cascadeDeleteAggregatedEnds(AssociationType associationType) {
        List<AssociationEnd> aggregatedEnds = associationType.getAggregatedEnds();
        //循环聚合端
        for (AssociationEnd end : aggregatedEnds) {
            //获取端对象
            Object endObj = end.getValue(this.object);
            if (endObj == null)
                continue;
            //是否已附加
            boolean attached = this.objectContext.attached(endObj);
            if (!attached) {
                //没附加 就附加
                this.objectContext.attach(endObj);
            }
            //一律移除
            this.objectContext.remove(endObj);
        }
    }

    /**
     * 对对象的所有属性建立快照副本
     */
    private void snapshotAttribute() {
        if (this.status == EObjectStatus.Unchanged)
            for (Attribute attr : this.objectType.getAttributes()) {
                if (attr.getIsForeignKeyDefineMissing())
                    continue;
                //获取值
                Object value = ObjectSystemVisitor.getValue(this.object, attr);
                //放入快照
                this.propDic.put(attr.getName(), value);
            }
    }

    /**
     * 使用指定的对象替换对象仓中的对象。
     * 实施替换操作须满足:
     * 原对象的状态为UnChanged 两个对象的键相等 两个对象均未实现IIntervene接口
     *
     * @param newObj 用于替换对象仓中的对象的新对象
     */
    public void replaceObject(Object newObj) {
        //如果是代理对象则不替换（代理对象重写了属性的Set方法，重写的Set方法会调用AttributeChanged修改状态为eObjectStatus.Modified）
        //AttributeChanged其实没有实现 此处都应检查
        //if (object instanceof IIntervene || newObj instanceof IIntervene) return;
        //如果是同一个对象 不进行操作
        if (this.object == newObj) return;
        if (this.status == EObjectStatus.Unchanged) {
            //获取对象的唯一标识
            Object newKey = ObjectSystemVisitor.getObjectKey(newObj, this.objectType);
            if (this.getObjectKey().equals(newKey))
                this.object = newObj;
            else
                throw new IllegalArgumentException("新对象的键必须与被替换的对象相等");
        } else {
            throw new IllegalArgumentException("对象已修改,不能被替换");
        }
    }

    /**
     * 获取对象指定属性的原值
     *
     * @param attribute 属性
     * @param parent    父属性
     * @return 对象指定属性的原值
     */
    public Object getAttributeOriginalValue(Attribute attribute, AttributePath parent) {
        Object parentObj = null;

        //是否为首节点
        boolean isFirst = true;

        if (parent != null) {
            for (Attribute pathNode : parent) {
                if (isFirst) {
                    String name = pathNode.getName();
                    parentObj = this.propDic.get(name);

                    //操作完成后翻转
                    isFirst = false;
                } else {
                    return this.getAttributeOriginalValue((Attribute) parentObj, parent);
                }
            }
        }

        if (parentObj == null) {
            String name = attribute.getName();
            return this.propDic.get(name);
        }

        return this.getAttributeOriginalValue((Attribute) parentObj, parent);
    }
}
