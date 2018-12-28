//广告控制层（运营商后台）查询广告分类ID,网页初始化时默认访问ID为1的的广告
app.controller("contentController",function($scope,contentService){

    $scope.contentList=[];//广告集合

    $scope.findByCategoryId=function(categoryId){
        contentService.findByCategoryId(categoryId).success(
            function(response){
                $scope.contentList[categoryId]=response;
            }
        );
    }
    //传递参数搜索
    $scope.search=function () {
        console.log("contentController.js  keywords为"+$scope.keywords)
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;

    }
});
