package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.pojogroup.Cart;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    @Autowired
    private PayLogDao payLogDao;

    //保存订单表
    //保存订单详情表
    public void add(Order order){

        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART").get(order.getUserId());
        //多张订单的总金额
        double totalFee = 0f;
        List<Long> orderList = new ArrayList<>();
        for (Cart cart : cartList) {
            //订单ID
            long id = idWorker.nextId();
            orderList.add(id);
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

                //总金额
                totalFee += totalPrice;

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
        //清空Redis缓存中的购物车
        redisTemplate.boundHashOps("CART").delete(order.getUserId());


//		添加支付日志表
        PayLog payLog = new PayLog();
//		ID       创建时间           用户ID   支付状态  支付类型       订单集合                    总金额
//		13132113  当前时间           315      0         1:微信支付   23545235,4253245325          21万多
        long outTradeNo = idWorker.nextId();
        payLog.setOutTradeNo(String.valueOf(outTradeNo));
        payLog.setCreateTime(new Date());
        payLog.setUserId(order.getUserId());
        payLog.setTradeState("0");
        payLog.setPayType("1");
        payLog.setTotalFee((long)(totalFee*100));
        payLog.setOrderList(orderList.toString().replace("[", "").replace("]", ""));
        payLogDao.insertSelective(payLog);
        //支付速度快  添加缓存中
        redisTemplate.boundHashOps("payLog").put(order.getUserId(), payLog);
    }
}
