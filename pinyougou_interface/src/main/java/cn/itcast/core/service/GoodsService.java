package cn.itcast.core.service;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojogroup.GoodsVo;
import entity.PageResult;

public interface GoodsService {
    /**
     * 增加
     */
    void add(GoodsVo goods);
    PageResult search(Integer page, Integer rows, Goods goods);
}
