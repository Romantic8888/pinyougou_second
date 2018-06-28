package cn.itcast.core.user.service;

import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;

    /**
     * 发送消息
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {

        //随机生成6位验证码
        final String numeric = RandomStringUtils.randomNumeric(6);
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
}
