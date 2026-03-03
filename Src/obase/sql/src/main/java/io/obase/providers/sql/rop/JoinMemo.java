/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：源联接备忘录.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-8 12:00:30
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.rop;

import io.obase.core.common.Utils;
import io.obase.providers.sql.sqlobject.MonomerSource;

import java.util.HashMap;
import java.util.Map;

/**
 * 源联接备忘录
 */
public class JoinMemo {

    /**
     * 在备忘录中添加一个节点别名及该节点的映射源
     */
    private final Map<String, MonomerSource> aliasSource = new HashMap<>();

    /**
     * 获取备忘录条数
     *
     * @return 备忘录条数
     */
    public int getCount() {
        return this.aliasSource.size();
    }

    /**
     * 在备忘录中添加一个节点别名及该节点的映射源
     *
     * @param nodeAlias 要添加的节点别名。当节点别名为null时，用空字符串作键
     * @param source    节点代表类型的映射源
     * @return 书否添加成功
     */
    public boolean append(String nodeAlias, MonomerSource source) {
        if (Utils.getStringIsEmpty(nodeAlias)) {
            nodeAlias = "";
        }
        if (this.aliasSource.containsKey(nodeAlias)) return false;
        this.aliasSource.put(nodeAlias, source);
        return true;
    }

    /**
     * 检查备忘录中是否存在指定的节点别名
     *
     * @param nodeAlias 要检查的节点别名。当节点别名为null时，用空字符串作键
     * @return 是否存在
     */
    public boolean exists(String nodeAlias) {
        if (Utils.getStringIsEmpty(nodeAlias)) {
            nodeAlias = "";
        }
        return this.aliasSource.containsKey(nodeAlias);
    }

    /**
     * 查询指定节点的代表类型的映射源
     *
     * @param nodeAlias 节点别名。当节点别名为null时，用空字符串作键
     * @return 节点的代表类型的映射源
     */
    public MonomerSource getSource(String nodeAlias) {
        if (Utils.getStringIsEmpty(nodeAlias)) {
            nodeAlias = "";
        }
        if (this.aliasSource.containsKey(nodeAlias)) return this.aliasSource.get(nodeAlias);
        if (this.aliasSource.containsKey(nodeAlias.toLowerCase())) return this.aliasSource.get(nodeAlias.toLowerCase());
        return null;
    }

    /**
     * 重置备忘录
     */
    public void reset() {
        this.aliasSource.clear();
    }
}
