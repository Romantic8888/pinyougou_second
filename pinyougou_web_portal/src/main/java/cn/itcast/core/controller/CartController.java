package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojogroup.Cart;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    //加入购物车
    @RequestMapping("/addGoodsToCartList")
    //缺省默认就是true
    @CrossOrigin(allowCredentials = "true",origins = "http://localhost:9103")
    public Result addGoodsToCartList(Long itemId, Integer num,HttpServletRequest request, HttpServletResponse response) {
        //老方案
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9103");
        //response.setHeader("Access-Control-Allow-Credentials", "true");
        //判断Cookie 中 是否有购物车
        Boolean k = false;
        try {
            // 购物车集合
            List<Cart> cartList = null;
            // 未登陆
            Cookie[] cookies = request.getCookies();
            if (null != cookies && cookies.length > 0) {
                // 1:获取Cookie中购物车
                for (Cookie cookie : cookies) {
                    if ("BUYERCART".equals(cookie.getName())) {
                        // 有
                        cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                        k = true;
                        break;
                    }
                }
            }
            // 2:没有 创建购物车
            if (null == cartList) {
                cartList = new ArrayList<Cart>();
            }
            // 3:追加当前款 Long itemId,Integer num
            Item item = cartService.findItemById(itemId);
            // 购物车
            Cart cart = new Cart();
            // 商家ID
            cart.setSellerId(item.getSellerId());
            // 商品集合
            List<OrderItem> orderItemList = new ArrayList<>();
            OrderItem orderItem = new OrderItem();
            // itemId
            orderItem.setItemId(itemId);
            // 数量
            orderItem.setNum(num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            // 1) 判断当前款的商品的商家 是否已经在购物车集合中已经存在
            int sellerIndexOf = cartList.indexOf(cart);
            if (sellerIndexOf != -1) {
                // 1:存在
                // 2):判断此商家下所有商品中是否已经有此商品了
                Cart oldCart = cartList.get(sellerIndexOf);

                int itemIndexOf = oldCart.getOrderItemList().indexOf(orderItem);
                if (itemIndexOf != -1) {
                    // 1:有 追加数量
                    OrderItem oldOrderItem = oldCart.getOrderItemList().get(itemIndexOf);
                    oldOrderItem.setNum(oldOrderItem.getNum() + orderItem.getNum());
                } else {
                    // 2:没有 添加新商品
                    oldCart.getOrderItemList().add(orderItem);
                }
            } else {
                // 2:不存在
                // 创建新商家 新建一个购物车
                // 购物车里面 商家ID 商品ID 商品数量
                cartList.add(cart);
            }
            // 登陆人
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser".equals(name)) {
                // 登陆了
//				4:将合并后的购物车再次合并到redis缓存中
                cartService.mergeCartList(cartList, name);
//				5: Cookie中如果有购物车 清空Cookie
                if(k){
                    //清空Cookie
                    Cookie c = new Cookie("BUYERCART",null);
                    // -1关闭浏览器销毁   0 立即马上销毁  >0  就了时间再销毁
                    c.setMaxAge(0);
                    c.setPath("/");
                    response.addCookie(c);
                }
            } else {
                // 未登陆
                // 4:创建Cookie 将购物车添加到Cookie中 Cookie字符串 将上面的购物车集合转成Json字符串 长度非常长
                Cookie cookie = new Cookie("BUYERCART", JSON.toJSONString(cartList));
                cookie.setMaxAge(60 * 60 * 24);
                cookie.setPath("/");
                // 5:写回浏览器
                response.addCookie(cookie);
            }

            return new Result(true, "加入购物车成功");
        } catch (Exception e) {
            // TODO: handle exception
            return new Result(false, "加入购物车失败");
        }
    }

    // 查询购物车列表
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest request, HttpServletResponse response) {
        // 购物车集合
        List<Cart> cartList = null;
        Cookie[] cookies = request.getCookies();
        if (null != cookies && cookies.length > 0) {
            // 1:获取Cookie中购物车
            for (Cookie cookie : cookies) {
                if ("BUYERCART".equals(cookie.getName())) {
                    // 有
                    cartList = JSON.parseArray(cookie.getValue(), Cart.class);
                    break;
                }
            }
        }
        // 登陆人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)) {
            //登陆了
//			2: 有  将购物车合并到缓存中购物车
            if(cartList != null){
                cartService.mergeCartList(cartList, name);
                //清空Cookie
                Cookie c = new Cookie("BUYERCART",null);
                // -1关闭浏览器销毁   0 立即马上销毁  >0  就了时间再销毁
                c.setMaxAge(0);
                c.setPath("/");
                response.addCookie(c);
            }
//			3:查询缓存中购物车
            cartList  = cartService.findCartListByName(name);

        }
        // 4:有 将购物车装满
        if (null != cartList) {
            cartList = cartService.findCartList(cartList);

        }
        // 5:回显购物车集合到页面上
        return cartList;

    }

}
