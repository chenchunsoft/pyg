
// 定义模块:
var app = angular.module("pinyougou",[]);

/*$sce 服务写成过滤器
* $sce 引用服务文件
* */
app.filter('trustHtml',['$sce',function($sce){
    return function(data){
        return $sce.trustAsHtml(data);
    }
}]);