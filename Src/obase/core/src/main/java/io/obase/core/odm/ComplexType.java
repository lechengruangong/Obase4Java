/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：复杂类型.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-12-2 17:25:34
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.odm;

import java.util.List;
import java.util.Map;

/**
 * 表示复杂类型
 */
public class ComplexType extends StructuralType {

    /**
     * 根据指定的CLR类型创建类型实例
     *
     * @param clrType      CLR类型
     * @param derivingFrom 基类
     */
    public ComplexType(Class<?> clrType, StructuralType derivingFrom) {
        super(clrType, derivingFrom);
        this.typeName.IsAssociation = false;
        this.typeName.IsEntity = false;
    }

    /**
     * 根据指定的CLR类型创建类型实例
     *
     * @param clrType CLR类型
     */
    public ComplexType(Class<?> clrType) {
        super(clrType);
        this.typeName.IsAssociation = false;
        this.typeName.IsEntity = false;
    }

    /**
     * 完整性检查
     * 继承类需要检查则重写此方法
     *
     * @param errDictionary 错误信息字典
     */
    @Override
    public void integrityCheck(Map<String, List<String>> errDictionary) {
        //复杂类型 没有完整性检查
    }

    /**
     * 重写字符串表示形式
     *
     * @return 字符串表示形式
     */
    @Override
    public String toString() {
        return "ComplexType:{{Name-\"" + this.getName() + "\",ClrType-\"" + this.getClrType().getName() + "\"}}";
    }
}
