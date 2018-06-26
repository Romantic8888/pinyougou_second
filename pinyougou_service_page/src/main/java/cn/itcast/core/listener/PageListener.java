package cn.itcast.core.listener;

import cn.itcast.core.service.ItemPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener  implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String goodsId = atm.getText();
            itemPageService.createItemPage(Long.parseLong(goodsId));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
