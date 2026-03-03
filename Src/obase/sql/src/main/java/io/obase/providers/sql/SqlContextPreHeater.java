/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Obase配置预热器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-13 11:13:45
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql;

import io.obase.core.ContextConfigProvider;
import io.obase.core.GlobalModelCache;
import io.obase.core.ObjectContext;
import io.obase.core.common.Utils;
import io.obase.core.odm.ObjectDataModel;
import io.obase.providers.sql.connectionpool.ObaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Obase配置预热器
 */
public abstract class SqlContextPreHeater {

    /**
     * 预热方法
     */
    public void preHeat(ObjectContext context) {

        ObjectDataModel model = context.getModel();
        Class<?> contextClass = context.getClass();
        ContextConfigProvider provider = context.getConfigProvider();
        SqlContextConfigProvider sqlContextConfigProvider;
        if (provider instanceof SqlContextConfigProvider) {
            sqlContextConfigProvider = (SqlContextConfigProvider) provider;
        } else {
            sqlContextConfigProvider = null;
        }

        if (sqlContextConfigProvider != null) {

            String driverClass = sqlContextConfigProvider.getDbDriverClass();
            String connectionStr = sqlContextConfigProvider.getConnectionString();
            String userName = sqlContextConfigProvider.getConnectionUserName();
            String passWord = sqlContextConfigProvider.getConnectionPassWord();

            if (Utils.getStringIsEmpty(driverClass))
                throw new IllegalArgumentException("未正确设置数据库驱动类");

            if (Utils.getStringIsEmpty(connectionStr))
                throw new IllegalArgumentException("未正确设置数据库连接字符串");

            if (Utils.getStringIsEmpty(userName) && sqlContextConfigProvider.getSourceType() != EDataSource.Sqlite)
                throw new IllegalArgumentException("未正确设置数据库登录名");

            if (Utils.getStringIsEmpty(passWord) && sqlContextConfigProvider.getSourceType() != EDataSource.Sqlite)
                throw new IllegalArgumentException("未正确设置数据库登录密码");

            DataSource source = ObaseConnectionPool.getInstance().getPool(driverClass, connectionStr, userName, passWord, contextClass);
            assert source != null;
        }

        assert GlobalModelCache.getInstance().getModel(contextClass).equals(model);

        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Obase Has Initialized");
    }
}
