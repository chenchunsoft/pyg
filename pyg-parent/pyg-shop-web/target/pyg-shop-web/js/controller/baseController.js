app.controller('baseController',function ($scope) {
    /**
     * 分页控制配置
     */
    $scope.paginationConf={
        currentPage:1,//当前页码
        totalItems:10,//总条数
        itemsPerPage:10,
        perPageOptions:[10,20,30,40,50],//野马选项
        onChange:function () {//onchange触发事件
            $scope.reloadList();//重新加载
        }
    };
    /**
     * 重新加载列表数据
     * 2018-11-30 00:33:53修改
     * 回滚
     */
    $scope.reloadList=function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage,
            $scope.paginationConf.itemsPerPage)
    }
    /**
     * 复选框的checked属性:用于判断是否被选中
     */
    //创建一个集合保存选中的ID
    $scope.selectIds=[];

    //刷新复选
    $scope.updateSelection=function ($event, id) {
        //如果小框框被选中,就添加进数组
        if ($event.target.checked){
            $scope.selectIds.push(id);
        }else{
            //如果没有被选中,就从集合中删除该元素
            var idx=$scope.selectIds.indexOf(id);
            //使用splice方法删除 参数为起始位置,删除的个数.
            $scope.selectIds.splice(idx,1);
        }
    }
    //提取json字符串数据中的某个属性,返回拼接字符串逗号分隔
    $scope.jsonToString=function (jsonString, key) {
        var json=JSON.parse(jsonString);//将json字符转换为json对象
        var value="";
        for(var i=0;i<json.length;i++){
            if(i>0){
                value+=","
            }
            value+=json[i][key]
        }
        return value;
    }

    //从集合中按照key查询对象,通用方法
    $scope.searchObjectByKey=function (list, key, keyValue) {
        for(var i=0;i<list.length;i++){
            if (list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }
});