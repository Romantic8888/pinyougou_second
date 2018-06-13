package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;

public interface SpecificationService {
    /**
     * 分页查询(带条件)
     * @param pageNum
     * @param pageSize
     * @param specification
     * @return
     */
    PageResult search(Integer pageNum, Integer pageSize, Specification specification);
}
