package cn.itcast.core.service.listener;

import cn.itcast.core.service.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage)message;
        try {
            String idStr = atm.getText();
            itemSearchService.deleteByGoodsIds(Long.parseLong(idStr));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
