app.service('seckillGoodsService',function($http){
//读取列表数据绑定到表单中
    this.findList=function(){
        return $http.get('seckillGoods/findList.do');
    }
});