/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql别名替换器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-9-1
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.common;

import java.util.Set;

/**
 * Sql别名替换器。
 * 在Sql字符串生成完成后，按"原始别名→短别名"映射字典对Sql字符串做标识符级替换：
 * （1）仅替换对象模型确认的别名（SqlAliasCollector收集的白名单），避免误伤真实表名/列名；
 * （2）识别各数据库的标识符定界形式：双引号"..."、反引号`...`、方括号[...]、裸标识符；
 * （3）跳过单引号字符串字面量，避免误伤字面量文本；
 * （4）对完整标识符做精确匹配，避免子串误伤（如"_Order_Items"不会匹配"_Order_Items_Price"）。
 */
public class SqlAliasReplacer {

    private SqlAliasReplacer() {
    }

    /**
     * 按别名映射字典替换Sql字符串中的别名。
     *
     * @param sql     Sql字符串
     * @param aliases 对象模型确认的别名白名单
     * @return 替换后的Sql字符串
     */
    public static String replace(String sql, Set<String> aliases) {
        if (sql == null || sql.isEmpty() || aliases == null || aliases.isEmpty()) return sql;

        StringBuilder sb = new StringBuilder(sql.length());
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (c == '\'') {
                //单引号字符串字面量 原样复制
                i = copyStringLiteral(sql, i, sb);
            } else if (c == '"' || c == '`') {
                //双引号/反引号定界标识符
                i = copyDelimitedIdentifier(sql, i, sb, c, aliases);
            } else if (c == '[') {
                //方括号定界标识符(SqlServer)
                i = copyBracketIdentifier(sql, i, sb, aliases);
            } else if (isIdentifierChar(c)) {
                //裸标识符
                i = copyBareIdentifier(sql, i, sb, aliases);
            } else {
                sb.append(c);
                i++;
            }
        }

        return sb.toString();
    }

    /**
     * 原样复制单引号字符串字面量（含''转义）。
     *
     * @param sql Sql字符串
     * @param i   当前位置
     * @param sb  输出
     * @return 复制后的位置
     */
    private static int copyStringLiteral(String sql, int i, StringBuilder sb) {
        int start = i;
        i++; //跳过开引号
        while (i < sql.length()) {
            if (sql.charAt(i) == '\'') {
                if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    i += 2; //''转义
                    continue;
                }
                i++; //闭合引号
                break;
            }
            i++;
        }
        sb.append(sql, start, i);
        return i;
    }

    /**
     * 复制定界标识符，若内部文本为别名则替换为短别名。
     *
     * @param sql       Sql字符串
     * @param i         当前位置
     * @param sb        输出
     * @param delimiter 定界符（双引号或反引号）
     * @param aliases   别名白名单
     * @return 复制后的位置
     */
    private static int copyDelimitedIdentifier(String sql, int i, StringBuilder sb, char delimiter, Set<String> aliases) {
        int start = i;
        i++; //跳过开定界符
        int contentStart = i;
        while (i < sql.length() && sql.charAt(i) != delimiter) i++;
        String content = sql.substring(contentStart, i);
        if (i < sql.length()) i++; //闭合定界符

        if (aliases.contains(content)) {
            String shortName = SqlAliasShortener.getShort(content);
            if (!shortName.equals(content)) {
                sb.append(delimiter).append(shortName).append(delimiter);
                return i;
            }
        }

        sb.append(sql, start, i);
        return i;
    }

    /**
     * 复制方括号定界标识符，若内部文本为别名则替换为短别名。
     *
     * @param sql     Sql字符串
     * @param i       当前位置
     * @param sb      输出
     * @param aliases 别名白名单
     * @return 复制后的位置
     */
    private static int copyBracketIdentifier(String sql, int i, StringBuilder sb, Set<String> aliases) {
        int start = i;
        i++; //跳过'['
        int contentStart = i;
        while (i < sql.length() && sql.charAt(i) != ']') i++;
        String content = sql.substring(contentStart, i);
        if (i < sql.length()) i++; //跳过']'

        if (aliases.contains(content)) {
            String shortName = SqlAliasShortener.getShort(content);
            if (!shortName.equals(content)) {
                sb.append('[').append(shortName).append(']');
                return i;
            }
        }

        sb.append(sql, start, i);
        return i;
    }

    /**
     * 复制裸标识符，若完整标识符为别名则替换为短别名。
     *
     * @param sql     Sql字符串
     * @param i       当前位置
     * @param sb      输出
     * @param aliases 别名白名单
     * @return 复制后的位置
     */
    private static int copyBareIdentifier(String sql, int i, StringBuilder sb, Set<String> aliases) {
        int start = i;
        while (i < sql.length() && isIdentifierChar(sql.charAt(i))) i++;
        String token = sql.substring(start, i);

        if (aliases.contains(token)) {
            String shortName = SqlAliasShortener.getShort(token);
            if (!shortName.equals(token)) {
                sb.append(shortName);
                return i;
            }
        }

        sb.append(token);
        return i;
    }

    /**
     * 判定字符是否为标识符字符。
     *
     * @param c 字符
     * @return 是否为标识符字符
     */
    private static boolean isIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '$' || c == '#';
    }
}
