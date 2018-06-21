package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import entity.PageResult;

import java.util.List;

public interface ContentService {
    List<Content> findAll();

    PageResult findPage(Content content, Integer pageNum, Integer pageSize);

    void add(Content content);

    void edit(Content content);

    Content findOne(Long id);

    void delAll(Long[] ids);

}
