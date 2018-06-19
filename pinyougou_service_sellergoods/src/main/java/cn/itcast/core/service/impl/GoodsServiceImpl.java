package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojogroup.GoodsVo;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;

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
        if ("1".equals(vo.getGoods().getIsEnableSpec())){
            //启用
        List<Item> itemList=vo.getItemList();
            for (Item item:itemList) {
                //标题
                String  title=vo.getGoods().getGoodsName();
                Map<String ,String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String ,String> entry:entries) {
                    title+=" "+entry.getValue();
                }
                item.setTitle(title);

            }
        }else{


        }
    }
}
