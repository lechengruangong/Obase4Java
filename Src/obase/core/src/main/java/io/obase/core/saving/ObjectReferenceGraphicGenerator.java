/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象参照图生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-11 16:58:14
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.saving;

import io.obase.core.common.Utils;
import io.obase.core.odm.AssociationEnd;
import io.obase.core.odm.AssociationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * 对象参照图生成器
 */
public class ObjectReferenceGraphicGenerator {

    /**
     * 分析关联对象。将关联对象加入对象参照图，并对各关联端进行分析
     *
     * @param associationObj  要分析的关联对象
     * @param associationType 关联对象的类型
     * @param isSaving        是否存在添加对象集合中的委托
     * @param graphic         要生成的对象参照图
     */
    public void analyzeAssociation(Object associationObj, AssociationType associationType,
                                   Predicate<Object> isSaving, ObjectReferenceGraphic graphic) {

        List<AssociationEnd> ends = associationType.getAssociationEnds();
        List<Object> endObjs = new ArrayList<>();
        if (ends == null || ends.size() == 0) return;
        Object hostObj = null;
        //遍历关联端
        for (AssociationEnd item : ends) {
            //获取端对象
            Object endObj = ObjectSystemVisitor.getValue(associationObj, item);
            if (endObj != null && isSaving.test(endObj)) //委托判断是否存在添加对象集合中
            {
                //是否为伴随端  含基类（伴随映射才有伴随端）
                if (Objects.equals(item.getEntityType().getTargetTable(), associationType.getTargetTable())
                        || Objects.equals(Utils.getDerivedTargetTable(item.getEntityType()), associationType.getTargetTable()))
                    hostObj = endObj;

                //排除伴随对象  含基类(如果是伴随对象则不添加到关联参照对象集合)
                if (!Objects.equals(item.getEntityType().getTargetTable(), associationType.getTargetTable())
                        && !Objects.equals(Utils.getDerivedTargetTable(item.getEntityType()), associationType.getTargetTable()))
                    endObjs.add(endObj);
            }
        }

        //如果每一端的目标表均与关联表相同 则此时为自关联
        if (ends.stream().allMatch(item -> Objects.equals(item.getEntityType().getTargetTable(), associationType.getTargetTable())
                || Objects.equals(Utils.getDerivedTargetTable(item.getEntityType()), associationType.getTargetTable()))) {
            //自关联中 如果存在伴随端 肯定是自己
            if (associationType.getCompanionEnd() != null)
                hostObj = ObjectSystemVisitor.getValue(associationObj, associationType.getCompanionEnd());
        }

        //图中不存在
        if (!graphic.exists(associationObj)) {
            //独立映射
            if (associationType.getIndependent())
                graphic.addHost(associationObj, endObjs.toArray());
                //伴随映射
            else
                graphic.addCompanion(associationObj, endObjs.toArray(), hostObj);
        }
    }

    /**
     * 分析实体对象。将实体对象加入对象参照图，并导航到各关联对象、分析这些关联对象。
     *
     * @param entityObj 要分析的实体对象
     * @param isSaving  一个委托，用于检查传入的对象是否为正在执行保存操作的对象，如果是返回true。第一个参数为传入的对象，第二个参数为返回值。
     * @param graphic   要生成的对象参照图
     */
    public void analyzeObject(Object entityObj, Predicate<Object> isSaving,
                              ObjectReferenceGraphic graphic) {
        if (!graphic.exists(entityObj) && isSaving.test(entityObj)) //对象不存在图中并且是要保存的对象
            graphic.addHost(entityObj);
    }
}

