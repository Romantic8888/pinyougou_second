package cn.itcast.core.service.impl;

import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 模版管理
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {
    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Autowired
    private RedisTemplate redisTemplate;

    //查询
    @Override
    public PageResult search(TypeTemplate typeTemplate, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TypeTemplate> page = (Page<TypeTemplate>) typeTemplateDao.selectByExample(null);
        saveToRedis();//存入数据到缓存
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(TypeTemplate typeTemplate) {
        typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateDao.deleteByPrimaryKey(id);
        }
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        //查询模板
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        //规格属性  [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        List<Map> mapList = JSON.parseArray(typeTemplate.getSpecIds(), Map.class);
        for (Map map : mapList) {
            //查询规格选项列表
            SpecificationOptionQuery query = new SpecificationOptionQuery();
            SpecificationOptionQuery.Criteria criteria = query.createCriteria();
            criteria.andSpecIdEqualTo(new Long((Integer) map.get("id")));
            List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(query);
            map.put("options", specificationOptions);
        }
        return mapList;
    }

    /**
     * 将数据存入缓存
     */
    private void saveToRedis() {
        //获取模板数据
        List<TypeTemplate> typeTemplateList = findAll();
        //循环模板
        for (TypeTemplate typeTemplate : typeTemplateList) {
            //存储品牌列表
            List<Map> brandList = JSON.parseArray(typeTemplate.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(), brandList);
            //存储规格列表
            List<Map> specList = findBySpecList(typeTemplate.getId());//根据模板ID查询规格列表
            redisTemplate.boundHashOps("specList").put(typeTemplate.getId(), specList);
        }
    }

    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }
}
