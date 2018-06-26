package cn.itcast.core.service.impl;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.service.ItemSearchService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        Map<String, Object> map = new HashMap();
        //1.按关键字查询（高亮显示）
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.查询品牌和规格列表
        if (categoryList.size() > 0) {
            map.putAll(searchBrandAndSpecList(String.valueOf(categoryList.get(0))));
        }
        return map;
    }

    @Override
    public List<Item> findItemListByGoodsIdAndStatus(Long id, String status) {
        ItemQuery itemQuery = new ItemQuery();
        cn.itcast.core.pojo.item.ItemQuery.Criteria createCriteria = itemQuery.createCriteria();
        createCriteria.andGoodsIdEqualTo(id);
        createCriteria.andStatusEqualTo(status);
        List<Item> list = itemDao.selectByExample(itemQuery);
        return list;
    }

    /**
     * 根据关键字搜索列表
     *
     * @param searchMap
     * @return
     */

    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        HighlightQuery query = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2 按商品分类过滤
        if (!"".equals(searchMap.get("category"))) {//如果用户选择了分类
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3 按品牌过滤
        if (!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4 按规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {

                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5按价格筛选.....
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            if (!price[0].equals("0")) {//如果区间起点不等于0
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!price[1].equals("*")) {//如果区间终点不等于*
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.6 分页查询
        Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
        if (pageNo == null) {
            pageNo = 1;//默认第一页
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize == null) {
            pageSize = 20;//默认20
        }
        query.setOffset((pageNo - 1) * pageSize);//从第几条记录查询
        query.setRows(pageSize);
        //1.7排序
        String sort = (String) searchMap.get("sort");//ASC DESC
        String sortField = (String) searchMap.get("sortField");//排序字段
        if (sort != null && !sort.equals("")) {
            if (sort.equals("ASC")) {
                Sort orders = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(orders);
            }
            if (sort.equals("DESC")) {
                Sort orders = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(orders);
            }
        }
        //***********  获取高亮结果集  ***********
        //高亮页对象
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(query, Item.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<Item>> entryList = page.getHighlighted();

        for (HighlightEntry<Item> entry : entryList) {
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
			/*
			for(Highlight h:highlightList){
				List<String> sns = h.getSnipplets();//每个域有可能存储多值
				System.out.println(sns);
			}*/
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                Item item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 查询分类列表
     *
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<String>();
        Query query = new SimpleQuery();
        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);
        //根据列得到分组结果集
        GroupResult<Item> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<Item>> content = groupEntries.getContent();
        for (GroupEntry<Item> entry : content) {
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询品牌和规格列表
     *
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);//获取模板ID
        if (typeId != null) {
            //根据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    @Autowired
    private ItemDao itemDao;

    @Override
    //导入审核通过的商品
    public void importList(List<Item> itemList) {
        if (itemList.size() > 0) {
            //导入索引库
            solrTemplate.saveBeans(itemList, 1000);
        }
    }

    @Override
    public void deleteByGoodsIds(long goodsId) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").is(goodsId);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
