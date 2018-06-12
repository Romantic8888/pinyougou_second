app.controller('brandController', function ($scope,$controller,brandService) {
        $controller("baseController",{$scope:$scope});//伪继承
    //读取列表数据绑定到表单中
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    };

    //分页
    $scope.findPage = function (page, rows) {
        brandService.findPage(page,rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        )
    }
    //新增品牌
    $scope.save = function () {
        var object = null;//方法名称
        if ($scope.entity.id != null) {//如果有id
            object=brandService.update($scope.entity);//则执行修改方法
        }else{
            object=brandService.add($scope.entity);//则执行添加方法
        }
        object.success(
            function (response) {
                if (response.flag) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        )
    }
    //查询一个商品
    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        )
    }


    //批量删除
    $scope.delete = function () {
        brandService.delete($scope.selectIds).success(
            function (response) {
                if (response.flag) {
                    //重新加载
                    $scope.reloadList();
                } else {
                    alert(
                        response.message
                    )
                }
            }
        )
    };
    $scope.searchEntity={};
    //查询 条件 带分页
    $scope.search=function (page,rows){
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems=response.total;//总记录数
                $scope.list=response.rows;//给列表变量赋值
            }
        )
    }
});