package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 品牌管理
 */
@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查询所有
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<Brand> findAll() {
        return brandService.findAll();
    }

    /**
     * 分页
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return brandService.findPage(page, rows);
    }

    /**
     * 新建品牌
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Brand brand) {

        try {
            brandService.add(brand);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    @RequestMapping("/update")
    public Result update(@RequestBody Brand brand) {
        try {
            brandService.update(brand);
            return new Result(true,"更新成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"更新失败");
        }
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Brand findOne(Long id){
        Brand brand = brandService.findOne(id);
        return brand;
    }

    /**
     *批量删除品牌
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return  new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"删除失败");
        }
    }

    /**
     * 条件查询带分页
     * @param page
     * @param rows
     * @param brand
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestParam("pageNum") Integer page, @RequestParam("pageSize") Integer rows, @RequestBody(required = false) Brand brand){
       return brandService.search(page,rows,brand);
    }
}
