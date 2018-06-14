package cn.itcast.core.controller;

import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TypeTemplateService typeTemplateService;
    //根据条件查询模板分页对象
    @RequestMapping(value ="/search")
    public PageResult search(@RequestBody TypeTemplate typeTemplate, Integer page, Integer rows){
        return typeTemplateService.search(typeTemplate, page, rows);
    }
}
