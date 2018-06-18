package cn.itcast.core.service.impl;

import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.pojogroup.GoodsVo;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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

    }
}
