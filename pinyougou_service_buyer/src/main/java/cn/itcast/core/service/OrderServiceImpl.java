package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojogroup.Cart;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private OrderItemDao orderItemDao;

    //保存订单表
    //保存订单详情表
    public void add(Order order){
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());
        for (Cart cart : cartList) {
            //订单ID
            long id = idWorker.nextId();
            order.setOrderId(id);
            //实付金额
            double totalPrice = 0f;
            //支付类型
            order.setPaymentType("1");
            //状态
            order.setStatus("0");
            //订单创建时间
            order.setCreateTime(new Date());
            //订单更新时间
            order.setUpdateTime(new Date());
            //来源
            order.setSourceType("2");
            //商家ID
            order.setSellerId(cart.getSellerId());
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
                //订单详情表
                //orderItemId
                long orderItemId = idWorker.nextId();
                orderItem.setId(orderItemId);
                //item_id
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //goods_id
                orderItem.setGoodsId(item.getGoodsId());
                //外键
                orderItem.setOrderId(order.getOrderId());
                //标题
                orderItem.setTitle(item.getTitle());
                //单价
                orderItem.setPrice(item.getPrice());
                //num
                //小计
                orderItem.setTotalFee
                        (new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //总计
                totalPrice += orderItem.getTotalFee().doubleValue();
                //商家Id
                orderItem.setSellerId(cart.getSellerId());
                //保存订单详情表
                orderItemDao.insertSelective(orderItem);

            }
            //设置实付金额
            order.setPayment(new BigDecimal(totalPrice));
            //保存订单
            orderDao.insertSelective(order);
        }
    }
}
