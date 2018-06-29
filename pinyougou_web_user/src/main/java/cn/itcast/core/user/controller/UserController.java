package cn.itcast.core.user.controller;

import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import cn.itcast.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
        try {
            //判断手机号 合法
            if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
                return new Result(false,"手机号不合法");
            }
            //发短信
            userService.sendCode(phone);
            return new Result(true,"获取成功");
        } catch (Exception e) {
            // TODO: handle exception
            return new Result(false,"获取失败");
        }
    }

    @RequestMapping("/add")
    public Result add(@RequestBody User user, String smscode){
        try {
            userService.add(user,smscode);
            return new Result(true,"注册成功");
        }catch (RuntimeException e) {
            return new Result(false, e.getMessage());
        }catch (Exception e) {
            return new Result(false,"注册失败");
        }

    }
}
