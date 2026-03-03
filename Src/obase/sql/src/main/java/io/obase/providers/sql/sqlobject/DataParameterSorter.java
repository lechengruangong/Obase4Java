/*
┌──────────────────────────────────────────────────────────────┐
│　描   述：参数重排器.
│　作   者：Obase开发团队
│　版权所有：武汉乐程软工科技有限公司
│　创建时间：2026-1-5 12:07:57
└──────────────────────────────────────────────────────────────┘
*/
package io.obase.providers.sql.sqlobject;

import java.util.List;

/**
 * 参数重排器
 */
public class DataParameterSorter {

    /**
     * 对参数进行重排序
     *
     * @param dataParameterList 要排序的列表
     */
    public static void sort(List<DataParameter> dataParameterList) {

        if (dataParameterList == null || dataParameterList.size() == 0)
            return;

        //重新排序Index 设为i+1
        for (int i = 0; i < dataParameterList.size(); i++) {
            dataParameterList.get(i).Index = (i + 1);
        }
    }
}
