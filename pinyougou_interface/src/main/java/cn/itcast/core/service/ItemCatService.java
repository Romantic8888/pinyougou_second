package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface ItemCatService {
    /**
     * 根据parentId查询分类
     * @param parentId
     * @return
     */
    List<ItemCat> findByParentId(Long parentId);

    List<Map> selectTypeTemplateList();

    void add(ItemCat itemCat);

    ItemCat findOne(Long id);

    void update(ItemCat itemCat);

    void delete(Long[] ids);

    PageResult search(ItemCat itemCat, Integer page, Integer rows);
}
