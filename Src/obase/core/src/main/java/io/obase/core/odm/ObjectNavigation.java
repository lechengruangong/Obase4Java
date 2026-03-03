/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象导航行为.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 12:07:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.core.common.Utils;

import java.util.Optional;

/**
 * 描述对象导航行为。
 * 基于特定的关联，可以从一个对象转移到另一个对象，这个过程称为导航。
 * 有两种类型的导航。一种是间接导航，即借助于关联对象，先从源对象转移到关联对象，然后再转移到目标对象。另一种是直接导航，即从源对象直接转移到目标对象。
 * 不管哪种导航都必须基于特定的关联，而导航总是发生在两个关联端之间。基于这一理解，源对象可称为源端，目标对象则可称为目标端。
 * 在物理层面上看，导航需要借助引用元素（即对象内部的指针）来实现。直接导航需要在源对象定义一个指向目标对象的关联引用，称为直接引用。间接导航则需要两个引用元素，分
 * 别为定义在源对象的关联引用和定义在关联对象的关联端。前者指向关联对象，称为发出引用；后者指向目标对象，称为到达引用。
 */
public class ObjectNavigation {

    /**
     * 作为导航依据的关联型
     */
    private final AssociationType associationType;

    /**
     * 导航类型
     */
    private final ENavigationType navigationType;

    /**
     * 源端名，即作为源端的关联端的名称。值为null表示源端不明确。
     */
    private final String sourceEndName;

    /**
     * 目标端名，即作为目标端的关联端的名称。值为null表示目标端不明确。
     */
    private final String targetEndName;
    /**
     * 目标对象类型。目标端不明确时返回null
     */
    private final ObjectType targetType;

    /**
     * 源端
     */
    private AssociationEnd sourceEnd;
    /**
     * 目标端
     */
    private AssociationEnd targetEnd;

    /**
     * 创建表示导航的ObjectNavigation实例，指定源端名称和目标端名称
     *
     * @param asoType 关联型
     * @param source  源端名称。值为null表示源端未明确的间接导航。source与target不能同时为null。
     * @param target  目标端名称。值为null表示目标端未明确的间接导航。source与target不能同时为null。
     */
    public ObjectNavigation(AssociationType asoType, String source, String target) {
        this.associationType = asoType;
        this.sourceEndName = source;
        this.targetEndName = target;

        this.navigationType = asoType.getVisible() ? ENavigationType.Indirectly : ENavigationType.Directly;

        /*源端*/
        if (!Utils.getStringIsEmpty(source)) {
            Optional<AssociationEnd> optional = asoType.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(source)).findFirst();
            optional.ifPresent(associationEnd -> this.sourceEnd = associationEnd);
        }


        /*目标端*/
        if (!Utils.getStringIsEmpty(target)) {
            Optional<AssociationEnd> optional = asoType.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(target)).findFirst();
            if (optional.isPresent()) {
                this.targetEnd = optional.get();
                this.targetType = optional.get().getReferenceType();
            } else {
                Optional<AssociationEnd> optional1 = asoType.getAssociationEnds().stream().filter(p -> p.getName().equalsIgnoreCase(target)).findFirst();
                if (optional1.isPresent()) {
                    this.targetEnd = optional1.get();
                    this.targetType = optional1.get().getReferenceType();
                } else {
                    this.targetType = null;
                }
            }
        } else {
            this.targetType = null;
        }
    }

    /**
     * 获取作为导航依据的关联型
     *
     * @return 获取作为导航依据的关联型
     */
    public AssociationType getAssociationType() {
        return this.associationType;
    }

    /**
     * 获取导航类型
     *
     * @return 获取导航类型
     */
    public ENavigationType getNavigationType() {
        return this.navigationType;
    }

    /**
     * 获取源端，值为null表示源端不明确
     *
     * @return 获取源端
     */
    public AssociationEnd getSourceEnd() {
        return this.sourceEnd;
    }

    /**
     * 获取源端名，即作为源端的关联端的名称。值为null表示源端不明确。
     *
     * @return 源端名
     */
    public String getSourceEndName() {
        return this.sourceEndName;
    }

    /**
     * 获取目标端，值为null表示目标端不明确
     *
     * @return 目标端
     */
    public AssociationEnd getTargetEnd() {
        return this.targetEnd;
    }

    /**
     * 获取目标端名，即作为目标端的关联端的名称。值为null表示目标端不明确。
     *
     * @return 获取目标端名，即作为目标端的关联端的名称。值为null表示目标端不明确。
     */
    public String getTargetEndName() {
        return this.targetEndName;
    }

    /**
     * 获取目标对象类型。目标端不明确时返回null。
     *
     * @return 获取目标对象类型。目标端不明确时返回null。
     */
    public ObjectType getTargetType() {
        return this.targetType;
    }
}
