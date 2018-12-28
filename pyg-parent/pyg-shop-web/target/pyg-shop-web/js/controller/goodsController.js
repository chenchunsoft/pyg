 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,uploadService,itemCatService,typeTemplateService,$location){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	

//查询实体
	//通过location获得ID 自带方法叫做search()可以获得页面上传递的所有参数.
    $scope.findOne=function(){
        var id= $location.search()['id'];//获取参数名为id 的 参数值
        console.log("进入了goodController.js的findOne,ID为"+id);

        if(id==null){
            return ;
        }
        console.log(id);
        goodsService.findOne(id).success(

            function(response){

                $scope.entity= response;
                console.log("向富文本编辑器添加商品介绍");
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表 将一个 JSON 字符串转换为对象。
                console.log("向页面加载图片信息");
                $scope.entity.goodsDesc.itemImages=
                    JSON.parse($scope.entity.goodsDesc.itemImages);


                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems=  JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

                //SKU列表规格列转换
                for( var i=0;i<$scope.entity.itemList.length;i++ ){
                    $scope.entity.itemList[i].spec =
                        JSON.parse( $scope.entity.itemList[i].spec);
                }

            }
        );
    }

	
	//增加商品
	$scope.add=function(){
		//
		$scope.entity.goodsDesc.introduction=editor.html();

        goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("新增成功")
					$scope.entity={};
                    editor.html('');
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    /**
     *上传图片
     */
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(function(response) {
            if(response.success){//如果上传成功，取出url
                $scope.image_entity.url=response.message;//设置文件地址
            }else{
                alert(response.message);
            }
        }).error(function() {
            alert("上传发生错误");
        });
    };
    $scope.entity={goodsDesc:{itemImages:[],specificationItems:[]}};//定义页面实体结构
    //将当前上传的图片实体存入图片列表
    $scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    }
    //移除图片
	$scope.remove_image_entity=function (index) {
		$scope.entity.goodsDesc.itemImages.splice(index,1);
    }

    //第一个选择框 查询一级商品分类列表
	$scope.selectItemCat1List=function () {
		itemCatService.findByParentId(0).success(
			function (response) {
				$scope.itemCat1List=response;
        });
    }
    //第二个选择框 查询二级商品分类列表
	//watch用来检测指定的值 从而触发事件
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
		//根据选择值查询二级分类
		itemCatService.findByParentId(newValue).success(
			function (res) {
				$scope.itemCat2List=res;
                $scope.itemCat3List={};
            }
		);
    });
    //第三个选择框 读取三级分类
	$scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List=response;
            }
		);
    });
	//读取三级分类后,在读取分类后的模板ID
	$scope.$watch('entity.goods.category3Id',function (newValue, oldvalue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId=response.typeId;//更新模板ID
            }
		);
    });
	//读取模板ID后,读取品牌列表 当用户更新模板ID时,读取模板中的扩展属性
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		typeTemplateService.findOne(newValue).success(
			function (response) {
				$scope.typeTemplate=response;
				//数据库中读取的数据为字符串形式,将字符串转换为json对象.
				//品牌列表
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
                //避免数据冲突,当没有ID的时候加载模板中的扩展数据
                if($location.search()['id']==null){

                    //扩展属性
                    $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);

                }
            }
		);

		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(
			function (response) {

				$scope.specList=response;
            }
		);
    });
	$scope.updateSpecAttribute=function ($event, name, value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		//如果找到了
		if (object!=null){
			//如果被选中了
			if ($event.target.checked){
				object.attributeValue.push(value);
			}
			else {
				//反选
                object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除选项
				//如果所有选项都移除了,将此记录移除
				if (object.atributeValue.length==0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object),1);
				}
			}
		}//如果没找到
		else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
    }

    //创建SKU列表
    $scope.createItemList=function(){
        $scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0' } ];//初始
        var items=  $scope.entity.goodsDesc.specificationItems;
        for(var i=0;i< items.length;i++){
            $scope.entity.itemList = addColumn( $scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
        }
    }
//添加列值
    addColumn=function(list,columnName,conlumnValues){
        var newList=[];//新的集合
        for(var i=0;i<list.length;i++){
            var oldRow= list[i];
            for(var j=0;j<conlumnValues.length;j++){
//将集合中的每一个元素克隆给neewRow
                var newRow= JSON.parse( JSON.stringify( oldRow )  );//深克隆
                newRow.spec[columnName]=conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }
	//添加sate数组,用来记录状态信息
	$scope.status=['未审核','已审核','审核未通过','关闭']

	//设置一个商品列表集合
	$scope.itemCatList=[];
	
	//加载商品分类列表
	//一步查询,根据ID去查询后端,返回商品的名称
	$scope.findItemCatList=function () {
		//调用findALl()方法返回当前用户的所有商品记录.
		itemCatService.findAll().success(
			//对返回的商品列表进行遍历修改
			function (response) {
				for(var i=0;i<response.length;i++){
					//将名字,赋值给ID,这样显示的三级分类的ID就变成了名字
					$scope.itemCatList[response[i].id]=response[i].name;
				}
            }
		);
    }
//根据规格名称和选项名称返回是否被勾选
	//数据格式是这样的:[{"attributeName":"网络制式","attributeValue":["移动3G","移动4G"]},{"att
    $scope.checkAttributeValue=function(specName,optionName){
		//首先获得被选中项的list
        var items= $scope.entity.goodsDesc.specificationItems;
        //看items中的attributeName是否有指定参数specName 不是则为空 返回假. 是说明找到了指定attributeName
        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
        	//找到了attributeName之后,看看是否有匹配的attributeValue
			//使用indexof方法查找.如果存在该值肯定会大于0
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }
	//实现修改数据的保存 如果有ID就是修改,如果没有ID就是新增.
    $scope.save=function(){
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction=editor.html();
        var serviceObject;//服务层对象
        if($scope.entity.goods.id!=null){//如果有ID
            serviceObject=goodsService.update( $scope.entity ); //修改
        }else{
            serviceObject=goodsService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    alert('保存成功');
                    $scope.entity={};
                    editor.html("");
                   location.href="goods.html";//跳转到商品列表页
                }else{
                    alert(response.message);
                }
            }
        );
    }

});	
