package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 支付管理
 * @author lx
 *
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private PayService payService;
    //获取code_url  订单号  金额   为了生成二维码
    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        return payService.createNative(name);
    }
    //调用查询
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        try {
            int x = 0;
            //无限循环开始
            while(true){
                //调用查询接口  当去查询时  未付款
                Map<String, String> map = payService.queryPayStatus(out_trade_no);
                if("NOTPAY".equals(map.get("trade_state"))){
                    // 休息 3秒
                    Thread.sleep(3000);
                    x++;
                }else{
                    return new Result(true,"支付成功");
                }
                // 5分钟
                if(x>100){
                    //关闭订单
                    Map<String, String> closeMap = payService.closeOrder(out_trade_no);
                    if("ORDERPAID".equals(closeMap.get("err_code"))){
                        return new Result(true,"支付成功");
                    }
                    if("SUCCESS".equals(closeMap.get("result_code"))){
                        return new Result(false,"二维码超时");
                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            return new Result(false,"查询失败");
        }
    }

}
