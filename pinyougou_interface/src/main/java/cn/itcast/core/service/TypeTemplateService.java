package cn.itcast.core.service;

import cn.itcast.core.pojo.template.TypeTemplate;
import entity.PageResult;

public interface TypeTemplateService  {
    PageResult search(TypeTemplate typeTemplate, Integer pageNum, Integer pageSize);

    void add(TypeTemplate typeTemplate);

    TypeTemplate findOne(Long id);

    void update(TypeTemplate typeTemplate);

    void delete(Long[] ids);
}
