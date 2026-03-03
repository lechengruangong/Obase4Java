/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：提供确保所有的外键属性都已定义的机制,提供执行保证方法.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-3 14:59:59
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import io.obase.common.ObjectReferencePack;
import io.obase.core.common.Utils;

import java.util.List;

/**
 * 提供确保所有的外键属性都已定义的机制。
 * 说明
 * 检查需要定义的外键属性是否已存在，如果不存在则自动定义，并将定义的属性追加至模型类型。
 * 对于关联型，检查其自身的外键；对于实体型，检查其作为关联端参与的关联型的外键。
 */
public abstract class ForeignKeyGuarantor {

    /**
     * 执行保证
     *
     * @param objType   确保其定义外键的对象类型
     * @param returnEnd 要返回其外键的关联端
     * @return 定义的外键
     */
    public Attribute[] guarantee(ObjectType objType, AssociationEnd returnEnd) {
        ObjectReferencePack<List<Attribute>> returnKey = new ObjectReferencePack<>();
        List<Attribute> attrs = Utils.getDefinedForeignAttributes(objType, returnEnd, returnKey);

        //检查构造的新属性
        if (attrs.size() > 0) {
            this.defineMissing(attrs.toArray(new Attribute[0]), objType);

            for (Attribute attribute : attrs) {
                if (attribute.getValueGetter() == null || attribute.getValueSetter() == null)
                    throw new ForeignKeyGuarantingException("构造外键时错误,没有为外键设置设值器或取值器");
            }
        }

        Attribute[] resultArray = new Attribute[returnKey.realValue.size()];
        returnKey.realValue.toArray(resultArray);
        return resultArray;
    }

    /**
     * 在外键属性缺失的情况下定义所缺的属性
     *
     * @param attrs   要定义的外键属性
     * @param objType 要定义属性的类型
     */
    protected abstract void defineMissing(Attribute[] attrs, ObjectType objType);
}
