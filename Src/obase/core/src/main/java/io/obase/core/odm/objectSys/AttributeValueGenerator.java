/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：属性值生成器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-27 14:57:39
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm.objectSys;

import io.obase.common.FunctionWithOneArg;
import io.obase.common.ObjectReferencePack;
import io.obase.core.odm.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 属性值生成器
 */
public class AttributeValueGenerator implements IAttributeTreeDownwardVisitor {

    /**
     * 一个委托，用于获取属性树节点代表的简单属性的值。
     */
    private final FunctionWithOneArg<SimpleAttributeNode, Object> attributeValueGetter;

    /**
     * 临时值存储字典
     */
    private final Map<String, Object> tempDict = new HashMap<>();

    /**
     * 遍历属性树的结果
     */
    private Object result;

    /**
     * 创建AttributeValueGenerator实例
     *
     * @param attrValueGetter 一个委托，用于获取属性树节点代表的简单属性的值
     */
    public AttributeValueGenerator(FunctionWithOneArg<SimpleAttributeNode, Object> attrValueGetter) {
        this.attributeValueGetter = attrValueGetter;
    }

    /**
     * 获取遍历属性树的结果
     *
     * @return 遍历属性树的结果
     */
    public Object getResult() {
        return this.result;
    }

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
        if (!subTree.getIsComplex()) //当前节点是否为复杂属性
        {
            //是根节点 则node必为simple 取值或为null
            this.result = subTree.getParent() == null && this.attributeValueGetter != null
                    ? this.attributeValueGetter.invoke((SimpleAttributeNode) subTree.getNode())
                    : null;
        }
        //处理复杂节点
        else {
            TypeBase type = subTree.getAttributeType();
            if (type instanceof StructuralType) {
                StructuralType structuralType = (StructuralType) type;
                //构造实例化结构类型的委托
                //实际上这里用了个local方法
                FunctionWithOneArg<Parameter, Object> argGetter = parameter -> {
                    TypeElement element = parameter.getElement();
                    if (element instanceof Attribute) {
                        Attribute attr = (Attribute) element;
                        Object value = attr.getIsComplex()
                                ? this.tempDict.get(attr.getName())
                                : this.attributeValueGetter.invoke((SimpleAttributeNode) subTree.getNode());

                        if (parameter.getValueConverter() != null) return parameter.getValueConverter().invoke(value);
                    }

                    return parameter.getElement();
                };

                Object targetObject = structuralType.instantiateWithParameter(argGetter);


                //为属性设值
                AttributeTree[] subtrees = subTree.getSubTrees();
                if (subtrees != null && subtrees.length > 0) {
                    for (AttributeTree item : subtrees) {
                        //有些值在构造时就以被赋值 这些值不用处理
                        if (structuralType.getConstructor().getParameterByElement(item.getAttributeName()) == null) {
                            if (item.getIsComplex()) {
                                item.getAttribute().setValue(targetObject, this.tempDict.get(subTree.getAttributeName()));
                            } else {
                                Object val = this.attributeValueGetter.invoke((SimpleAttributeNode) item.getNode());
                                if (val != null)
                                    item.getAttribute().setValue(targetObject, val);
                            }
                        }
                    }
                }

                //是根节点
                if (subTree.getParent() == null) {
                    //最终结果
                    this.result = targetObject;
                    this.tempDict.clear();
                } else {
                    //暂存
                    this.tempDict.put(subTree.getAttributeName(), targetObject);
                }

            }
        }
    }

    /**
     * 重置访问者
     */
    @Override
    public void reset() {
        //Nothing to Do
    }
}
