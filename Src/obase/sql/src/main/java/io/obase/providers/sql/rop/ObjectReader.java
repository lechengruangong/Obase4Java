/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：对象读取器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:38:53
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.objectSys.AssociationTree;
import io.obase.core.odm.objectSys.IAttachObject;
import io.obase.core.odm.objectSys.ObjectSystemBuilder;
import io.obase.providers.sql.ISqlExecutor;

import java.sql.ResultSet;

/**
 * 对象读取器，负责从结果集读取对象
 *
 * @param <T> 结果集中的对象的类型
 */
public class ObjectReader<T> extends ResultReader<T> {

    /**
     * 数据分配器
     */
    private final DataRowAssigner dataRowAssigner;

    /**
     * DataRowAssignment存储
     */
    private final DataRowAssignmentSet dataRowAssignmentSet;

    /**
     * 表示挂起的所有包含运算的关联树
     */
    private final AssociationTree includingTree;

    /**
     * 对象建造器
     */
    private final ObjectSystemBuilder objectSystemBuilder;

    /**
     * 对象的模型类型
     */
    private final ReferringType objectType;

    /**
     * 构造ObjectReader的新实例
     *
     * @param objectType    要读取的对象的模型类型
     * @param includingTree 包含所有挂起的包含运算的关联树
     * @param dataReader    据读取器，负责从数据库读取数据
     * @param attachObject  对象附加委托
     * @param type          结果类型
     * @param sqlExecutor   Sql执行器
     * @param attachRoot    是否作为根对象附加
     */
    public ObjectReader(ReferringType objectType, AssociationTree includingTree, ResultSet dataReader,
                        IAttachObject attachObject, Class<T> type, boolean attachRoot, ISqlExecutor sqlExecutor) {
        super(dataReader, type, sqlExecutor);
        this.objectType = objectType;
        this.includingTree = includingTree;
        this.dataRowAssignmentSet = new DataRowAssignmentSet();
        this.dataRowAssigner = new DataRowAssigner(this.dataRowAssignmentSet);
        this.objectSystemBuilder = new ObjectSystemBuilder(this.dataRowAssignmentSet, attachObject, attachRoot);
    }

    /**
     * 构造ObjectReader的新实例
     *
     * @param objectType    要读取的对象的模型类型
     * @param includingTree 包含所有挂起的包含运算的关联树
     * @param dataReader    据读取器，负责从数据库读取数据
     * @param attachObject  对象附加委托
     * @param type          结果类型
     * @param sqlExecutor   Sql执行器
     */
    public ObjectReader(ReferringType objectType, AssociationTree includingTree, ResultSet dataReader,
                        IAttachObject attachObject, Class<T> type, ISqlExecutor sqlExecutor) {
        this(objectType, includingTree, dataReader, attachObject, type, true, sqlExecutor);
    }

    /**
     * 构造ObjectReader的新实例
     *
     * @param objectType    要读取的对象的模型类型
     * @param includingTree 包含所有挂起的包含运算的关联树
     * @param dataReader    据读取器，负责从数据库读取数据
     * @param attachObject  对象附加委托
     * @param type          结果类型
     */
    public ObjectReader(ReferringType objectType, AssociationTree includingTree, ResultSet dataReader,
                        IAttachObject attachObject, Class<T> type) {
        this(objectType, includingTree, dataReader, attachObject, type, true, null);
    }

    /**
     * 获取要读取的对象的模型类型
     *
     * @return 对象的模型类型
     */
    public ReferringType getObjectType() {
        return this.objectType;
    }

    /**
     * 从结果集读取下一个元素（值或对象）
     *
     * @param result 返回读取结果
     * @return 读取成功返回true，否则返回false
     */
    @Override
    public boolean read(ObjectReferencePack<T> result) {
        T buildResult = null;
        boolean tagetReturn = false; //输出
        while (true) {
            //读取下一行
            DataRow dataRow = this.nextRow();

            if (dataRow == null && this.dataRowAssignmentSet.IsEmpty()) {
                //什么也没有
                result.realValue = null;
                return false;
            }

            if (dataRow == null && !this.dataRowAssignmentSet.IsEmpty()) {
                //建造对象
                result.realValue = (T) this.includingTree.accept(this.objectSystemBuilder);
                this.dataRowAssignmentSet.clear();
                return true;
            }

            if (dataRow != null) {
                if (!this.dataRowAssignmentSet.containEquivalent(this.includingTree.getNode(), dataRow) &&
                        !this.dataRowAssignmentSet.IsEmpty()) {
                    //建造对象
                    buildResult = (T) this.includingTree.accept(this.objectSystemBuilder);
                    this.dataRowAssignmentSet.clear();
                    tagetReturn = true;
                }

                this.dataRowAssigner.setDataRow(dataRow);
                this.includingTree.accept(this.dataRowAssigner);
            }

            if (dataRow == null || tagetReturn) {
                result.realValue = buildResult;
                return true;
            }
        }

    }
}
