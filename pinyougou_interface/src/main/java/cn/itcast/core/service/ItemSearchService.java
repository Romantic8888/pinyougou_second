package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 搜索
     * @param
     * @return
     */
    Map<String,Object> search(Map searchMap);

    List<Item> findItemListByGoodsIdAndStatus(Long id, String status);
    void importList(List<Item> itemList);

    void deleteByGoodsIds(long goodsId);
}
