package cn.itcast.core.user.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送消息
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {

        //随机生成6位验证码
        final String numeric = RandomStringUtils.randomNumeric(6);
        //将 验证码放入到redis缓存中
        redisTemplate.boundHashOps("smscode").put(phone,numeric);
        jmsTemplate.send(smsDestination,new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                // TODO Auto-generated method stub
                MapMessage map = session.createMapMessage();
                //手机号:
                map.setString("phone", phone);
                //验证码
                map.setString("templateParam", "{\"code\":\""+numeric+"\"}");
                //签名
                map.setString("signName", "BlackMamba");
                //模板
                map.setString("templateCode", "SMS_134185275");
                return map;
            }
        });
    }
    @Autowired
    private UserDao userDao;
    @Override
    public void add(User user, String smscode) {
        String code = (String) redisTemplate.boundHashOps("smscode").get(user.getPhone());
        if (null==code||!smscode.equals(code)){
            throw  new RuntimeException("您输入的验证码有误！");
        }
        user.setCreated(new Date());
        user.setUpdated(new Date());
        String md5Hex = DigestUtils.md5Hex(user.getPassword());//对密码进行加密
        user.setPassword(md5Hex);
        userDao.insertSelective(user);
    }
}
