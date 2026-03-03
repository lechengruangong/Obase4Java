/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：字段条件.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-6 17:29:37
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段条件
 */
public class FieldCriteria extends SimpleCriteria<Field> {

    /**
     * 创建字段条件实例
     *
     * @param leftField        左端字段名
     * @param relationOperator 关系运算符
     * @param rightField       右端字段名
     */
    public FieldCriteria(String leftField, ERelationOperator relationOperator, String rightField) {
        super(leftField, relationOperator, new Field(rightField));
    }

    /**
     * 创建字段条件实例
     *
     * @param leftSource       左端源名称
     * @param leftField        左端字段名
     * @param relationOperator 关系运算符
     * @param rightSource      右端源名称
     * @param rightField       右端字段名
     */
    public FieldCriteria(String leftSource, String leftField, ERelationOperator relationOperator, String rightSource, String rightField) {
        super(leftSource, leftField, relationOperator, new Field(rightSource, rightField));
    }

    /**
     * 创建字段条件实例
     *
     * @param leftField        左端字段
     * @param relationOperator 关系运算符
     * @param rightField       右端字段
     */
    public FieldCriteria(Field leftField, ERelationOperator relationOperator, Field rightField) {
        super(leftField, relationOperator, rightField);
    }

    /**
     * 创建字段条件实例
     *
     * @param leftSource       左端源
     * @param leftField        左端字段名
     * @param relationOperator 关系运算符
     * @param rightSource      右端源
     * @param rightField       右端字段名
     */
    public FieldCriteria(ISource leftSource, String leftField, ERelationOperator relationOperator, ISource rightSource, String rightField) {
        super(new Field((MonomerSource) leftSource, leftField), relationOperator, new Field((MonomerSource) rightSource, rightField));
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return 字符串表示形式
     */
    @Override
    public String toString(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        String returnValue = null;
        //字段
        String result = this.getField().toString(sourceType);
        ObjectReferencePack<List<DataParameter>> matchValueParameters = new ObjectReferencePack<>();
        //值
        String matchValue = this.getValue().toString(sourceType);
        matchValueParameters.realValue = new ArrayList<>();

        switch (this.getOperator()) {
            case Equal:
                returnValue = matchValue != null ? result + " = " + matchValue : result + " is null";
                break;
            case GreaterThan:
                returnValue = result + " > " + matchValue;
                break;
            case GreaterThanOrEqual:
                returnValue = result + " >= " + matchValue;
                break;
            case LessThan:
                returnValue = result + " < " + matchValue;
                break;
            case LessThanOrEqual:
                returnValue = result + " <= " + matchValue;
                break;
            case In:
                returnValue = result + " in (" + matchValue + ")";
                break;
            case Like:
                returnValue = result + "like '%" + matchValue.replaceAll("[" + "%" + "]+$", "").replaceAll("^[" + "%" + "]+", "") + "%'";
                break;
            case NotIn:
                returnValue = result + " not in (" + matchValue + ")";
                break;
            case Unequal:
                returnValue = matchValue != null ? result + " <> " + matchValue : result + " is not null";
                break;
        }

        sqlParameters.realValue = new ArrayList<>();
        sqlParameters.realValue.addAll(matchValueParameters.realValue);

        DataParameterSorter.sort(sqlParameters.realValue);

        return returnValue;
    }
}
