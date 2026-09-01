package io.obase.test.core.functional;

import io.obase.providers.sql.EDataSource;
import io.obase.providers.sql.common.SqlAliasReplacer;
import io.obase.providers.sql.common.SqlAliasShortener;
import io.obase.providers.sql.sqlobject.ChangeSql;
import io.obase.providers.sql.sqlobject.EChangeType;
import io.obase.providers.sql.sqlobject.Field;
import io.obase.providers.sql.sqlobject.QuerySql;
import io.obase.providers.sql.sqlobject.SimpleSource;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Sql别名缩短器测试（集中式替换方案）。
 * 验证在生成Sql字符串后，按"原始别名↔短别名"映射字典将规则生成的别名（下划线前缀）统一替换为
 * "_obase_gen_alias+哈希"的短名称（总长度40以下），以保证不被数据库因别名过长等原因截断。
 */
public class SqlAliasShortenerTest {

    /**
     * 规则生成的别名（下划线前缀）一律缩短为"前缀+哈希"，总长度40以下。
     */
    @Test
    public void shorten_RuleAlias_IsShortenedWithinFortyChars() {
        String[] aliases = {"_Order_Items_Price", "_Student", "_" + "a".repeat(100)};
        for (String alias : aliases) {
            String shortAlias = SqlAliasShortener.shorten(alias);
            assertTrue(shortAlias.startsWith(SqlAliasShortener.PREFIX), alias);
            assertTrue(shortAlias.length() < 40, alias);
            assertEquals(SqlAliasShortener.MAX_GENERATED_LENGTH, shortAlias.length(), alias);
        }
    }

    /**
     * 非规则名称（无下划线前缀）保持原样。
     */
    @Test
    public void shorten_PlainName_KeepsOriginal() {
        String[] names = {"price", "Name", "Student", "t1", "OTB_Col", "obaseOrderCol0"};
        for (String name : names)
            assertEquals(name, SqlAliasShortener.shorten(name), name);
    }

    /**
     * 同一原始别名在多次调用中产生相同结果（确定性），且映射字典缓存生效。
     */
    @Test
    public void shorten_IsDeterministic() {
        String alias = "_ThisIsAVeryLongAliasNameThatExceedsTheLimitForSure_Order_Items";
        String first = SqlAliasShortener.shorten(alias);
        String second = SqlAliasShortener.shorten(alias);
        assertEquals(first, second);
        assertEquals(first, SqlAliasShortener.getShort(alias));
    }

    /**
     * 缩短是幂等的：对已缩短的别名再次缩短结果不变。
     */
    @Test
    public void shorten_IsIdempotent() {
        String alias = "_ThisIsAVeryLongAliasNameThatExceedsTheLimitForSure_Order_Items";
        String once = SqlAliasShortener.shorten(alias);
        assertEquals(once, SqlAliasShortener.shorten(once));
    }

    /**
     * 不同的原始别名产生不同的短别名。
     */
    @Test
    public void shorten_DifferentAliases_ProduceDifferentResults() {
        List<String> aliases = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            String alias = "_" + i + "_SomeVeryLongElementName_" + i + "_AnotherVeryLongElementName_" + i;
            aliases.add(SqlAliasShortener.shorten(alias));
        }

        assertEquals(aliases.size(), new HashSet<>(aliases).size(), "不同原始别名产生了相同的短别名");
    }

    /**
     * 空别名与null保持原样。
     */
    @Test
    public void shorten_NullOrEmpty_ReturnsOriginal() {
        assertNull(SqlAliasShortener.shorten(null));
        assertEquals("", SqlAliasShortener.shorten(""));
    }

    /**
     * 替换器：识别双引号、反引号、方括号及裸标识符并替换白名单中的别名。
     */
    @Test
    public void replace_QuotedAndBareIdentifiers_AreReplaced() {
        String alias = "_Order_Items";
        String shortAlias = SqlAliasShortener.shorten(alias);
        Set<String> aliases = new HashSet<>();
        aliases.add(alias);

        //PostgreSql双引号
        assertEquals("\"t_order\" \"" + shortAlias + "\"", SqlAliasReplacer.replace("\"t_order\" \"" + alias + "\"", aliases));
        //MySql/Sqlite反引号
        assertEquals("`t_order` `" + shortAlias + "`", SqlAliasReplacer.replace("`t_order` `" + alias + "`", aliases));
        //SqlServer方括号
        assertEquals("[t_order] [" + shortAlias + "]", SqlAliasReplacer.replace("[t_order] [" + alias + "]", aliases));
        //Oracle裸标识符
        assertEquals("t_order " + shortAlias, SqlAliasReplacer.replace("t_order " + alias, aliases));
        //限定引用
        assertEquals("\"" + shortAlias + "\".\"price\"", SqlAliasReplacer.replace("\"" + alias + "\".\"price\"", aliases));
    }

    /**
     * 替换器：跳过单引号字符串字面量。
     */
    @Test
    public void replace_StringLiteral_IsNotReplaced() {
        String alias = "_Order_Items";
        Set<String> aliases = new HashSet<>();
        aliases.add(alias);
        String sql = "select '" + alias + "' from t";
        assertEquals(sql, SqlAliasReplacer.replace(sql, aliases));
    }

    /**
     * 替换器：不做子串替换，且白名单外的标识符（如真实表名）不替换。
     */
    @Test
    public void replace_NoSubstringOrNonAliasReplacement() {
        String alias = "_Order_Items";
        String shortAlias = SqlAliasShortener.shorten(alias);
        Set<String> aliases = new HashSet<>();
        aliases.add(alias);

        //子串不误伤
        assertEquals("\"" + alias + "_Price\"", SqlAliasReplacer.replace("\"" + alias + "_Price\"", aliases));
        assertNotEquals(shortAlias, SqlAliasShortener.shorten(alias + "_Price"));
        //白名单外的下划线名称（真实表名等）不替换
        String tableAlias = "_user";
        String sql = "\"" + tableAlias + "\" \"" + tableAlias + "\"";
        assertEquals(sql, SqlAliasReplacer.replace(sql, aliases));
    }

    /**
     * Sql生成侧集成：QuerySql渲染Sql后，规则别名被统一替换为短别名。
     */
    @Test
    public void querySqlRender_RuleAliases_AreReplaced() {
        SimpleSource source = new SimpleSource("t_order", "_Order_Items");
        QuerySql query = new QuerySql(source);
        query.getSelectionSet().add(new Field(source, "price"), "_Order_Items_Price");

        String sql = query.toSql(EDataSource.PostgreSql);
        String expectedSymbol = SqlAliasShortener.shorten("_Order_Items");
        String expectedColumn = SqlAliasShortener.shorten("_Order_Items_Price");

        assertTrue(sql.contains("\"t_order\""), sql);
        assertTrue(sql.contains("\"" + expectedSymbol + "\""), sql);
        assertTrue(sql.contains("\"" + expectedColumn + "\""), sql);
        //原始别名不再出现
        assertFalse(sql.contains("_Order_Items"), sql);
    }

    /**
     * Sql生成侧集成：未设置别名的表名保持原样。
     */
    @Test
    public void querySqlRender_PlainTableName_IsKept() {
        QuerySql query = new QuerySql("t_order");
        String sql = query.toSql(EDataSource.PostgreSql);
        assertTrue(sql.contains("\"t_order\""), sql);
        assertFalse(sql.contains("_obase_gen_alias"), sql);
    }

    /**
     * Sql生成侧集成：修改Sql（插入）的目标表名保持原样。
     */
    @Test
    public void changeSqlRender_InsertTarget_IsKept() {
        ChangeSql change = new ChangeSql("t_order", EChangeType.Insert);
        change.overwriteField("price", 100);
        String sql = change.toSql(EDataSource.SqlServer);
        assertTrue(sql.contains("[t_order]"), sql);
        assertFalse(sql.contains("_obase_gen_alias"), sql);
    }
}
