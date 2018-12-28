package com.pyg.cart.service;

import entity.Cart;

import java.util.List;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/21
 * @description:购物车服务接口
 */
public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );
    /**
     * 从 redis 中查询购物车
     * @param username
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);
    /**
     * 将购物车保存到 redis
     * @param username
     * @param cartList
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);
    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
