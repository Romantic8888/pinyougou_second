package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojogroup.SpecificationVo;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;

    /**
     * 初始化列表及带条件的分页查询
     *
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody(required = false) Specification specification) {
        return specificationService.search(page, rows, specification);
    }

    /**
     * 新增规格
     * @param vo
     * @return
     */
    @RequestMapping("add")
    public Result add(@RequestBody SpecificationVo vo) {
        try {
            specificationService.add(vo);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    //通过规格ID 查询规格表 及规格属性表结果集
    @RequestMapping(value = "/findOne.do")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }

    //修改
    @RequestMapping(value = "/update")
    public Result update(@RequestBody SpecificationVo vo){
        try {

            specificationService.update(vo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.getStackTrace();
            return new Result(false,"修改失败");
        }
    }

    @RequestMapping(value="/delete")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.getStackTrace();
            return new Result(false,"删除失败");
        }
    }

    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return specificationService.selectOptionList();
    }


}