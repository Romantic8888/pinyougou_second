package cn.itcast.core.service;

import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.util.HttpClient;
import cn.itcast.core.util.IdWorker;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * 支付管理
 * @author lx
 *
 */
@Service
public class PayServiceImpl implements PayService{
    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private PayLogDao payLogDao;
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;

    //获取code_url  订单号  金额   为了生成二维码
    public Map<String,String> createNative(String name){
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(name);
//		调用统一下单API
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
        //发起Http请求
        HttpClient httpClient = new HttpClient(url);
        //设置 https
        httpClient.setHttps(true);

        //Map  将Map转成XML字符串
        Map<String,String> map = new HashMap<>();
//		字段名 	变量名 	必填 	类型 	示例值 	描述
//		公众账号ID 	appid 	是 	String(32) 	wxd678efh567hg6787 	微信支付分配的公众账号ID（企业号corpid即为此appId）
        map.put("appid", appid);
//		商户号 	mch_id 	是 	String(32) 	1230000109 	微信支付分配的商户号
        map.put("mch_id", partner);
//		设备号 	device_info 	否 	String(32) 	013467007045764 	自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
//		随机字符串 	nonce_str 	是 	String(32) 	5K8264ILTKCH16CQ2502SI8ZNMTM67VS 	随机字符串，长度要求在32位以内。推荐随机数生成算法
        map.put("nonce_str", WXPayUtil.generateNonceStr());
//		商品描述 	body 	是 	String(128) 	腾讯充值中心-QQ会员充值
        map.put("body", "品优购订单付款");


//		商品简单描述，该字段请按照规范传递，具体请见参数规定
//		商品详情 	detail 	否 	String(6000) 	  	商品详细描述，对于使用单品优惠的商户，改字段必须按照规范上传，详见“单品优惠参数说明”
//		附加数据 	attach 	否 	String(127) 	深圳分店 	附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。
//		商户订单号 	out_trade_no 	是 	String(32) 	20150806125346 	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|* 且在同一个商户号下唯一。详见商户订单号
        map.put("out_trade_no", payLog.getOutTradeNo());
//		标价币种 	fee_type 	否 	String(16) 	CNY 	符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
//		标价金额 	total_fee 	是 	Int 	88 	订单总金额，单位为分，详见支付金额
//		map.put("total_fee", String.valueOf(payLog.getTotalFee()));//真正程序就是这么写的
        map.put("total_fee", "1");
//		终端IP 	spbill_create_ip 	是 	String(16) 	123.12.12.123 	APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP。
        map.put("spbill_create_ip", "127.0.0.1");
//		交易起始时间 	time_start 	否 	String(14) 	20091225091010 	订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
//		交易结束时间 	time_expire 	否 	String(14) 	20091227091010

//		订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。订单失效时间是针对订单号而言的，由于在请求支付的时候有一个必传参数prepay_id只有两小时的有效期，所以在重入时间超过2小时的时候需要重新请求下单接口获取新的prepay_id。其他详见时间规则

//		建议：最短失效时间间隔大于1分钟
//		订单优惠标记 	goods_tag 	否 	String(32) 	WXG 	订单优惠标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠
//		通知地址 	notify_url 	是 	String(256) 	http://www.weixin.qq.com/wxpay/pay.php 	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
        map.put("notify_url", "http://www.itcast.cn");
//		交易类型 	trade_type 	是 	String(16) 	JSAPI
        map.put("trade_type", "NATIVE");

//		JSAPI 公众号支付

//		NATIVE 扫码支付

//		APP APP支付

//		说明详见参数规定
//		签名 	sign 	是 	String(32) 	C380BEC2BFD727A4B6845133519F3AD6 	通过签名算法计算得出的签名值，详见签名生成算法
//		签名类型 	sign_type 	否 	String(32) 	MD5 	签名类型，默认为MD5，支持HMAC-SHA256和MD5。
        Map<String, String> resultMap = null;
        try {
            //入参
            String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);
            httpClient.setXmlParam(xmlParam);
            //发出POST
            httpClient.post();
            //响应并返回结果
            String result = httpClient.getContent();
            resultMap = WXPayUtil.xmlToMap(result);
            //code_url
            //订单号
            resultMap.put("out_trade_no", payLog.getOutTradeNo());
            //金额
            resultMap.put("total_fee", String.valueOf(payLog.getTotalFee()));

            System.out.println(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultMap;
    }
    //调用微信查询接口
    public Map<String,String> queryPayStatus(String out_trade_no){

        String url = "https://api.mch.weixin.qq.com/pay/orderquery";
        //发起Http请求
        HttpClient httpClient = new HttpClient(url);
        //设置 https
        httpClient.setHttps(true);
        //Map  将Map转成XML字符串
        Map<String,String> map = new HashMap<>();
//		字段名 	变量名 	必填 	类型 	示例值 	描述
//		公众账号ID 	appid 	是 	String(32) 	wxd678efh567hg6787 	微信支付分配的公众账号ID（企业号corpid即为此appId）
        map.put("appid", appid);
//		商户号 	mch_id 	是 	String(32) 	1230000109 	微信支付分配的商户号
        map.put("mch_id", partner);
//		微信订单号 	transaction_id 	二选一 	String(32) 	1009660380201506130728806387 	微信的订单号，建议优先使用
//		商户订单号 	out_trade_no 	String(32) 	20150806125346 	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        map.put("out_trade_no", out_trade_no);
//		随机字符串 	nonce_str 	是 	String(32) 	C380BEC2BFD727A4B6845133519F3AD6 	随机字符串，不长于32位。推荐随机数生成算法
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        //返回结果Map
        Map<String, String> resultMap = null;
        try {
            //入参
//		签名 	sign 	是 	String(32) 	5K8264ILTKCH16CQ2502SI8ZNMTM67VS 	通过签名算法计算得出的签名值，详见签名生成算法
//		签名类型 	sign_type 	否 	String(32) 	HMAC-SHA256 	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
            String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);
            httpClient.setXmlParam(xmlParam);
            //发出POST
            httpClient.post();
            //响应并返回结果
            String result = httpClient.getContent();
            resultMap = WXPayUtil.xmlToMap(result);

            //判断是否支付成功
            if("SUCCESS".equals(resultMap.get("trade_state"))){
                PayLog payLog = new PayLog();
                payLog.setOutTradeNo(out_trade_no);
                payLog.setTradeState("1");
                payLog.setPayTime(new Date());
                payLog.setTransactionId(resultMap.get("transaction_id"));
                payLogDao.updateByPrimaryKeySelective(payLog);
                System.out.println("---------------支付日志已经更新完成------------------------");
            }

            System.out.println("---------------下面是查询结果xml------------------------");
            System.out.println(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultMap;


    }

    //调用关闭订单接口
    public Map<String,String> closeOrder(String out_trade_no){
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";
        //发起Http请求
        HttpClient httpClient = new HttpClient(url);
        //设置 https
        httpClient.setHttps(true);
        //Map  将Map转成XML字符串
        Map<String,String> map = new HashMap<>();
//		字段名 	变量名 	必填 	类型 	示例值 	描述
//		公众账号ID 	appid 	是 	String(32) 	wxd678efh567hg6787 	微信支付分配的公众账号ID（企业号corpid即为此appId）
        map.put("appid", appid);
//		商户号 	mch_id 	是 	String(32) 	1230000109 	微信支付分配的商户号
        map.put("mch_id", partner);
//		商户订单号 	out_trade_no 	String(32) 	20150806125346 	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。 详见商户订单号
        map.put("out_trade_no", out_trade_no);
//		随机字符串 	nonce_str 	是 	String(32) 	C380BEC2BFD727A4B6845133519F3AD6 	随机字符串，不长于32位。推荐随机数生成算法
        map.put("nonce_str", WXPayUtil.generateNonceStr());
        //返回结果Map
        Map<String, String> resultMap = null;
        try {
            //入参
//		签名 	sign 	是 	String(32) 	5K8264ILTKCH16CQ2502SI8ZNMTM67VS 	通过签名算法计算得出的签名值，详见签名生成算法
//		签名类型 	sign_type 	否 	String(32) 	HMAC-SHA256 	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
            String xmlParam = WXPayUtil.generateSignedXml(map, partnerkey);
            httpClient.setXmlParam(xmlParam);
            //发出POST
            httpClient.post();
            //响应并返回结果
            String result = httpClient.getContent();
            resultMap = WXPayUtil.xmlToMap(result);
            System.out.println("---------------关闭订单返回结果------------------------");
            System.out.println(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resultMap;
    }
}
