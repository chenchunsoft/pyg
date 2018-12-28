//购物车服务层
app.service('cartService',function($http){
    //购物车列表
    this.findCartList=function(){
        return $http.get('cart/findCartList.do');
    }

    //添加商品到购物车
    this.addGoodsToCartList=function(itemId,num){
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    }

    //求合计数
    this.sum=function(cartList){
        var totalValue={totalNum:0,totalMoney:0 };
        //遍历购物车列表对象
        for(var i=0;i<cartList.length ;i++){
            //拿到每一个购物车对象
            var cart=cartList[i];//购物车对象
            //遍历购物车的 购物车明细对象
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=  cart.orderItemList[j];//购物车明细
                //累加购物车明细对象中的数量 和 金额.
                totalValue.totalNum+=orderItem.num;//累加数量
                totalValue.totalMoney+=orderItem.totalFee;//累加金额
            }
        }
        return totalValue;
    }
//获取地址列表
    this.findAddressList=function(){
        console.log("cartService.js : findAddressList")
        return $http.get('address/findListByLoginUser.do');
    }

    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }
});