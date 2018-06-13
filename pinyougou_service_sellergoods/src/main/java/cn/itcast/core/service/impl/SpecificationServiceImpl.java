package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
    @Autowired
    private SpecificationDao specificationDao;

    /**
     * 带条件的分页查询
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
}
