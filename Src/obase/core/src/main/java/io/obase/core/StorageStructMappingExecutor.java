/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：存储结构映射执行器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-6-30 16:19:08
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;
import io.obase.core.odm.builder.IStructMappingExecutor;
import io.obase.core.odm.objectSys.AttributeTree;
import io.obase.core.odm.objectSys.AttributeTreeNode;
import io.obase.core.odm.objectSys.IAttributeTreeDownwardVisitorWithResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 存储结构映射执行器，将模型结构映射为存储数据结构。
 */
public class StorageStructMappingExecutor implements IStructMappingExecutor {

    /**
     * 根据存储符号创建映射提供程序的委托
     */
    private final FunctionWithOneArg<StorageSymbol, IStorageStructMappingProvider> mappingProviderCreator;

    /**
     * 构造存储结构映射执行器
     *
     * @param mappingProviderCreator 根据存储符号创建映射提供程序的委托
     */
    public StorageStructMappingExecutor(FunctionWithOneArg<StorageSymbol, IStorageStructMappingProvider> mappingProviderCreator) {
        this.mappingProviderCreator = mappingProviderCreator;
    }

    /**
     * 执行结构映射
     *
     * @param model 对象数据模型
     */
    @Override
    public void execute(ObjectDataModel model) {
        StorageSymbol storageSymbol = model.getStorageSymbol();
        IStorageStructMappingProvider provider = this.mappingProviderCreator.invoke(storageSymbol);

        if (provider == null)
            return;

        List<StructuralType> types = new ArrayList<>();
        //分开处理
        types.addAll(model.getTypes().stream().filter(p -> p instanceof EntityType).collect(Collectors.toList()));
        types.addAll(model.getTypes().stream().filter(p -> p instanceof AssociationType).collect(Collectors.toList()));

        //具体的映射逻辑
        for (StructuralType type : types) {
            try {
                if (type instanceof EntityType) {
                    EntityType entityType = (EntityType) type;
                    List<String> keyAttrs = entityType.getKeyAttributes();
                    List<Field> attrFields = this.mapAttribute(entityType);
                    this.ensureTable(entityType.getTargetTable(), keyAttrs.toArray(new String[0]), attrFields.toArray(new Field[0]), provider);
                } else if (type instanceof AssociationType) {
                    AssociationType associationType = (AssociationType) type;
                    List<Field> attrFields = this.mapAttribute(associationType);
                    //独立映射
                    if (associationType.getIndependent()) {
                        List<String> endFields = new ArrayList<>();
                        for (AssociationEnd end : associationType.getAssociationEnds()) {
                            //映射关联端
                            List<Field> endField = this.mapAssociationEnd(end);
                            endFields.addAll(endField.stream().map(Field::getName).collect(Collectors.toList()));
                            attrFields.addAll(endField);
                        }
                        //合并去重
                        attrFields = attrFields.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing((Field f) -> f.getName().toLowerCase()))), ArrayList::new));
                        endFields = endFields.stream().distinct().collect(Collectors.toList());
                        this.ensureTable(associationType.getTargetName(), endFields.toArray(new String[0]), attrFields.toArray(new Field[0]), provider);
                    } else {
                        for (AssociationEnd end : associationType.getAssociationEnds()) {
                            //映射关联端
                            List<Field> endField = this.mapAssociationEnd(end);
                            attrFields.addAll(endField);
                        }

                        //合并去重
                        attrFields = attrFields.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing((Field f) -> f.getName().toLowerCase()))), ArrayList::new));

                        ObjectReferencePack<Field[]> lackOnes = new ObjectReferencePack<>();
                        ObjectReferencePack<Field[]> shorterOnes = new ObjectReferencePack<>();
                        provider.fieldExist(associationType.getTargetTable(), attrFields.toArray(new Field[0]), lackOnes, shorterOnes);
                        provider.appendField(associationType.getTargetTable(), lackOnes.realValue);
                        provider.expandField(associationType.getTargetTable(), shorterOnes.realValue);

                        for (AssociationEnd end : associationType.getAssociationEnds()) {
                            if (end.isCompanionEnd())
                                continue;
                            //映射关联端
                            List<String> endFields = this.mapAssociationEnd(end).stream().map(Field::getName).collect(Collectors.toList());
                            for (String field : endFields) {
                                if (!provider.checkKey(associationType.getTargetTable(), new String[]{field}))
                                    provider.createIndex(associationType.getTargetTable(), new String[]{field});
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                throw new UnsupportedOperationException("在映射存储结构时，处理类型" + type.getName() + "的映射表" + ((ObjectType) type).getTargetTable() + "发生错误，请参照内部异常。", exception);
            }
        }
    }

    /**
     * 处理表
     *
     * @param name      名称
     * @param keyFields 键
     * @param fields    字段
     * @param provider  提供器
     */
    private void ensureTable(String name, String[] keyFields, Field[] fields, IStorageStructMappingProvider provider) {
        //创建表
        if (!provider.tableExist(name)) {
            provider.createTable(name, fields, keyFields);
        }
        //检测主键索引
        //检测主键索引
        for (String keyField : keyFields) {
            try {
                //挨个检查
                if (!provider.checkKey(name, new String[]{keyField})) {
                    //没有索引 创建索引
                    provider.createIndex(name, new String[]{keyField});
                }
            } catch (Exception ex) {
                //检查或创建过程中出错 抛出异常由用户检查
                throw new IllegalArgumentException("表" + name + "的索引与主键不完全匹配且暂时无法自动创建,请检查中" + keyField + "字段,自行创建相应字段或者删除此表由自动映射创建.", ex);
            }
        }
        //扩展字段
        ObjectReferencePack<Field[]> lackOnes = new ObjectReferencePack<>();
        ObjectReferencePack<Field[]> shorterOnes = new ObjectReferencePack<>();
        provider.fieldExist(name, fields, lackOnes, shorterOnes);
        provider.appendField(name, lackOnes.realValue);
        provider.expandField(name, shorterOnes.realValue);
    }

    /**
     * 映射属性
     *
     * @param objectType 对象类型
     * @return 得到的字段
     */
    private List<Field> mapAttribute(ObjectType objectType) {
        Iterable<AttributeTree> trees = objectType.enumerateAttributeTree();
        FieldCollector collector = new FieldCollector();
        for (AttributeTree tree : trees) {
            tree.accept(collector);
        }
        return collector.getResult();
    }

    /**
     * 映射关联端
     *
     * @param end 关联端
     * @return 得到的字段
     */
    private List<Field> mapAssociationEnd(AssociationEnd end) {
        List<Field> result = new ArrayList<>();
        for (AssociationEndMapping mapping : end.getMappings()) {
            //查找对应端的键属性
            Attribute attribute = end.getEntityType().getAttribute(mapping.getKeyAttribute());
            result.add(new Field(mapping.getTargetField(), PrimitiveType.fromType(attribute.getDataType()), attribute.getValueLength(), false, attribute.getNullable(), attribute.getPrecision()));
        }
        return result;
    }

    /**
     * 字段收集器，作为属性树向下访问者收集叶子节点的映射字段。
     */
    private static class FieldCollector implements IAttributeTreeDownwardVisitorWithResult<List<Field>> {

        /**
         * 结果
         */
        private List<Field> result = new ArrayList<>();

        /**
         * 前置访问，即在访问子级前执行操作
         *
         * @param subTree          被访问的子树
         * @param parentState      访问父级时产生的状态数据
         * @param outParentState   返回一个状态数据，在遍历到子级时该数据将被视为父级状态
         * @param outPreVisitState 返回一个状态数据，在执行后置访问时该数据将被视为前置访问状态
         */
        @Override
        public void preVisit(AttributeTree subTree, Object parentState, ObjectReferencePack<Object> outParentState, ObjectReferencePack<Object> outPreVisitState) {
            outParentState.realValue = null;
            outPreVisitState.realValue = null;

            AttributeTreeNode treeNode = subTree.getNode();
            if (treeNode.getAttribute() != null && !(treeNode.getAttribute() instanceof ComplexAttribute)) {
                Attribute attribute = treeNode.getAttribute();
                boolean isSelfIncrease = false;
                if (attribute.getHostType() instanceof EntityType) {
                    EntityType entityType = (EntityType) attribute.getHostType();
                    isSelfIncrease = entityType.getKeyFields().contains(attribute.getName()) && entityType.getKeyIsSelfIncreased();
                }

                this.result.add(new Field((parentState == null ? "" : parentState) + attribute.getTargetField(), PrimitiveType.fromType(attribute.getDataType()), attribute.getValueLength(), isSelfIncrease, attribute.getNullable(), attribute.getPrecision()));
            } else if (treeNode.getAttribute() instanceof ComplexAttribute) {
                ComplexAttribute complexAttribute = (ComplexAttribute) treeNode.getAttribute();
                char connectChar = complexAttribute.getMappingConnectionChar();
                outParentState.realValue = (parentState == null ? "" : parentState) + (connectChar == (char) -1 ? "" : complexAttribute.getTargetField() + connectChar);
            }
        }

        /**
         * 后置访问，即在访问子级后执行操作
         *
         * @param subTree       被访问的子树
         * @param parentState   访问父级时产生的状态数据
         * @param preVisitState 前置访问产生的状态数据
         */
        @Override
        public void postVisit(AttributeTree subTree, Object parentState, Object preVisitState) {
            //nothing to do
        }

        /**
         * 重置访问者
         */
        @Override
        public void reset() {
            this.result = new ArrayList<>();
        }

        /**
         * 获取结果
         *
         * @return 结果
         */
        public List<Field> getResult() {
            return this.result;
        }
    }
}
