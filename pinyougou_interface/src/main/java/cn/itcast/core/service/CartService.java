package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojogroup.Cart;

import java.util.List;

public interface CartService {
    //根据itemId 查询Item对象  商品价格  商家ID 商家的名称
    Item findItemById(Long itemId);

    //将购物车装满
    List<Cart> findCartList(List<Cart> cartList);

    //	将合并后的购物车  新车              再次合并到redis缓存中 老车
    void mergeCartList(List<Cart> newCartList, String name);

    //	查询缓存中购物车
    List<Cart> findCartListByName(String name);
}
