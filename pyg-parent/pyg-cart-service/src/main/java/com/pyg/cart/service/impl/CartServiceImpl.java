package com.pyg.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.cart.service.CartService;
import com.pyg.mapper.TbItemMapper;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/21
 * @description:
 */
@Service
public class CartServiceImpl implements CartService {

    //用来查询商品明细对象
    @Autowired
    private TbItemMapper itemMapper;
    //将读取 保存到reids
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据skuID查询商品明细SKU的对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item==null){
            throw new RuntimeException("没有找到该商品的SKU数据");
        }
      /*  if(item.getStatus().equals("1")){
            throw new RuntimeException("商品状态异常");
        }*/
        //2.根据SKU对象得到商家ID 购物车分组以ID为区分.
        String sellerId = item.getSellerId();
        //3.根据商家ID在购物车列表中查询购物车对象  (添加商品所属的商家 在列表存在 ) (添加商品所属的商家 在列表不存在 新建)
        //提取一个查询方法用来进行查询
        Cart cart=null;
         cart = SearchCarBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart==null){
           //4.1 创建了一个新的购物车对象
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List<TbOrderItem> orderItemList = new ArrayList<>();//创建建购物车明细列表
            TbOrderItem orderitem = createOrderitem(item, num);

            orderItemList.add(orderitem);
            cart.setOrderItemList(orderItemList);
            //4.2 将新的购物车对象添加到购物车列表中
            if (cartList==null){
                cartList=new ArrayList<>();
            }
            cartList.add(cart);

        }else {
            //5.如果购物车列表中存在该商家的购物车
            //判断该商品是否在该购物车的明细列表中存在
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem==null){
                orderItem=createOrderitem(item,num);
                cart.getOrderItemList().add(orderItem);

            }else {
                //5.2如果存在,在原有的数量上添加数量,并且更新金额.
                orderItem.setNum(orderItem.getNum()+num);//更改数量
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                //当明细的数量小于0
                if (orderItem.getNum()<=0){
                        cart.getOrderItemList().remove(orderItem);
                }
                //当购物车的明细数量为0  移除该购物车 避免显示空的购物车.
                if (cart.getOrderItemList().size()==0){
                    cartList.remove(cart);
                }
            }



        }



        return cartList;
    }

    /**
     * 从redis中查找购物车
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {

        try{
            List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
            return cartList;
        }catch (Exception e){
            System.out.println("用户名为"+username+"的购物车不存在!");
            List<Cart> cartList=new ArrayList();
            return cartList;
        }
//        System.out.println("从 redis 中提取购物车数据....."+username);
//        List<Cart> cartList = (List<Cart>)
//                redisTemplate.boundHashOps("cartList").get(username);
//        if(cartList==null){
//            cartList=new ArrayList();
//        }
//        return cartList;

    }

    /**
     * 将购物车保存在redis中
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向 redis 存入购物车数据....."+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    /**
     * 根据商家ID 在购物车列表中 查询购物车对象
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart SearchCarBySellerId(List<Cart> cartList,String sellerId){
        if (cartList==null){
            return null;
        }
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    /**
     *
     * @param item
     * @param num
     * @return/创建购物车明细对象
     */
    private  TbOrderItem createOrderitem(TbItem item,int num){
        //创建购物车明细对象
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;
    }

    /**
     * 根据itemId SKUid 查询购物车明细对象 在购物车明细列表中
     * @param orderItemList
     * @param itemId
     * @return
     */
    public TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList ,Long itemId){
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue()==itemId){
                return orderItem;
            }
        }
        return null;
    }

    /**
     * cookie与reids购物车合并  将cartList2添加到cartList1中
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        System.out.println("合并购物车");
        if (cartList1==null){
            return cartList2;
        }
        if (cartList2==null){
            return cartList1;
        }
        for(Cart cart: cartList2){
            for(TbOrderItem orderItem:cart.getOrderItemList()){
                cartList1= addGoodsToCartList(cartList1,orderItem.getItemId(),orderItem.getNum());
            }
        }
        return cartList1;
    }


}
