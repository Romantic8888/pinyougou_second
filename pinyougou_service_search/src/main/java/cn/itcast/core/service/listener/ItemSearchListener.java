package cn.itcast.core.service.listener;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.service.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.List;

public class ItemSearchListener implements MessageListener {
    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String text = atm.getText();
            List<Item> list =itemSearchService.findItemListByGoodsIdAndStatus(Long.parseLong(text), "1");
            itemSearchService.importList(list);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
