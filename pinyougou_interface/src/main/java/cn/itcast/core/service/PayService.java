package cn.itcast.core.service;

import java.util.Map;

public interface PayService {

    //获取code_url  订单号  金额   为了生成二维码
     Map<String,String> createNative(String name);

    //调用微信查询接口
     Map<String,String> queryPayStatus(String out_trade_no);

    //调用关闭订单接口
     Map<String,String> closeOrder(String out_trade_no);

}
