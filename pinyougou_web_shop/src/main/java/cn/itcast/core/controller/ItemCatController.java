package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;

    /**
     * 根据上级ID查询列表
     * @param parentId
     * @return
     */
    @RequestMapping("/findByParentId")
    public List<ItemCat> findByParentId(Long parentId) {
        return itemCatService.findByParentId(parentId);
    }

    @RequestMapping("/selectTypeTemplateList")
    public List<Map> selectTypeTemplateList(){
        return itemCatService.selectTypeTemplateList();
    }

    /**
     * 新增
     * @param itemCat
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody ItemCat itemCat){
        try {
            itemCatService.add(itemCat);
            return new Result(true,"提交成功") ;
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(true,"提交失败");
        }
    }

    @RequestMapping("/findOne")
    public ItemCat findOne(Long id){
        return itemCatService.findOne(id);
    }

    @RequestMapping("/update")
    public Result update(@RequestBody ItemCat itemCat){
        try {
            itemCatService.update(itemCat);
            return  new Result(true,"提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(true,"提交失败");
        }
    }

    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            itemCatService.delete(ids);
            return  new Result(true,"提交成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(true,"提交失败");
        }
    }

    @RequestMapping(value = "/search")
    public PageResult search(@RequestBody ItemCat itemCat, Integer page, Integer rows) {
        return itemCatService.search(itemCat, page, rows);
    }
}
