package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RedisTemplate redisTemplate;

    //根据itemId 查询Item对象  商品价格  商家ID 商家的名称
    public Item findItemById(Long itemId){
        return itemDao.selectByPrimaryKey(itemId);
    }
    //将购物车装满
    public List<Cart> findCartList(List<Cart> cartList){
        //遍历购物车集合
        for (Cart cart : cartList) {

            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //商家名称
                cart.setNickName(item.getSeller());
                //商品图片
                orderItem.setPicPath(item.getImage());
                //商品标题
                orderItem.setTitle(item.getTitle());
                //价格
                orderItem.setPrice(item.getPrice());
                //数量  已有
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
            }

        }
        return cartList;
    }

    //	将合并后的购物车  新车              再次合并到redis缓存中 老车
    public void mergeCartList(List<Cart> newCartList,String name){
        //获取缓存中的购物车集合  老合
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
        //将新车 合并到老车中
        oldCartList = mergeNewCartListToOldCartList(newCartList,oldCartList);
        //再把老车  再次保存到缓存中
        redisTemplate.boundHashOps("CART").put(name, oldCartList);

    }
    //将新车 合并到老车中
    public List<Cart> mergeNewCartListToOldCartList(List<Cart> newCartList,List<Cart> oldCartList){
        //如果新车为NULL
        if(null !=  newCartList){
            //如果老车为NULL
            if(null != oldCartList){
                //新车 老车都不为NULl 正式开始合并了
                //遍历新车
                for (Cart newCart : newCartList) {
                    int sellerIndexOf = oldCartList.indexOf(newCart);
                    //判断是否为 -1
                    if(sellerIndexOf != -1){
                        //有此商家    将此商家中的所有商品遍历  取出每一个商品合并到此商家的中去
                        Cart oldCart = oldCartList.get(sellerIndexOf);

                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();
                        for (OrderItem newOrderItem : newOrderItemList) {
                            int orderItemIndexOf = oldCart.getOrderItemList().indexOf(newOrderItem);
                            //判断是否为-1
                            if(orderItemIndexOf != -1){
                                //此商品在老车的商品集合中是存在 的  数量追加
                                OrderItem oldOrderItem = oldCart.getOrderItemList().get(orderItemIndexOf);
                                oldOrderItem.setNum(oldOrderItem.getNum() + newOrderItem.getNum());
                            }else{
                                //此商品在老车的商品集合中是不存在 的  添加新商品
                                oldCart.getOrderItemList().add(newOrderItem);
                            }
                        }
                    }else{
                        //没有此商家
                        oldCartList.add(newCart);
                    }
                }
            }else{
                //返回新车
                return newCartList;
            }
        }
        //返回老车
        return oldCartList;
    }
    //	查询缓存中购物车
    public List<Cart> findCartListByName(String name){
        return (List<Cart>) redisTemplate.boundHashOps("CART").get(name);
    }

}
