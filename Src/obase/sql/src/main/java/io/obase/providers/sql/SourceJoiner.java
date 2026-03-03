/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：源联接器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 12:16:50
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;
import io.obase.core.odm.*;
import io.obase.core.odm.objectSys.AssociationTreeNodeAliasGenerator;
import io.obase.core.odm.typeviews.SelfReference;
import io.obase.core.odm.typeviews.TypeView;
import io.obase.core.odm.typeviews.ViewReference;
import io.obase.core.saving.IHeredityMapper;
import io.obase.providers.sql.sqlobject.*;

/**
 * 源联接器，以关联树的某一节点为基础向其某一子节点发起源联接操作。
 * 该节点称为基节点，其代表的类型称为基型，基型对应的源称为基源。子节点称为目标节点，其代表的类型称为目标型，目标型的源称为目标源。
 * 如果子节点代表关联引用，由于关联引用总是指向关联型，而其宿主类型作为该关联型的一端，所以这种联接属于从关联端联接到关联型。其中，关联端的实体型为基型，关联型称为
 * 目标型。
 * 如果子节点代表关联端，由于关联端的宿主类型总是关联型，所以这种联接属于从关联型联接到关联端。其中，关联型为基型，关联端指向实体型称为目标型。
 * 如果子节点代表视图引用，该依据此引用最终绑定的类型元素确定联接方案。
 * 默认使用基源作为联接操作的左操作数。调用方亦可显式指定左操作数源，但该源必须等效于或逻辑蕴含基源。
 */
public class SourceJoiner {

    /**
     * 基源，可能为对象型的映射源，也可能为其衍生源。
     */
    private final MonomerSource baseSource;

    /**
     * 源联接器内核
     */
    private final SourceJoinerCore core = new SourceJoinerCore();

    /**
     * 基型，即关联引用或关联端所属的类型。
     */
    private final ReferringType hostType;

    /**
     * 基节点的别名
     */
    private final String nodeAlias;

    /**
     * 当基型为类型视图时，为其映射源提供遗传映射机制
     */
    private final TypeViewHeredityMapper typeViewHeredityMapper = new TypeViewHeredityMapper();

    /**
     * 作为联接操作左操作数的源
     */
    private ISource leftSource;

    /**
     * 创建SourceJoiner实例，联接时使用指定的基源和基节点别名
     *
     * @param hostType   关联引用或关联端所属的对象型
     * @param baseSource 基源
     * @param nodeAlias  宿主类型对应的关联树节点的别名
     * @param leftSource 作为左操作数的源
     */
    public SourceJoiner(ReferringType hostType, MonomerSource baseSource, String nodeAlias,
                        ISource leftSource) {
        this.hostType = hostType;
        this.nodeAlias = nodeAlias;
        this.leftSource = leftSource;

        if (baseSource == null) {
            if (hostType instanceof ObjectType) {
                ObjectType objectType = (ObjectType) hostType;
                baseSource = new SimpleSource(objectType.getTargetTable(), nodeAlias);
            } else if (hostType instanceof TypeView) {
                TypeView typeView = (TypeView) hostType;
                baseSource = new SimpleSource(typeView.getTargetName(), nodeAlias);
            }
        }

        this.baseSource = baseSource;
    }

    /**
     * 获取基型
     *
     * @return 基型
     */
    public ReferringType getHostType() {
        return this.hostType;
    }

    /**
     * 获取作为联接操作左操作数的源
     *
     * @return 作为联接操作左操作数的源
     */
    public ISource getLeftSource() {
        return this.leftSource;
    }

    /**
     * 设置作为联接操作左操作数的源
     *
     * @param leftSource 作为联接操作左操作数的源
     */
    public void setLeftSource(ISource leftSource) {
        this.leftSource = leftSource;
    }

    /**
     * 获取基节点的别名
     *
     * @return 基节点的别名
     */
    public String getNodeAlias() {
        return this.nodeAlias;
    }

    /**
     * 生成目标节点的别名
     *
     * @param element 指向目标节点的引用元素
     * @return 目标节点的别名
     */
    private String generateAlias(ReferenceElement element) {
        return AssociationTreeNodeAliasGenerator.generateAlias(element, this.getNodeAlias());
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param elementName 指向目标型的引用元素的名称
     * @param joinType    Join运算类型
     * @return 联接后的源
     */
    public ISource join(String elementName, ESourceJoinType joinType) {
        ReferenceElement element = this.hostType.getReferenceElement(elementName);
        return this.join(element, joinType);
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param element  指向目标型的引用元素
     * @param joinType Join运算类型
     * @return 联接后的源
     */
    public ISource join(ReferenceElement element, ESourceJoinType joinType) {
        return this.join(element, new ObjectReferencePack<>(), new ObjectReferencePack<>(), joinType);
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param elementName     指向目标型的引用元素的名称
     * @param targetSource    返回联接操作生成的目标源。如果不应当联接，则返回基源
     * @param targetNodeAlias 返回目标节点的别名
     * @param joinType        Join运算类型
     * @return 联接后的源
     */
    public ISource join(String elementName, ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias, ESourceJoinType joinType) {
        ReferenceElement element = this.hostType.getReferenceElement(elementName);
        return this.join(element, targetSource, targetNodeAlias, joinType);
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param element         指向目标型的引用元素
     * @param targetSource    返回联接操作生成的目标源。如果不应当联接，则返回基源
     * @param targetNodeAlias 返回目标节点的别名
     * @param joinType        Join运算类型
     * @return 联接后的源
     */
    public ISource join(ReferenceElement element, ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias, ESourceJoinType joinType) {
        if (element instanceof AssociationEnd) {
            AssociationEnd associationEnd = (AssociationEnd) element;
            targetNodeAlias.realValue = null;
            return this.join(associationEnd, targetSource, targetNodeAlias, null, joinType);
        }

        if (element instanceof AssociationReference) {
            AssociationReference associationReference = (AssociationReference) element;
            return this.join(associationReference, targetSource, targetNodeAlias, null, joinType);
        }

        if (element instanceof SelfReference) {
            SelfReference selfReference = (SelfReference) element;
            return this.join(selfReference, targetSource, targetNodeAlias, joinType);
        }

        if (element instanceof ViewReference) {
            ViewReference viewReference = (ViewReference) element;
            return this.join(viewReference, targetSource, targetNodeAlias, joinType);
        }

        //保底 不可能走到这
        targetSource.realValue = null;
        targetNodeAlias.realValue = null;
        return null;
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param assocRef           指向目标型的关联引用
     * @param targetSource       Join运算类型
     * @param targetNodeAlias    适用于基源的遗传映射器
     * @param baseHeredityMapper 返回联接操作生成的目标源。如果不应当联接，则返回基源
     * @param joinType           返回目标节点的别名
     * @return 联接后的源
     */
    private ISource join(AssociationReference assocRef, ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias,
                         IHeredityMapper baseHeredityMapper, ESourceJoinType joinType) {
        //别名
        if (Utils.getStringIsEmpty(targetNodeAlias.realValue))
            targetNodeAlias.realValue = this.generateAlias(assocRef);
        //配置连接核心
        this.core.config(assocRef.getAssociationType(), targetNodeAlias.realValue);
        //目标源
        targetSource.realValue = this.core.getAssociationSource();
        //关联左端
        AssociationEnd assocEnd = assocRef.getAssociationType().getAssociationEnd(assocRef.getLeftEnd());
        //联接
        if (this.core.shouldJoin(assocEnd)) {
            this.core.setJoinType(joinType);
            return this.core.fromEnd(assocEnd, this.baseSource, this.leftSource, baseHeredityMapper);
        }

        targetSource.realValue = this.baseSource;
        if (this.leftSource == null) {
            return this.baseSource;
        }
        return this.leftSource;
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param assocEnd           指向目标型的关联端
     * @param targetSource       返回联接操作生成的目标源。如果不应当联接，则返回基源
     * @param targetNodeAlias    返回目标节点的别名
     * @param baseHeredityMapper 适用于基源的遗传映射器
     * @param joinType           Join运算类型
     * @return 联接后的源
     */
    private ISource join(AssociationEnd assocEnd,
                         ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias,
                         IHeredityMapper baseHeredityMapper, ESourceJoinType joinType) {
        //别名
        if (Utils.getStringIsEmpty(targetNodeAlias.realValue))
            targetNodeAlias.realValue = this.generateAlias(assocEnd);

        if (assocEnd.getHostType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) assocEnd.getHostType();

            this.core.config(associationType, this.baseSource, baseHeredityMapper);
            if (this.core.shouldJoin(assocEnd)) {
                this.core.setJoinType(joinType);
                ObjectReferencePack<SimpleSource> simpleSource = new ObjectReferencePack<>();
                JoinedSource result = this.core.toEnd(assocEnd, targetNodeAlias.realValue, this.leftSource, simpleSource);
                targetSource.realValue = simpleSource.realValue;
                return result;
            }

            targetSource.realValue = this.baseSource;
            if (this.leftSource == null) {
                return this.baseSource;
            }
            return this.leftSource;
        }

        //保底 不可能走到这
        targetSource.realValue = null;
        targetNodeAlias.realValue = null;
        return null;
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param viewRef         指向目标型的视图引用
     * @param targetSource    返回联接操作生成的目标源。如果不应当联接，则返回基源
     * @param targetNodeAlias 返回目标节点的别名
     * @param joinType        Join运算类型
     * @return 联接后的源
     */
    private ISource join(ViewReference viewRef, ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias, ESourceJoinType joinType) {

        ReferenceElement ref = viewRef.getFinalBinding();
        targetNodeAlias.realValue = this.generateAlias(viewRef);
        //配置遗传映射机制
        this.typeViewHeredityMapper.setJoinReference(viewRef);

        //关联端和关联引用
        if (ref instanceof AssociationReference) {
            AssociationReference associationReference = (AssociationReference) ref;
            return this.join(associationReference, targetSource, targetNodeAlias, this.typeViewHeredityMapper,
                    joinType);
        }


        if (ref instanceof AssociationEnd) {
            AssociationEnd associationEnd = (AssociationEnd) ref;
            return this.join(associationEnd, targetSource, targetNodeAlias, this.typeViewHeredityMapper, joinType);
        }


        //保底 不可能走到这
        targetSource.realValue = null;
        targetNodeAlias.realValue = null;
        return null;
    }

    /**
     * 向引用元素指向的目标型发起源联接操作
     *
     * @param selfRef         指向目标型的反身引用
     * @param targetSource    返回联接操作生成的目标源。（总是返回基源）
     * @param targetNodeAlias 返回目标节点的别名。（总是返回基节点的别名）
     * @param joinType        Join运算类型
     * @return 联接后的源
     */
    private ISource join(SelfReference selfRef, ObjectReferencePack<MonomerSource> targetSource, ObjectReferencePack<String> targetNodeAlias, ESourceJoinType joinType) {
        targetSource.realValue = this.baseSource;
        targetNodeAlias.realValue = this.nodeAlias;
        return this.baseSource;
    }

    /**
     * 向引用元素指向的目标型发起联接时，判定源联接操作是否应当执行。
     *
     * @param elementName 指向目标型的引用元素的名称
     * @return 是否应当执行
     */
    public boolean shouldJoin(String elementName) {
        ReferenceElement element = this.hostType.getReferenceElement(elementName);
        return this.shouldJoin(element);
    }

    /**
     * 向引用元素指向的目标型发起联接时，判定源联接操作是否应当执行
     *
     * @param element 指向目标型的引用元素
     * @return 是否应当执行
     */
    public boolean shouldJoin(ReferenceElement element) {
        if (element instanceof AssociationEnd) {
            AssociationEnd associationEnd = (AssociationEnd) element;
            return this.shouldJoin(associationEnd);
        }

        if (element instanceof AssociationReference) {
            AssociationReference associationReference = (AssociationReference) element;
            return this.shouldJoin(associationReference);
        }
        if (element instanceof SelfReference) {
            SelfReference selfReference = (SelfReference) element;
            return this.shouldJoin(selfReference);
        }
        if (element instanceof ViewReference) {
            ViewReference viewReference = (ViewReference) element;
            return this.shouldJoin(viewReference);
        }

        return false;
    }

    /**
     * 向关联引用指向的目标型发起联接时，判定源联接操作是否应当执行。
     *
     * @param assocRef 指向目标型的关联引用
     * @return 应当执行返回true，否则返回false
     */
    private boolean shouldJoin(AssociationReference assocRef) {
        AssociationType assocType = assocRef.getAssociationType();
        this.core.config(assocType, null);
        AssociationEnd assocEnd = assocType.getAssociationEnd(assocRef.getLeftEnd());
        return this.core.shouldJoin(assocEnd);
    }

    /**
     * 向关联端指向的目标型发起联接时，判定源联接操作是否应当执行
     *
     * @param assocEnd 指向目标型的关联端
     * @return 是否应当执行
     */
    private boolean shouldJoin(AssociationEnd assocEnd) {
        if (assocEnd.getHostType() instanceof AssociationType) {
            AssociationType associationType = (AssociationType) assocEnd.getHostType();
            this.core.config(associationType, null);
            return this.core.shouldJoin(assocEnd);
        }

        return false;
    }

    /**
     * 向视图引用指向的目标型发起联接时，判定源联接操作是否应当执行。
     *
     * @param viewRef 指向目标型的视图引用
     * @return 应当执行返回true，否则返回false
     */
    private boolean shouldJoin(ViewReference viewRef) {
        return true;
    }

    /**
     * 向反身引用指向的目标型发起联接时，判定源联接操作是否应当执行。（总是返回false）
     *
     * @param selfRef 指向目标型的反身引用
     * @return 应当执行返回true，否则返回false
     */
    private boolean shouldJoin(SelfReference selfRef) {
        return false;
    }
}
