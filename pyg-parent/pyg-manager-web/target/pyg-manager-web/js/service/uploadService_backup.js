app.service('uploadService',function ($http) {
    this.uploadFile=function () {

        var formdata = new FormData();
        /**
         * //第一个file代表文件上传框的name 固定死的.
         * 第二个files[]取得第一个文件上传框
         */
        formdata.append('file',fine.files[0]);    }
        return $http({
           url:'../upload.do',
            mehtod:'post',
            data:'formdata',
            headers:{'Content-Type':undefined},
            transformRequest:angular.identity
        });
});