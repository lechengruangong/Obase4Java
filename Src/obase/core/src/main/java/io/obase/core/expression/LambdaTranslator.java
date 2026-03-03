/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Lambda表达式翻译器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2025-11-18 15:26:51
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.core.expression;

import ch.epfl.labos.iu.orm.queryll2.path.*;
import ch.epfl.labos.iu.orm.queryll2.symbolic.ConstantValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValue;
import ch.epfl.labos.iu.orm.queryll2.symbolic.TypedValueVisitorException;
import com.user00.thunk.SerializedLambda;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lambda表达式翻译器
 * 表达式翻译的总入口
 */
public class LambdaTranslator {

    /**
     * 解析表达式
     *
     * @param lambda 可解析的Lambda表达式必须是Serializable的
     * @return Lambda表达式
     */
    public LambdaExpression getLambdaExpression(Serializable lambda) {
        MethodChecker methodChecker = MethodChecker.getInstance();
        PathAnalysisFactory pathAnalysisFactory = new PathAnalysisFactory(methodChecker);
        //解析结果
        MethodAnalysisResults analysis;
        try {
            //对lambda进行计算
            SerializedLambda lambdaInfo = SerializedLambda.extractLambda(lambda);
            TransformationClassAnalyzer classAnalyzer = TransformationClassAnalyzerCache.getInstance().getTransformationClassAnalyzer(lambdaInfo.implClass);
            if (classAnalyzer == null) {
                classAnalyzer = new TransformationClassAnalyzer(lambdaInfo.implClass, null);
                TransformationClassAnalyzerCache.getInstance().setTransformationClassAnalyzer(lambdaInfo.implClass, classAnalyzer);
            }
            analysis = classAnalyzer.analyzeLambdaMethod(lambdaInfo.implMethodName, lambdaInfo.implMethodSignature, pathAnalysisFactory);
            PathAnalysisSimplifier.cleanAndSimplify(analysis, methodChecker.getComparisonMethods(true), methodChecker.getComparisonStaticMethods(true), true);
        } catch (Exception exception) {
            throw new RuntimeException("无法构造表达式解析器,请参考内部异常.", exception);
        }

        //容器 每一项都是一组单独的条件
        List<List<TypedValue>> conditions = new ArrayList<>();

        for (int n = 0; n < analysis.paths.size(); n++) {
            PathAnalysis path = analysis.paths.get(n);

            //分析结果 此路径是否为完全路径
            TypedValue returnVal = PathAnalysisSimplifier
                    .simplifyBoolean(path.getReturnValue(), methodChecker.getComparisonMethods(true), methodChecker.getComparisonStaticMethods(true), true);
            //此组条件的临时容器
            List<TypedValue> clauses = new ArrayList<>();

            if (returnVal instanceof ConstantValue.BooleanConstant) {
                //返回true或者false的时候 这种路径并非是真正的路径
                //为true的需要用getConditions()获取具体的条件
                //false的忽略掉
                if (((ConstantValue.BooleanConstant) returnVal).val) {
                    returnVal = null;
                } else {
                    continue;
                }
            }

            if (returnVal != null)
                clauses.add(returnVal);
            //都装进去
            clauses.addAll(path.getConditions());
            //一组条件
            conditions.add(clauses);
        }

        //组条件为null 则返回一个1==0
        if (conditions.isEmpty()) {
            return Expression.lambda(new ParameterExpression[0], Expression.equal(new ConstantExpression("1"), new ConstantExpression("0"), boolean.class));
        }
        //组条件都没有值 则返回一个1==1
        if (conditions.stream().allMatch(List::isEmpty)) {
            return Expression.lambda(new ParameterExpression[0], Expression.equal(new ConstantExpression("1"), new ConstantExpression("1"), boolean.class));
        }

        //去除每组条件中因为 或运算 导致的重复条件
        for (int n = 0; n < conditions.size(); n++) {
            //或运算会导致一个条件分裂成两个 一个是原条件 一个是原条件的取非
            List<TypedValue> conjunction = conditions.get(n);
            if (conjunction.size() != 1)
                continue;
            TypedValue not = TypedValue.NotValue.invert(conjunction.get(0));
            for (int i = n + 1; i < conditions.size(); i++)
                conditions.get(i).remove(not);
        }

        //获取实参
        Field[] fields = lambda.getClass().getDeclaredFields();
        Map<Integer, Object> realArguments = new HashMap<>();
        int index = 0;
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                realArguments.put(index, field.get(lambda));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("无法获取实参,请参考内部异常.", e);
            }
            index++;
        }

        //翻译器
        ExpressionTranslator translator = new ExpressionTranslator(realArguments);
        //参数表达式字典 用于在访问中暂存参数
        Map<String, ParameterExpression> parameterMap = new HashMap<>();

        Expression resultExpression = null;
        //循环每组条件
        for (List<TypedValue> conjunction : conditions) {
            Expression pathExpression = null;
            for (TypedValue clause : conjunction) {
                //一组条件
                Expression expression;
                try {
                    expression = clause.visit(translator, parameterMap);
                } catch (TypedValueVisitorException e) {
                    throw new RuntimeException("表达式翻译错误,清参考内部异常.", e);
                }
                //关系都是 AND
                if (pathExpression == null)
                    pathExpression = expression;
                else
                    pathExpression = Expression.and(pathExpression, expression, Boolean.class);
            }
            //组外是 OR
            if (resultExpression != null)
                resultExpression = Expression.or(resultExpression, pathExpression, Boolean.class);
            else
                resultExpression = pathExpression;
        }

        //排序参数
        ParameterExpression[] array = new ParameterExpression[parameterMap.values().size()];
        parameterMap.values().stream().sorted(Comparator.comparingInt(ParameterExpression::getIndex)).collect(Collectors.toList()).toArray(array);

        //返回表达式
        return Expression.lambda(array, resultExpression);
    }
}
