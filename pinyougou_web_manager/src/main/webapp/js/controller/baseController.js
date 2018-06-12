app.controller("baseController",function ($scope) {
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
})