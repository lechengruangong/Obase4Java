/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：源联接器核心.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 12:23:15
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;
import io.obase.core.saving.IHeredityMapper;
import io.obase.providers.sql.sqlobject.*;

import java.util.List;

/**
 * 源联接器核心。
 * 对象之间存在关联关系，基于关联可以在对象对应的源之间执行联接操作。假设存在两个实体型A和B，其关联型为AB，也就是说，A和B分别对应关联型AB的两个关联端。如果
 * 要联接A和B对应的源，可采取两步操作：首先基于A的源联接AB的源，然后再联接B的源。由此可见，每次执行联接操作的根本依据都是某一关联型及其某个关联端。
 * 为表述方便，我们将关联型对应的源简称为关联源，将关联端对应的源简称为端源。通常情况下，关联源和端源分别是关联型和关联端所对应实体型的映射源（SimpleSour
 * ce）。
 * 对象投影运算（Select）会生成一个类型视图，投影运算结果是一个SelectSource,
 * 它可以视为该视图的映射源进入运算管道，参与后续运算。后续运算如果需要以视图源或某一扩展节点为依据实施联接运算，那么视图的映射源（SelectSource）就成为
 * 关联源或端源。由于视图的映射源是对视图源及扩展的映射源实施查询运算生成的，因此可以把它称为衍生源，相对地，将视图源及其扩展的映射源称为母体源。衍生源的字段全部来
 * 自于母源，但字段名称通常会发生变化（一般是为了规避重名），我们把这种衍生于母体又产生名称变化的形象称为遗传映射。在联接操作中，我们只需要关注标识成员映射字段的遗
 * 传映射。
 * 源联接的核心任务就是在关联源与端源之间执行联接操作。有两种基本的联接方式，一是从关联端联接到关联型，二是从关联型联接到关联端。
 */
public class SourceJoinerCore {

    /**
     * 当AssociationSource为衍生源时，指定其遗传映射器。
     */
    private IHeredityMapper associationHeredityMapper;

    /**
     * 关联源，可能为映射源，也可能为其衍生源。从关联端联接关联型时，该源为目标源；从关联型联接到关联端时，该源为基源。
     */
    private MonomerSource associationSource;

    /**
     * 作为连接依据的关联型
     */
    private AssociationType associationType;

    /**
     * 联接类型
     */
    private ESourceJoinType joinType;

    /**
     * 返回适用于关联源的遗传映射器
     *
     * @return 遗传映射器
     */
    public IHeredityMapper getAssociationHeredityMapper() {
        return this.associationHeredityMapper;
    }

    /**
     * 获取关联源
     *
     * @return 关联源
     */
    public MonomerSource getAssociationSource() {
        return this.associationSource;
    }

    /**
     * 获取作为联接依据的关联型
     *
     * @return 联接依据的关联型
     */
    public AssociationType getAssociationType() {
        return this.associationType;
    }

    /**
     * 获取联接类型
     *
     * @return 联接类型
     */
    public ESourceJoinType getJoinType() {
        return this.joinType;
    }

    /**
     * 设置联接类型
     *
     * @param joinType 联接类型
     */
    public void setJoinType(ESourceJoinType joinType) {
        this.joinType = joinType;
    }

    /**
     * 更换联接时使用的关联源，可以同时更换其遗传映射器。
     *
     * @param assocSource    新的关联源，可以是映射源也可以是衍生源
     * @param heredityMapper 关联源为衍生源时指定遗传映射器
     */
    public void changeSource(MonomerSource assocSource, IHeredityMapper heredityMapper) {
        this.associationSource = assocSource;
        this.associationHeredityMapper = heredityMapper;
    }

    /**
     * 配置源联接器核心
     *
     * @param assocType      作为联接依据的关联型
     * @param assocSource    关联源。值为null时表示不指定关联源，联接时将使用映射源且无别名
     * @param heredityMapper 当关联源为衍生源时指定其遗传映射器。值为null表示不需要使用遗传映射器
     */
    public void config(AssociationType assocType, MonomerSource assocSource, IHeredityMapper heredityMapper) {
        this.associationType = assocType;
        this.associationSource = assocSource;
        this.associationHeredityMapper = heredityMapper;
    }

    /**
     * 配置联接器核心，该核心实施联接操作时将自动使用关联型的映射源。
     *
     * @param assocType   作为联接依据的关联型
     * @param sourceAlias 指定关联映射源的别名
     */
    public void config(AssociationType assocType, String sourceAlias) {
        this.associationType = assocType;
        this.associationSource = new SimpleSource(assocType.getTargetTable(), sourceAlias);
        this.associationHeredityMapper = null;
    }

    /**
     * 从指定关联端源联接关联源或从关联源联接指定关联端源时，判定联接操作是否应当执行
     *
     * @param assocEnd 关联端
     * @return 是否应当执行
     */
    public boolean shouldJoin(AssociationEnd assocEnd) {
        return !this.associationType.isCompanionEnd(assocEnd);
    }

    /**
     * 从指定关联端源联接关联源或从关联源联接指定关联端源时，判定联接操作是否应当执行。
     *
     * @param endName 关联端名称
     * @return 是否应当执行
     */
    public boolean shouldJoin(String endName) {
        return !this.associationType.isCompanionEnd(endName);
    }

    /**
     * 从指定关联端的源联接关联的源
     *
     * @param endName           关系端的名称
     * @param endHeredityMapper 端源的遗传映射器
     * @param baseSource        基源
     * @param leftSource        左操作数
     * @return 联接关联的源
     */
    public JoinedSource fromEnd(String endName, IHeredityMapper endHeredityMapper, MonomerSource baseSource, ISource leftSource) {
        AssociationEnd assoEnd = this.associationType.getAssociationEnd(endName);
        return this.fromEnd(assoEnd, baseSource, leftSource, endHeredityMapper);
    }

    /**
     * 从指定关联端的源联接关联的源
     *
     * @param assocEnd          关系端
     * @param baseSource        基源
     * @param leftSource        左操作数
     * @param endHeredityMapper 端源的遗传映射器
     * @return 联接关联的源
     */
    public JoinedSource fromEnd(AssociationEnd assocEnd, MonomerSource baseSource, ISource leftSource, IHeredityMapper endHeredityMapper) {
        //基源为空
        if (baseSource == null) {
            //关联端做基源
            EntityType entityType = assocEnd.getEntityType();
            String endTable = entityType.getTargetTable();
            baseSource = new SimpleSource(endTable);
        }

        //构造条件
        ICriteria criteria = this.generateCriteria(assocEnd, baseSource, endHeredityMapper);
        //无左操作数 则基源做做操作数
        if (leftSource == null)
            leftSource = baseSource;
        //联接后的源
        return new JoinedSource(leftSource, this.associationSource, criteria, this.joinType);
    }

    /**
     * 从指定关联的源联接关联端的源
     *
     * @param endName      关系端的名称
     * @param targetAlias  目标源的别名
     * @param targetSource 返回联接操作中生成的目标源
     * @param leftSource   左操作数
     * @return 联接关联端的源
     */
    public JoinedSource toEnd(String endName, String targetAlias, ObjectReferencePack<SimpleSource> targetSource,
                              ISource leftSource) {
        AssociationEnd assocEnd = this.associationType.getAssociationEnd(endName);
        return this.toEnd(assocEnd, targetAlias, leftSource, targetSource);
    }

    /**
     * 从指定关联的源联接关联端的源
     *
     * @param assocEnd     关系端
     * @param targetAlias  目标源别名
     * @param leftSource   左操作数
     * @param targetSource 返回联接操作中生成的目标源
     * @return 联接关联端的源
     */
    public JoinedSource toEnd(AssociationEnd assocEnd, String targetAlias, ISource leftSource, ObjectReferencePack<SimpleSource> targetSource) {
        //无左操作数 则关联源做做操作数
        if (leftSource == null)
            leftSource = this.associationSource;
        //根据关联端做目标源
        EntityType entityType = assocEnd.getEntityType();
        String endTable = entityType.getTargetTable();
        targetSource.realValue = new SimpleSource(endTable, targetAlias);

        //构造条件
        ICriteria criteria = this.generateCriteria(assocEnd, targetSource.realValue, null);
        //联接后的源
        return new JoinedSource(leftSource, targetSource.realValue, criteria, this.joinType);
    }

    /**
     * 生成联接条件
     *
     * @param assocEnd          关联端
     * @param endSource         端源
     * @param endHeredityMapper 端源的遗传映射器
     * @return 联接条件
     */
    private ICriteria generateCriteria(AssociationEnd assocEnd, MonomerSource endSource, IHeredityMapper endHeredityMapper) {
        //此端的类型
        EntityType endType = assocEnd.getEntityType();
        //处理每个映射
        List<AssociationEndMapping> mappings = assocEnd.getMappings();

        //最终条件
        ICriteria result = null;

        for (AssociationEndMapping mapping : mappings) {
            //映射字段
            String mappingTarget = mapping.getTargetField();
            if (this.associationHeredityMapper != null)
                mappingTarget = this.associationHeredityMapper.map(mappingTarget);
            Field mappingField = new Field(this.associationSource, mappingTarget);
            //映射的标识属性
            Attribute keyAttr = endType.getAttribute(mapping.getKeyAttribute());
            //映射标识属性字段
            String keyAttrTarget = keyAttr.getTargetField();
            if (endHeredityMapper != null)
                keyAttrTarget = endHeredityMapper.map(keyAttrTarget);
            Field endField = new Field(endSource, keyAttrTarget);

            //构造表达式
            FieldExpression mappingFieldExp = Expression.field(mappingField);
            FieldExpression endFieldExp = Expression.field(endField);
            ComparisonExpression criteriaExp = Expression.equal(mappingFieldExp, endFieldExp);
            //包装为表达式条件
            ExpressionCriteria segment = new ExpressionCriteria(criteriaExp);
            //与最终条件联接
            result = result == null ? segment : result.and(segment);
        }

        return result;
    }
}
