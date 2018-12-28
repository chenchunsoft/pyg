package com.pyg.page.service;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/14
 * @description:
 */
public interface ItemPageService {
    /**
     * 一个spu生成一个页面.
     * 生成商品详细页.
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
    /**
     * 删除商品详细页
     * @param
     * @return
     */
    /**
     * 删除商品详细页
     * @param
     * @return
     */
    public boolean deleteItemHtml(Long[] goodsIds);

}
