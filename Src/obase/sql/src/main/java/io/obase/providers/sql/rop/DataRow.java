/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示数据库查询结果集中的一行.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-12 11:14:49
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;


import io.obase.core.common.Utils;
import io.obase.core.odm.IMappable;
import io.obase.core.odm.ObjectKey;
import io.obase.core.odm.ObjectKeyMember;
import io.obase.core.odm.ReferringType;
import io.obase.core.odm.objectSys.*;
import io.obase.providers.sql.AliasGenerator;
import io.obase.providers.sql.common.SqlAliasShortener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示数据库查询结果集中的一行，简称数据行。结果集各列在该行中的值称为域。
 */
public class DataRow {

    /**
     * 别名生成器
     */
    private final AliasGenerator aliasGenerator;

    /**
     * 存储域的字典，其值为域，键为该域对应的数据集列名
     */
    private final Map<String, Object> dataDict = new HashMap<>();

    /**
     * 行序字典
     */
    private final Map<Integer, String> rowIndexDict = new HashMap<>();

    /**
     * 映射字段生成器
     */
    private final TargetFieldGenerator targetFieldGenerator;

    /**
     * 创建DataRow实例
     *
     * @param aliasGenerator       别名生成器
     * @param targetFieldGenerator 映射字段生成器
     */
    public DataRow(AliasGenerator aliasGenerator, TargetFieldGenerator targetFieldGenerator) {
        this.aliasGenerator = aliasGenerator;
        this.targetFieldGenerator = targetFieldGenerator;
    }

    /**
     * 向数据行添加一个域
     *
     * @param columnName 列名
     * @param value      域
     */
    public void add(String columnName, Object value, int rowIndex) {
        columnName = columnName.toLowerCase();
        this.dataDict.put(columnName, value);
        this.rowIndexDict.put(rowIndex - 1, columnName);
    }

    /**
     * 获取基于数据行在指定关联树节点上创建的对象的标识
     *
     * @param treeNode 关联树节点
     * @return 对象的标识
     */
    public ObjectKey getObjectKey(AssociationTreeNode treeNode) {
        //获取节点类型
        ReferringType nodeType = treeNode.getRepresentedType();
        //作为根节点的树
        AssociationTree tree = treeNode.asTree();


        //KeyMember和KeyField是对应的
        int count = ((IMappable) nodeType).getKeyFields().size();
        List<String> keyField = ((IMappable) nodeType).getKeyFields();
        String[] keyMember = ((IMappable) nodeType).getKeyMemberNames();

        //key成员
        List<ObjectKeyMember> members = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            //别名
            String alias = tree.accept(this.aliasGenerator, keyField.get(i));

            //列名（与Sql发射侧一致 查表时优先尝试缩短后的列名）
            String columnName = Utils.getStringIsEmpty(alias) ? keyField.get(i) : alias;

            Object value = this.lookup(columnName);
            //是null 返回null
            if (value == null) return null;

            members.add(new ObjectKeyMember(keyMember[i], value));
        }

        return new ObjectKey(nodeType, members);
    }

    /**
     * 从数据行中获取指定简单属性的值
     *
     * @param attrNode  代表简单属性的属性树节点
     * @param assocNode 代表属性所属类型的关联树节点
     * @return 值
     */
    public Object getValue(SimpleAttributeNode attrNode, AssociationTreeNode assocNode) {
        //根节点树
        AttributeTree tree = attrNode.asTree();
        //目标字段
        String targetField = tree.accept(this.targetFieldGenerator);
        //列名
        String columnName = "";

        //处理关联树
        if (assocNode != null) {
            AssociationTree assTree = assocNode.asTree();
            columnName = assTree.accept(this.aliasGenerator, targetField);
        }

        //没有别名
        if (Utils.getStringIsEmpty(columnName)) columnName = targetField;

        //与Sql发射侧一致 查表时优先尝试缩短后的列名
        return this.lookup(columnName);
    }

    /**
     * 按索引号从数据行中获取域，索引号为域加入数据行的顺序
     *
     * @param columnIndex 索引号
     * @return 数据域的值
     */
    public Object getValue(int columnIndex) {
        String colName = this.rowIndexDict.get(columnIndex);
        //按照索引号返回
        return this.dataDict.get(colName);
    }

    /**
     * 按列名从数据行中获取域。
     * 与Sql发射侧保持一致：先尝试以"_obase_gen_alias+哈希"缩短后的列名查找（对应Sql中起别名的列），
     * 未命中再以原始列名查找（对应Sql中未起别名的列，数据库返回的是原始列名）。
     *
     * @param columnName 列名
     * @return 域
     */
    private Object lookup(String columnName) {
        if (Utils.getStringIsEmpty(columnName)) return this.dataDict.get(columnName);

        //优先尝试缩短后的列名
        String shortened = SqlAliasShortener.shorten(columnName).toLowerCase();
        if (!shortened.equals(columnName.toLowerCase()) && this.dataDict.containsKey(shortened))
            return this.dataDict.get(shortened);

        return this.dataDict.get(columnName.toLowerCase());
    }


    /**
     * 节点专门视图
     */
    public static class NodeSpecializedView implements IObjectData {

        /**
         * 作为视图依据的关联树节点
         */
        private final AssociationTreeNode node;

        /**
         * 作为视图源的数据行
         */
        private final DataRow sourceRow;

        /**
         * 创建NodeSpecializedView实例
         *
         * @param sourceRow 作为源的数据行
         * @param node      作为视图依据的关联树节点
         */
        public NodeSpecializedView(DataRow sourceRow, AssociationTreeNode node) {
            this.sourceRow = sourceRow;
            this.node = node;
        }

        /**
         * 获取指定属性树节点代表的简单属性的值
         *
         * @param attrNode 属性树节点
         * @return 值
         */
        @Override
        public Object getValue(SimpleAttributeNode attrNode) {
            return this.sourceRow.getValue(attrNode, this.node);
        }

        /**
         * 获取对象标识
         *
         * @return 对象标识
         */
        @Override
        public ObjectKey getObjectKey() {
            return this.sourceRow.getObjectKey(this.node);
        }
    }
}
