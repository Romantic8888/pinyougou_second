package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojogroup.GoodsVo;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.*;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private BrandDao brandDao;

    @Override
    public void add(GoodsVo vo) {
        //设置商品未审核状态
        vo.getGoods().setAuditStatus("0");
        goodsDao.insertSelective(vo.getGoods());
        //设置ID
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
        //商品详情表保存
        goodsDescDao.insertSelective(vo.getGoodsDesc());
        //保存库存表
        checkIsEnableSpec(vo);
    }

    //设置库存对象的属性
    public void setAttribute(Item item, GoodsVo vo) {
        List<Map> list = JSON.parseArray(vo.getGoodsDesc().getItemImages(), Map.class);
        if (null != list && list.size() > 0) {
            //图片
            item.setImage((String) list.get(0).get("url"));
        }
        //商品分类 第三级分类的ID
        item.setCategoryid(vo.getGoods().getCategory3Id());
        //商品分类第三级分类的名称
        item.setCategory(itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id()).getName());
        //添加时间
        item.setCreateTime(new Date());
        //更新时间
        item.setUpdateTime(new Date());
        //外键
        item.setGoodsId(vo.getGoods().getId());
        //商家ID
        item.setSellerId(vo.getGoods().getSellerId());
        //商家名称
        item.setSeller(sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId()).getNickName());
        //品牌名称
        item.setBrand(brandDao.selectByPrimaryKey(vo.getGoods().getBrandId()).getName());
    }

    //查询商品结果集
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page, rows);
        PageHelper.orderBy("id desc");
        //判断条件  由同学完成
        GoodsQuery query = new GoodsQuery();
        GoodsQuery.Criteria criteria = query.createCriteria();
        if (goods != null) {
            if (null != goods.getSellerId() && !"".equals(goods.getSellerId().trim())) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (null != goods.getGoodsName() && !"".equals(goods.getGoodsName().trim())) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName().trim() + "%");
            }
            if (null != goods.getAuditStatus() && !"".equals(goods.getAuditStatus())) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            criteria.andIsDeleteIsNull();
        }
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(query);
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo vo = new GoodsVo();
        vo.setGoods(goodsDao.selectByPrimaryKey(id));
        vo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        vo.setItemList(itemList);
        return vo;
    }

    @Override
    public void update(GoodsVo vo) {
        //保存商品表
        goodsDao.updateByPrimaryKeySelective(vo.getGoods());
        //保存商品扩展表
        goodsDescDao.updateByPrimaryKeySelective(vo.getGoodsDesc());
        //保存商品sku(库存)表时 先删除，在保存
        ItemQuery query = new ItemQuery();
        ItemQuery.Criteria criteria = query.createCriteria();
        criteria.andGoodsIdEqualTo(vo.getGoods().getId());
        itemDao.deleteByExample(query);
        //保存sku表 保存
        checkIsEnableSpec(vo);

    }

    /**
     * 删除
     *
     * @param ids
     */
    @Autowired
    private Destination queueSolrDeleteDestination;

    @Override
    public void delete(Long[] ids) {
        /**
         * 这里的删除并非是物理删除，而是修改tb_goods表的is_delete字段为1 ，我们可以称之为“逻辑删除”
         */
        if (ids != null) {
            Goods goods = new Goods();
            goods.setIsDelete("1");
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                //删除索引库
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage  = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
                //solrTemplate.deleteById(String.valueOf(id));
                //solrTemplate.commit();
            }
        }
    }

    /**
     * 更新状态   审核通过  或驳回
     *
     * @param ids
     * @param status
     */
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;

    @Override
    public void updateStatus(Long[] ids, String status) {
        if (ids != null) {
            Goods goods = new Goods();
            goods.setAuditStatus(status);
            for (final Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                //判断是否为审核通过
                //发布订阅模式, 发布审核通过的商品id
                if ("1".equals(status)) {
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            TextMessage message = session.createTextMessage(String.valueOf(id));
                            return message;
                        }
                    });
                    //importList(ids);
                }
            }

        }
    }

    @Autowired
    private SolrTemplate solrTemplate;

    private void checkIsEnableSpec(GoodsVo vo) {
        //判断是否启用规格
        if ("1".equals(vo.getGoods().getIsEnableSpec())) {
            //启用
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {
                //标题
                String title = vo.getGoods().getGoodsName();
                Map<String, String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();
                }
                item.setTitle(title);
                //设置库存对象的属性
                setAttribute(item, vo);
                itemDao.insertSelective(item);
            }
        } else {
            //未启用  默认一个规格  不要为Null
            //标题
            Item item = new Item();
            item.setTitle(vo.getGoods().getGoodsName());
            //设置库存对象的属性
            setAttribute(item, vo);
            //价格
            item.setPrice(new BigDecimal(0));
            item.setNum(9999);
            item.setStatus("1");
            item.setIsDefault("1");
            item.setSpec("{}");
            itemDao.insertSelective(item);
        }
    }
}
