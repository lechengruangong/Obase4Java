/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：Sql别名缩短器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-9-2 09:56:29
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sql别名缩短器。
 * 部分数据库（如PostgreSql、Oracle、MySql等）对标识符（表别名、列别名）的长度有限制，
 * 别名过长时会被数据库截断，导致SQL语句异常。
 * 本类维护"原始别名↔短别名"的映射字典（_obase_gen_alias + 唯一哈希，总长度40以下），
 * Sql生成侧与结果读取侧（DataRow）共用该字典进行转换，保证对应关系不被破坏。
 * 实施要点：
 * （1）仅缩短规则生成的别名（以'_'开头）：非下划线前缀的名称（如字段名、派生表名、视图目标字段等）
 * 会被Sql的其它部分按原名引用（如投影后继续筛选时引用投影列名），缩短会破坏这些引用，故保持原样；
 * （2）使用SHA-256取前16位十六进制作为哈希，确定性且跨进程稳定（不可使用String.hashCode，其每次进程随机化）；
 * （3）生成的短别名全部小写、纯ASCII、以'_'开头，是各数据库的合法标识符；
 * （4）幂等：已以"_obase_gen_alias"开头的别名直接返回，避免重复缩短。
 */
public class SqlAliasShortener {

    /**
     * 短别名前缀。
     */
    public static final String PREFIX = "_obase_gen_alias";

    /**
     * 哈希长度（十六进制字符数）。
     */
    public static final int HASH_LENGTH = 16;

    /**
     * 生成短别名的最大总长度，即"前缀+哈希"的长度，恒小于40。
     */
    public static final int MAX_GENERATED_LENGTH = 32;

    /**
     * 原始别名到短别名的映射字典，Sql生成侧与DataRow读取侧共用。
     */
    private static final ConcurrentHashMap<String, String> MAPPING_CACHE = new ConcurrentHashMap<>();

    /**
     * 私有构造 防止外部初始化
     */
    private SqlAliasShortener() {
    }

    /**
     * 获取指定原始别名对应的短别名（未超过映射规则时返回原值），并缓存于映射字典。
     *
     * @param alias 原始别名
     * @return 短别名
     */
    public static String getShort(String alias) {
        if (alias == null || alias.isEmpty()) return alias;
        String shortAlias = MAPPING_CACHE.get(alias);
        if (shortAlias == null) {
            shortAlias = computeShort(alias);
            String existing = MAPPING_CACHE.putIfAbsent(alias, shortAlias);
            if (existing != null) shortAlias = existing;
        }
        return shortAlias;
    }

    /**
     * 缩短指定的别名：规则生成的别名（以'_'开头）统一替换为"_obase_gen_alias+哈希"，其余名称保持原样。
     *
     * @param alias 原始别名
     * @return 短别名
     */
    public static String shorten(String alias) {
        return getShort(alias);
    }

    /**
     * 计算别名的唯一哈希（SHA-256前16位十六进制，共64位，碰撞概率可忽略）。
     *
     * @param value 原始别名
     * @return 哈希
     */
    private static String computeHash(String value) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(HASH_LENGTH);
            for (int i = 0; i < HASH_LENGTH / 2; i++)
                sb.append(String.format("%02x", bytes[i]));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256算法不可用", e);
        }
    }

    /**
     * 计算原始别名到短别名的映射。
     *
     * @param alias 原始别名
     * @return 短别名
     */
    private static String computeShort(String alias) {
        //已生成过短别名 幂等返回
        if (alias.startsWith(PREFIX)) return alias;
        //仅缩短规则生成的别名（下划线前缀） 其余名称（字段名/派生表名等）保持原样
        if (!alias.startsWith("_")) return alias;
        //生成"前缀+哈希"
        return PREFIX + computeHash(alias);
    }
}
