package com.pyg.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/11
 * @description:
 */
/**

 * 搜索

 * @param

 * @return

 */
public interface ItemSearchService {
    /**
     * 查询
     * @param searchMap
     * @return
     */
    public Map<String,Object> search(Map searchMap);

    /**
     * 导入数据
     * @param list
     */
    public void importList(List list);

    /**
     * 删除数据
     * @param goodsIdList
     */
    public void deleteByGoodsIds(List goodsIdList);




}


