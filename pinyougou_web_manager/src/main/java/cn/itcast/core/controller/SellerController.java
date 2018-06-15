package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    // 添加
    @RequestMapping("/add")
    public Result add(@RequestBody Seller seller) {
        try {
            sellerService.add(seller);
            return new Result(true, "提交成功");
        } catch (Exception e) {
            // TODO: handle exception
            return new Result(false, "提交失败");
        }
    }
    // 查询所有分页 条件
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller) {
        return sellerService.search(page, rows, seller);
    }

    // 查询一个商家
    @RequestMapping("/findOne")
    public Seller findOne(String id) {
        return sellerService.findOne(id);
    }

    // 审核开始
    @RequestMapping("/updateStatus")
    public Result updateStatus(String sellerId, String status) {
        try {
            sellerService.updateStatus(sellerId, status);
            return new Result(true, "审核成功");
        } catch (Exception e) {
            // TODO: handle exception
            return new Result(false, "审核失败");
        }
    }
}
