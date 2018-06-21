package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.ContentCategory;
import entity.PageResult;

import java.util.List;

public interface ContentCategoryService {
    List<ContentCategory> findAll();

    PageResult findPage(ContentCategory contentCategory, Integer pageNum, Integer pageSize);

    void add(ContentCategory contentCategory);

    void edit(ContentCategory contentCategory);

    ContentCategory findOne(Long id);

    void delAll(Long[] ids);

}
