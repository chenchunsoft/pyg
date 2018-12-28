//登陆服务层
app.service('loginService',function ($http) {
    this.loginName=function () {
    //读取登陆人的姓名

        return $http.get('../login/name.do');
    }
});