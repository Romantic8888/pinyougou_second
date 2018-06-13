package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 品牌管理
 */
@Service
@Transactional
public class BrandServiceImpl  implements BrandService {
    @Autowired
    private BrandDao brandDao;

    /**
     *
     * 查询所有
     * @return
     */
    @Override
    public List<Brand> findAll() {
        return brandDao.selectByExample(null);
    }

    /**
     * 分页
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Brand> page = (Page) brandDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 添加
     * @param brand
     */
    @Override
    public void add(Brand brand) {
        brandDao.insertSelective(brand);
    }

    /**
     *修改
     * @param brand
     */
    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    /**
     * 根据id查询一个品牌
     * @param id
     * @return
     */
    @Override
    public Brand findOne(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }

    /**
     *批量删除
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            brandDao.deleteByPrimaryKey(id);
        }
    }

    /**
     * 条件查询
     * @param pageNum
     * @param pageSize
     * @param brand
     * @return
     */
    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        //封装查询条件
        BrandQuery brandQuery = new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
        if (brand!=null) {
            if (null != brand.getName() && !"".equals(brand.getName().trim())) {
                criteria.andNameLike("%" + brand.getName().trim() + "%");
            }
            if (null != brand.getFirstChar() && !"".equals(brand.getFirstChar().trim())) {
                criteria.andFirstCharEqualTo(brand.getFirstChar().trim());
            }
        }
        //分页插件
        PageHelper.startPage(pageNum, pageSize);
        //排序
       // PageHelper.orderBy("id desc");
        //查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        return new PageResult(page.getTotal(), page.getResult());
    }

}
