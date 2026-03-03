/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：表示分组子句.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 16:59:03
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.providers.sql.EDataSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 表示分组子句
 */
public class GroupBy {

    /**
     * 作为分组依据的表达式
     */
    private final List<Expression> expressions;

    /**
     * 创建表示依据指定的表达式分组的GroupBy实例
     *
     * @param expressions 作为分组依据的表达式
     */
    public GroupBy(Expression... expressions) {
        List<Expression> tempList = new ArrayList<>(Arrays.asList(expressions));
        this.expressions = Collections.unmodifiableList(tempList);
    }

    /**
     * 获取作为分组依据的表达式
     *
     * @return 分组依据的表达式
     */
    public Expression[] getExpressions() {
        return this.expressions.toArray(new Expression[0]);
    }

    /**
     * GroupBy的字符串表示形式
     *
     * @param sourceType 数据源类型
     * @return 字符串表示形式
     */
    public String toString(EDataSource sourceType) {
        StringBuilder builder = new StringBuilder(" group by ");

        for (int i = 0; i < this.expressions.size(); i++) {
            if (i != this.expressions.size() - 1) {
                builder.append(this.expressions.get(i).toString(sourceType)).append(",");
            } else {
                builder.append(this.expressions.get(i).toString(sourceType));
            }
        }

        return builder.toString();
    }
}
