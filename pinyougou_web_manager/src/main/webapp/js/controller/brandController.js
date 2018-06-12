app.controller('brandController', function ($scope, brandService) {
    //读取列表数据绑定到表单中
    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        )
    };
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,//当前页码
        totalItems: 0,//总条数
        itemsPerPage: 5,//每页默认显示多少条记录
        perPageOptions: [5, 10, 20, 30, 40, 50],//页码选项
        onChange: function () {//更改页面时触发事件
            $scope.reloadList();//重新加载
        }
    };
    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);

    }
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

    $scope.selectIds = [];//选中的集合
    //更新复选
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {//如过是被选中，则增加到数组中
            $scope.selectIds.push(id);//数组的push方法：向数组中添加元素
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//数组的splice方法：从数组的指定位置移除指定个数的元素 ，参数1为位置  ，参数2位移除的个数
        }
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