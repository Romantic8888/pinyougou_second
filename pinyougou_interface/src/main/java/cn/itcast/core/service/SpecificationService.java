package cn.itcast.core.service;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojogroup.SpecificationVo;
import entity.PageResult;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    /**
     * 分页查询(带条件)
     * @param pageNum
     * @param pageSize
     * @param specification
     * @return
     */
    PageResult search(Integer pageNum, Integer pageSize, Specification specification);

    /**
     * 新增规格
     * @param specificationVo
     */
    void add(SpecificationVo specificationVo);

    /**
     *根据id查询一个实体
     * @param id
     * @return
     */
    SpecificationVo findOne(Long id);

    /**
     * 修改规格表及规格属性表
     * @param vo
     */
    void update(SpecificationVo vo);

    /**
     * 批量删除
     * @param ids
     */
    void delete(Long[] ids);

    List<Map> selectOptionList();
}
