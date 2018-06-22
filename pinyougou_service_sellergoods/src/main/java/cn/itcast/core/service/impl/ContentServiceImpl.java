package cn.itcast.core.service.impl;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import cn.itcast.core.service.ContentService;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentDao contentDao;

    @Override
    public List<Content> findAll() {
        List<Content> list = contentDao.selectByExample(null);
        return list;
    }

    @Override
    public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<Content> page = (Page<Content>) contentDao.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void add(Content content) {
        contentDao.insertSelective(content);
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }

    @Override
    public void edit(Content content) {
        Content c = contentDao.selectByPrimaryKey(content.getId());
        contentDao.updateByPrimaryKeySelective(content);
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());

        if(!c.getCategoryId().equals(content.getCategoryId())){
            redisTemplate.boundHashOps("content").delete(c.getCategoryId());
        }


    }

    @Override
    public Content findOne(Long id) {
        Content content = contentDao.selectByPrimaryKey(id);
        return content;
    }

    @Override
    public void delAll(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                Content content = contentDao.selectByPrimaryKey(id);

                contentDao.deleteByPrimaryKey(id);

                redisTemplate.boundHashOps("content").delete(content.getCategoryId());

            }
        }
    }

    /**
     *
     * @param categoryId
     * @return
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public List<Content> findByCategoryId(Long categoryId) {
        List<Content> contents = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        if (null==contents){
            ContentQuery query = new ContentQuery();
            query.createCriteria().andCategoryIdEqualTo(categoryId)
                    .andStatusEqualTo("1");
            query.setOrderByClause("sort_order desc");
            contents=contentDao.selectByExample(query);
            redisTemplate.boundHashOps("content").put(categoryId,contents);
        }
        return  contents;
     }

}

