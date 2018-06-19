package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojogroup.GoodsVo;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo vo){
        //获取登录名
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        vo.getGoods().setSellerId(sellerId);//设置商家ID
        try {
            goodsService.add(vo);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows,@RequestBody Goods goods){
        return goodsService.search(page,rows,goods);
    }
}
