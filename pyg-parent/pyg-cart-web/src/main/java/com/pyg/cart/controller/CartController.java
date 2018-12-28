package com.pyg.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pyg.cart.service.CartService;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/22
 * @description:
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout=6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    /**
     * 购物车列表
     * @param
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录人是: "+username);
        String cartListString = util.CookieUtil.getCookieValue(request, "cartList","UTF-8");
        if(cartListString==null || cartListString.equals("")){
            cartListString="[]";
        }
        //转换成Cart类型的list
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username.equals("anonymousUser")){
            //如果未登录
            //返回的是一个json格式的string
            System.out.println("从cookie 中读取数据.");

            return cartList_cookie;
        }else {
            //如果登陆了 从redis中提取购物车列表
            System.out.println("从redis中提取购物车列表");
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);

           if (cartList_cookie.size()>0){
               System.out.println("合并购物车列表");
               List<Cart> cartList = cartService.mergeCartList(cartList_cookie, cartList_redis);
               //合并后存储到redis中
               cartService.saveCartListToRedis(username,cartList);
               //存储到了redis后可以将本地的cookie删除了,不然重复了
               util.CookieUtil.deleteCookie(request,response,"cartList");
               return cartList;

           }
            return cartList_redis;

        }


    }

    /**
     * 添加商品到购物车
     * @param
     * @param
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("/addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId,Integer num) {
        //添加头信息 允许访问的域
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录用户：" + username);

        try {
            List<Cart> cartList = findCartList();//获取购物车列表
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            System.out.println("cartList中的值为:"+JSON.toJSONString(cartList));
            if (username.equals("anonymousUser")) { //如果是未登录，保存到 cookie
                util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
                System.out.println("向 cookie 存入数据");
            } else {//如果是已登录，保存到 redis
                System.out.println("保存到redis");
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "添加成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }
    }


}
