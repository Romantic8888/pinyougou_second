package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;

    //根据条件查询模板分页对象
    @RequestMapping(value = "/search")
    public PageResult search(@RequestBody TypeTemplate typeTemplate, Integer page, Integer rows) {
        return typeTemplateService.search(typeTemplate, page, rows);
    }

    /**
     * 新建
     *
     * @param typeTemplate
     * @return
     */
    @RequestMapping(value = "/add")
    public Result add(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true, "提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "提交失败");
        }

    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/findOne")
    public TypeTemplate findOne(Long id) {
        return typeTemplateService.findOne(id);
    }

    /**
     * 更新
     *
     * @param typeTemplate
     * @return
     */
    @RequestMapping(value = "/update")
    public Result update(@RequestBody TypeTemplate typeTemplate) {
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true, "提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "提交失败");
        }

    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @RequestMapping(value = "/delete")
    public Result delete(Long[] ids) {
        try {
            typeTemplateService.delete(ids);
            return new Result(true, "提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "提交失败");
        }
    }
    @RequestMapping(value = "/findBySpecList")
    public List<Map> findBySpecList(Long id){
         return  typeTemplateService.findBySpecList(id);
    }

}
