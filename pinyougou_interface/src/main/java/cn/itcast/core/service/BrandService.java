package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Brand;
import entity.PageResult;

import java.util.List;

public interface BrandService {
    /**
     * 查询所有
     * @return
     */
    List<Brand> findAll();

    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    PageResult findPage(Integer pageNum,Integer pageSize);

    /**
     * 新建品牌
     * @param brand
     */
    void add(Brand brand);

    /**
     *更新品牌
     * @param brand
     */
    void update(Brand brand);

    /**
     * 根据id查询一个品牌
     * @param id
     * @return
     */
    Brand findOne(Long id);

    /**
     * 批量删除
     * @param ids
     */
    void delete (Long[] ids);

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    PageResult search(Integer pageNum,Integer pageSize,Brand brand);
}
