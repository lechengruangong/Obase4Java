/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：修改Sql语句的对象化表示.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-7 17:33:22
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import io.obase.common.ObjectReferencePack;
import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.common.SqlAliasCollector;
import io.obase.providers.sql.common.SqlAliasReplacer;
import io.obase.providers.sql.common.SqlUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 修改Sql语句的对象化表示
 */
public class ChangeSql extends SqlBase {

    /**
     * 字段设值器
     */
    private final HashMap<String, IFieldSetter> fieldSetters = new HashMap<>();

    /**
     * 修改类型
     */
    private EChangeType changeType;

    /**
     * 要修改的源，参与构建Delete子句、Update子句和Insert Into子句
     */
    private MonomerSource targetSource;

    /**
     * 无参创建修改Sql语句
     */
    public ChangeSql() {
    }

    /**
     * 创建修改Sql语句，指定源、修改类型和筛选条件
     *
     * @param source     源
     * @param changeType 修改类型
     * @param criteria   筛选条件
     */
    public ChangeSql(ISource source, EChangeType changeType, ICriteria criteria) {
        super(source, criteria, getSqlTypeFromChangeType(changeType));
        this.changeType = changeType;
    }

    /**
     * 创建修改Sql语句，指定源、修改类型和筛选条件
     *
     * @param source     源
     * @param changeType 修改类型
     * @param criteria   筛选条件
     */
    public ChangeSql(String source, EChangeType changeType, ICriteria criteria) {
        super(constructSource(source), criteria, getSqlTypeFromChangeType(changeType));
        this.changeType = changeType;
    }

    /**
     * 创建修改Sql语句，指定源、修改类型
     *
     * @param source     源
     * @param changeType 修改类型
     */
    public ChangeSql(ISource source, EChangeType changeType) {
        super(source, getSqlTypeFromChangeType(changeType));
        this.changeType = changeType;
    }

    /**
     * 创建修改Sql语句，指定源、修改类型
     *
     * @param source     源
     * @param changeType 修改类型
     */
    public ChangeSql(String source, EChangeType changeType) {
        super(constructSource(source), getSqlTypeFromChangeType(changeType));
        this.changeType = changeType;
    }

    /**
     * 创建用于插入的Sql语句
     *
     * @param source       源
     * @param fieldSetters 字段设值器集合
     */
    public ChangeSql(String source, List<IFieldSetter> fieldSetters) {
        super(constructSource(source), getSqlTypeFromChangeType(EChangeType.Insert));
        this.appendFieldSetter(fieldSetters);
        this.changeType = EChangeType.Insert;
    }

    /**
     * 创建用于插入的Sql语句
     *
     * @param source       源
     * @param fieldSetters 字段设值器集合
     */
    public ChangeSql(ISource source, List<IFieldSetter> fieldSetters) {
        super(source, getSqlTypeFromChangeType(EChangeType.Insert));
        this.appendFieldSetter(fieldSetters);
        this.changeType = EChangeType.Insert;
    }

    /**
     * 创建用于更新的Sql语句
     *
     * @param source       源
     * @param criteria     筛选条件
     * @param fieldSetters 字段设值器集合
     */
    public ChangeSql(String source, ICriteria criteria, List<IFieldSetter> fieldSetters) {
        super(constructSource(source), criteria, getSqlTypeFromChangeType(EChangeType.Update));
        this.appendFieldSetter(fieldSetters);
        this.changeType = EChangeType.Update;
    }

    /**
     * 创建用于更新的Sql语句
     *
     * @param source       源
     * @param criteria     筛选条件
     * @param fieldSetters 字段设值器集合
     */
    public ChangeSql(ISource source, ICriteria criteria, List<IFieldSetter> fieldSetters) {
        super(source, criteria, getSqlTypeFromChangeType(EChangeType.Update));
        this.appendFieldSetter(fieldSetters);
        this.changeType = EChangeType.Update;
    }

    /**
     * 创建用于删除的Sql语句
     *
     * @param source   源
     * @param criteria 筛选条件
     */
    public ChangeSql(String source, ICriteria criteria) {
        super(constructSource(source), criteria, getSqlTypeFromChangeType(EChangeType.Delete));
        this.changeType = EChangeType.Delete;
    }

    /**
     * 创建用于删除的Sql语句
     *
     * @param source   源
     * @param criteria 筛选条件
     */
    public ChangeSql(ISource source, ICriteria criteria) {
        super(source, criteria, getSqlTypeFromChangeType(EChangeType.Delete));
        this.changeType = EChangeType.Delete;
    }

    /**
     * 通过名称构造ISource
     *
     * @param sourceName 源名称
     * @return 源
     */
    private static ISource constructSource(String sourceName) {
        return new SimpleSource(sourceName);
    }

    /**
     * 根据修改类型获取Sql语句类型
     *
     * @param changeType 修改类型
     * @return Sql语句类型
     */
    private static ESqlType getSqlTypeFromChangeType(EChangeType changeType) {
        switch (changeType) {
            case Insert:
                return ESqlType.Insert;
            case Update:
                return ESqlType.Update;
            case Delete:
                return ESqlType.Delete;
            default:
                throw new IllegalArgumentException("Unexpected value: " + changeType);
        }
    }

    /**
     * 获取要修改的源，参与构建Delete子句、Update子句和Insert Into子句。
     *
     * @return 要修改的源
     */
    public MonomerSource getTargetSource() {
        return this.targetSource;
    }

    /**
     * 设置要修改的源，参与构建Delete子句、Update子句和Insert Into子句。
     *
     * @param targetSource 要修改的源
     */
    public void setTargetSource(MonomerSource targetSource) {
        this.targetSource = targetSource;
    }

    /**
     * 获取修改类型
     *
     * @return 修改类型
     */
    public EChangeType getChangeType() {
        return this.changeType;
    }

    /**
     * 设置修改类型
     *
     * @param changeType 修改类型
     */
    public void setChangeType(EChangeType changeType) {
        this.changeType = changeType;
    }

    /**
     * 针对指定的数据源类型，根据修改Sql语句的对象表示法生成Sql语句。
     * 生成后按别名映射字典将规则别名统一替换为短别名，以避免数据库因别名过长而截断。
     *
     * @param sourceType 数据源类型
     * @return Sql语句
     */
    @Override
    public String toSql(EDataSource sourceType) {
        String sql = renderSql(sourceType);
        return SqlAliasReplacer.replace(sql, SqlAliasCollector.collect(this));
    }

    /**
     * 生成Sql语句（未进行别名缩短）。
     *
     * @param sourceType 数据源类型
     * @return Sql语句
     */
    private String renderSql(EDataSource sourceType) {
        StringBuilder resultBuilder;
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        switch (this.changeType) {
            case Insert: {
                resultBuilder = new StringBuilder("insert into " + this.getSource().toString(sourceType) + " ");
                for (IFieldSetter u : this.fieldSetters.values()) {
                    ObjectReferencePack<String> column = new ObjectReferencePack<>();
                    values.add(u.toString(column, sourceType));
                    columns.add(column.realValue);
                }

                resultBuilder.append("(").append(String.join(",", columns)).append(")");
                resultBuilder.append(" values(").append(String.join(",", values)).append(")");
                break;
            }
            case Update: {
                //Sqlite不支持JoinSource
                if (this.getSource() instanceof JoinedSource && sourceType == EDataSource.Sqlite)
                    throw new IllegalArgumentException(sourceType + "不支持更新连接查询源");

                //目标源
                MonomerSource targetSource = this.getTargetSource();
                if (targetSource == null) targetSource = (MonomerSource) this.getSource();

                //字段
                for (IFieldSetter u : this.fieldSetters.values()) {
                    String column = sourceType == EDataSource.Sqlite
                            ? u.toString(sourceType)
                            : targetSource.getSymbol() + "." + u.toString(sourceType);
                    columns.add(column);
                }

                resultBuilder = new StringBuilder("update ");
                //对于更新语句 SqlServer 和 MySql的语句组成方式有差异
                switch (sourceType) {
                    case SqlServer: {
                        //SqlServer形如 update source set source.value = '' from Source
                        resultBuilder.append(targetSource.getSymbol()).append(" set ").append(String.join(",", columns))
                                .append("  from ").append(this.getSource().toString(sourceType));
                        break;
                    }
                    case Oracle:
                    case MySql:
                    case PostgreSql:
                    case Sqlite: {
                        //MySql形如 update Source set source.value = ''
                        resultBuilder.append(this.getSource().toString(sourceType)).append(" set ").append(String.join(",", columns));
                        break;
                    }
                    default:
                        throw new IllegalArgumentException("不支持的数据源: " + sourceType);
                }

                if (this.getCriteria() != null)
                    resultBuilder.append(" where ").append(this.getCriteria().toString(sourceType));
                break;
            }
            case Delete: {
                //Sqlite不支持JoinSource
                if (this.getSource() instanceof JoinedSource &&
                        (sourceType == EDataSource.Sqlite || sourceType == EDataSource.PostgreSql))
                    throw new IllegalArgumentException(sourceType + "不支持删除连接查询源");

                resultBuilder = new StringBuilder("delete ");

                //补丁 用于处理直接删除等直接修改部分
                MonomerSource targetSource = this.getTargetSource();
                if (targetSource == null) targetSource = (MonomerSource) this.getSource();
                String source = targetSource.getSymbol();
                //简单源使用本名
                if (targetSource instanceof SimpleSource) {
                    SimpleSource simpleSource = (SimpleSource) targetSource;
                    source = simpleSource.getName();
                    //连接源使用目标别名
                    if (this.getSource() instanceof JoinedSource) source = simpleSource.getSymbol();
                }

                //Sqlite无源名称
                if (sourceType != EDataSource.Sqlite) resultBuilder.append(source);
                resultBuilder.append(" from ").append(this.getSource().toString(sourceType));
                if (this.getCriteria() != null)
                    resultBuilder.append(" where ").append(this.getCriteria().toString(sourceType));
                break;
            }
            default:
                throw new IllegalArgumentException("未知的修改Sql类型: " + this.changeType);
        }

        return resultBuilder.toString();
    }

    /**
     * 向Sql语句追加字段设值器
     *
     * @param fieldSetters 字段设值器
     */
    public void appendFieldSetter(List<IFieldSetter> fieldSetters) {
        for (IFieldSetter fieldSetter : fieldSetters) {
            String fieldName = fieldSetter.getField().getName();
            this.fieldSetters.put(StringUtils.capitalize(fieldName), fieldSetter);
        }
    }

    /**
     * 从Sql语句中移除指定的字段设值器
     *
     * @param fieldName 字段设值器
     */
    public void removeFieldSetter(String fieldName) {
        this.fieldSetters.remove(StringUtils.capitalize(fieldName));
    }

    /**
     * 强制覆盖Sql语句中指定字段设值器的值
     *
     * @param fieldName 要覆盖其设值器值的字段的名称
     * @param value     新值
     */
    public void overwriteField(String fieldName, Object value) {
        this.fieldSetters.put(StringUtils.capitalize(fieldName), SqlUtils.getFieldSetter(value == null ? null : value.getClass(), fieldName, value, false, null));
    }

    /**
     * 强制覆盖Sql语句中指定字段设值器的值
     *
     * @param fieldName 要覆盖其设值器值的字段的名称
     * @param value     新值
     */
    public void overwriteField(String fieldName, Expression value) {
        this.fieldSetters.put(StringUtils.capitalize(fieldName), new ExpressionFieldSetter(fieldName, value));
    }

    /**
     * 生成Insert SQL 语句
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 输出 参数列表
     * @param creator       参数对象构造器
     * @return SQL 语句
     */
    private StringBuilder generateInsertTSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {

        //注意out值不要赋空
        // Insert into 目标源

        StringBuilder resultBuilder;
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        //目标源
        MonomerSource tSource = this.getTargetSource();
        if (tSource == null)
            tSource = (MonomerSource) this.getSource();


        /*INSERT INTO  */
        switch (sourceType) {
            //SqlServer数据源
            case SqlServer:
                resultBuilder = new StringBuilder("INSERT INTO [" + tSource.getSymbol() + "]");
                break;
            //PostgreSQL数据源
            case PostgreSql:
                resultBuilder = new StringBuilder("INSERT INTO \"" + tSource.getSymbol() + "\"");
                break;
            //Oracle数据源
            case Oracle:
                //OLEDB数据提供程序
            case Oledb:
                //MySql数据源
            case MySql:
                //Sqlite数据源
            case Sqlite:
                //其他数据源
            case Other:
                resultBuilder = new StringBuilder("INSERT INTO `" + tSource.getSymbol() + "`");
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        /*(columns) values(...)*/
        for (String key : this.fieldSetters.keySet()) {
            ObjectReferencePack<DataParameter> para = new ObjectReferencePack<>();
            ObjectReferencePack<String> column = new ObjectReferencePack<>();
            String placeholder = this.fieldSetters.get(key).toString(para, column, sourceType, creator);
            sqlParameters.realValue.add(para.realValue);
            values.add(placeholder);
            columns.add(column.realValue);
        }

        //(columns)
        resultBuilder.append("(").append(String.join(",", columns)).append(")");
        //values(...)
        resultBuilder.append(" VALUES(").append(String.join(",", values)).append(")");
        return resultBuilder;
    }

    /**
     * 生成Update SQL 语句
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 输出 参数列表
     * @param creator       参数对象构造器
     * @return SQL 语句
     */
    private StringBuilder generateUpdateTSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        StringBuilder resultBuilder;
        List<String> sets = new ArrayList<>();

        //目标源
        MonomerSource tSource = this.getTargetSource();
        if (tSource == null)
            tSource = (MonomerSource) this.getSource();

        /*UPDATE 目标源*/
        switch (sourceType) {
            //SqlServer数据源
            case SqlServer:
                resultBuilder = new StringBuilder("UPDATE [" + tSource.getSymbol() + "]");
                break;
            case PostgreSql:
                resultBuilder = new StringBuilder("UPDATE \"" + tSource.getSymbol() + "\" \"" + tSource.getSymbol() + "\"");
                break;
            //Oracle数据源
            case Oracle:
                //OLEDB数据提供程序
            case Oledb:
                //MySql数据源
            case MySql:
                //Sqlite数据源
            case Sqlite:
                //其他数据源
            case Other:
                resultBuilder = new StringBuilder("UPDATE `" + tSource.getSymbol() + "`");
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        for (String key : this.fieldSetters.keySet()) {
            ObjectReferencePack<DataParameter> para = new ObjectReferencePack<>();
            String setWithPlaceholder = this.fieldSetters.get(key).toString(para, sourceType, creator);
            if (para.realValue != null) sqlParameters.realValue.add(para.realValue);
            switch (sourceType) {
                case SqlServer:
                case Oracle:
                case Oledb:
                case PostgreSql:
                case MySql:
                    break;
                case Sqlite:
                    setWithPlaceholder = setWithPlaceholder + "";
                    break;
            }
            sets.add(setWithPlaceholder);
        }

        resultBuilder.append(" SET ").append(String.join(", ", sets)).append(" ");

        ISource source = this.getSource();
        if (source instanceof MonomerSource) {
            MonomerSource monomerSource = (MonomerSource) source;
            monomerSource.clearSymbol();
        }

        /*FROM 查询源（A inner join B on ----）*/
        if (sourceType == EDataSource.SqlServer) {
            //SqlServer形如 update source set source.value = '' from Source
            resultBuilder.append(" FROM ").append(this.getSource().toString(sourceType)).append(" ");
        }

        /*WHERE 条件*/
        if (this.getCriteria() != null) {
            ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
            resultBuilder.append("  WHERE  ").append(this.getCriteria().toString(sourceType, paras, creator));
            sqlParameters.realValue.addAll(paras.realValue);
        }

        return resultBuilder;
    }

    /**
     * 生成生成Delete SQL 语句
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 输出 参数列表
     * @param creator       参数对象构造器
     * @return SQL 语句
     */
    private StringBuilder generateDeleteTSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        StringBuilder resultBuilder;

        //目标源
        MonomerSource tSource = this.getTargetSource();
        if (tSource == null)
            tSource = (MonomerSource) this.getSource();

        /*Delete 目标源*/
        switch (sourceType) {
            //SqlServer数据源
            case SqlServer:
                resultBuilder = new StringBuilder("DELETE [" + tSource.getSymbol() + "]");
                break;
            //PostgreSQL数据源
            case PostgreSql: {
                if (this.getSource() instanceof JoinedSource) {
                    resultBuilder = new StringBuilder("DELETE \"" + tSource.getSymbol() + "\"");
                } else {
                    resultBuilder = new StringBuilder(" DELETE ");
                }

                break;
            }
            //Oracle数据源
            case Oracle:
                //OLEDB数据提供程序
            case Oledb:
                //MySql数据源
            case MySql:
                //Sqlite数据源
            case Sqlite: {
                if (this.getSource() instanceof JoinedSource) {
                    resultBuilder = new StringBuilder("DELETE `" + tSource.getSymbol() + "`");
                } else {
                    resultBuilder = new StringBuilder(" DELETE ");
                }

                break;
            }
            //其他数据源
            case Other:
                resultBuilder = new StringBuilder("DELETE `" + tSource.getSymbol() + "`");
                break;
            default:
                throw new IllegalArgumentException("不支持的数据源: " + sourceType);
        }

        ISource source = this.getSource();
        if (source instanceof MonomerSource) {
            MonomerSource monomerSource = (MonomerSource) source;
            monomerSource.clearSymbol();
        }

        if (sourceType == EDataSource.Sqlite || sourceType == EDataSource.PostgreSql && (this.getSource() instanceof JoinedSource || this.getSource() instanceof SimpleSource)) {
            if (this.getSource() instanceof JoinedSource) {
                throw new IllegalArgumentException("SqlIte和PostgreSQL不支持Delete Join源");
            }

            if (this.getSource() instanceof SimpleSource) {
                SimpleSource simpleSource = (SimpleSource) this.getSource();
                /*From 查询源*/
                resultBuilder.append(" FROM ").append(simpleSource.toNoSymbolString(sourceType));
                if (sourceType == EDataSource.PostgreSql) {
                    resultBuilder.append(" \"").append(simpleSource.getName()).append("\" ");
                }
            }
        } else {
            /*From 查询源*/
            resultBuilder.append(" From ").append(source.toString(sourceType));
        }


        /*WHERE 条件*/
        if (this.getCriteria() != null) {
            ObjectReferencePack<List<DataParameter>> paras = new ObjectReferencePack<>();
            resultBuilder.append(" WHERE ").append(this.getCriteria().toString(sourceType, paras, creator));
            sqlParameters.realValue.addAll(paras.realValue);
        }

        return resultBuilder;
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串。
     * 生成后按别名映射字典将规则别名统一替换为短别名，以避免数据库因别名过长而截断。
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    @Override
    public String toSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        String sql = renderSql(sourceType, sqlParameters, creator);
        return SqlAliasReplacer.replace(sql, SqlAliasCollector.collect(this));
    }

    /**
     * 使用参数化的方式 和 指定的数据源 将Sql对象表示为Sql字符串（未进行别名缩短）。
     *
     * @param sourceType    数据源类型
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    private String renderSql(EDataSource sourceType, ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        //注意out值不要赋空
        StringBuilder resultBuilder;
        switch (this.changeType) {
            case Insert:
                resultBuilder = this.generateInsertTSql(sourceType, sqlParameters, creator);
                break;
            case Update:
                resultBuilder = this.generateUpdateTSql(sourceType, sqlParameters, creator);
                break;
            case Delete:
                resultBuilder = this.generateDeleteTSql(sourceType, sqlParameters, creator);
                break;
            default:
                throw new IllegalArgumentException("未知的变更Sql类型: " + this.changeType);
        }

        DataParameterSorter.sort(sqlParameters.realValue);

        return resultBuilder.toString();
    }

    /**
     * 使用参数化的方式 和 默认的数据源 将Sql对象表示为Sql字符串
     *
     * @param sqlParameters 参数列表
     * @param creator       参数构造器
     * @return Sql语句
     */
    @Override
    public String toSql(ObjectReferencePack<List<DataParameter>> sqlParameters, IParameterCreator creator) {
        return this.toSql(EDataSource.SqlServer, sqlParameters, creator);
    }
}
