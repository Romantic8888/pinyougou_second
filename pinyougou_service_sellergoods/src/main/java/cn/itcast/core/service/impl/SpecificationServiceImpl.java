package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.pojogroup.SpecificationVo;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specificationDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    /**
     * 带条件的分页查询
     *
     * @param pageNum
     * @param pageSize
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Specification specification) {
        SpecificationQuery query = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = query.createCriteria();
        if (null != specification.getSpecName() && !"".equals(specification.getSpecName().trim())) {
            criteria.andSpecNameLike("%" + specification.getSpecName().trim() + "%");
        }
        //开启分页
        PageHelper.startPage(pageNum, pageSize);
        Page<Specification> page = (Page<Specification>) specificationDao.selectByExample(query);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 新增规格
     *
     * @param specificationVo
     */
    @Override
    public void add(SpecificationVo specificationVo) {
        //保存规格表 并返回规格的ID
        specificationDao.insertSelective(specificationVo.getSpecification());
        //保存规格属性表  多个
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //设置规格表的ID  规格属性表的外键
            specificationOption.setSpecId(specificationVo.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    /**
     * 根据id查询一个实体
     *
     * @param id
     * @return
     */
    //通过规格ID 查询规格表 及规格属性表结果集
    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo vo = new SpecificationVo();
        vo.setSpecification(specificationDao.selectByPrimaryKey(id));
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(id);
        vo.setSpecificationOptionList(specificationOptionDao.selectByExample(query));
        return vo;
    }

    /**
     * 修改规格表及规格属性表
     *
     * @param vo
     */
    @Override
    public void update(SpecificationVo vo) {
        //修改规格表
        specificationDao.updateByPrimaryKeySelective(vo.getSpecification());
        //删除规格属性表
        SpecificationOptionQuery query = new SpecificationOptionQuery();
        query.createCriteria().andSpecIdEqualTo(vo.getSpecification().getId());
        specificationOptionDao.deleteByExample(query);
        //再保存规格属性表
        List<SpecificationOption> specificationOptionList = vo.getSpecificationOptionList();
        for (SpecificationOption specificationOption : specificationOptionList) {
            //设置规格表的ID  规格属性表的外键
            specificationOption.setSpecId(vo.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }

    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationDao.deleteByPrimaryKey(id);
            //删除规格属性表
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            query.createCriteria().andSpecIdEqualTo(id);
            specificationOptionDao.deleteByExample(query);
        }

    }

    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
