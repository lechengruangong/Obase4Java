/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示对两个查询结果执行集运算的Sql语句.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:26:23
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.core.query.ESetOperator;
import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 表示对两个查询结果执行集运算的Sql语句
 */
public class QuerySet implements ISetOperand {

    /**
     * 左操作数
     */
    private final ISetOperand left;

    /**
     * 集运算操作符
     */
    private final ESetOperator operator;

    /**
     * 右操作数
     */
    private final ISetOperand right;

    /**
     * 创建QuerySet实例，同时指定左操作数、右操作数和运算符
     *
     * @param left      作为左操作数的查询Sql语句
     * @param right     作为右操作数的查询Sql语句
     * @param eOperator 集运算符
     */
    public QuerySet(ISetOperand left, ISetOperand right, ESetOperator eOperator) {
        this.left = left;
        this.right = right;
        this.operator = eOperator;
    }

    /**
     * 获取集运算操作符
     *
     * @return 集运算操作符
     */
    public ESetOperator getOperator() {
        return this.operator;
    }

    /**
     * 获取作为集运算左操作数的查询Sql语句
     *
     * @return 左操作数的查询Sql语句
     */
    public ISetOperand getLeft() {
        return this.left;
    }

    /**
     * 获取作为集运算右操作数的查询Sql语句
     *
     * @return 右操作数的查询Sql语句
     */
    public ISetOperand getRight() {
        return this.right;
    }

    /**
     * 使用参数化的方式 和 默认的数据源 将Sql对象表示为Sql字符串
     *
     * @param parameters 返回字符串中的参数及其值的集合
     * @param creator    参数构造器
     * @return Sql字符串
     */
    @Override
    public String toSql(ObjectReferencePack<List<DataParameter>> parameters, IParameterCreator creator) {
        return this.toSql(EDataSource.SqlServer, parameters, creator);
    }

    /**
     * 对指定的数据源类型，根据查询Sql语句的对象表示法生成Sql语句。
     *
     * @param sourceType 数据源类型
     * @return Sql字符串
     */
    @Override
    public String toSql(EDataSource sourceType) {
        String result = null;
        //对每个部分处理
        switch (this.operator) {

            case Concat:
                result = this.left.toSql(sourceType) + " union all  " + this.right.toSql(sourceType);
                break;
            case Interact:
                result = this.left.toSql(sourceType) + " except all " + this.right.toSql(sourceType);
                break;
            case Except:
                result = this.left.toSql(sourceType) + " intersect all " + this.right.toSql(sourceType);
                break;
            case Union:
                result = this.left.toSql(sourceType) + " union all " + this.right.toSql(sourceType);
                break;
        }

        return result;
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串
     *
     * @param sourceType 指定的数据源
     * @param parameters 参数
     * @param creator    参数构造器
     * @return Sql字符串
     */
    @Override
    public String toSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> parameters, IParameterCreator creator) {
        //参数集合
        parameters.realValue = new ArrayList<>();
        ObjectReferencePack<List<DataParameter>> parameterLeft = new ObjectReferencePack<>();
        ObjectReferencePack<List<DataParameter>> parameterRight = new ObjectReferencePack<>();

        String result = null;
        //对每个部分处理
        switch (this.operator) {

            case Concat:
                result = this.left.toSql(sourceType, parameterLeft, creator) + " union all  " + this.right.toSql(sourceType, parameterRight, creator);
                break;
            case Interact:
                result = this.left.toSql(sourceType, parameterLeft, creator) + " except all " + this.right.toSql(sourceType, parameterRight, creator);
                break;
            case Except:
                result = this.left.toSql(sourceType, parameterLeft, creator) + " intersect all " + this.right.toSql(sourceType, parameterRight, creator);
                break;
            case Union:
                result = this.left.toSql(sourceType, parameterLeft, creator) + " union all " + this.right.toSql(sourceType, parameterRight, creator);
                break;
        }

        parameters.realValue.addAll(parameterLeft.realValue);
        parameters.realValue.addAll(parameterRight.realValue);

        DataParameterSorter.sort(parameters.realValue);

        return result;
    }
}
