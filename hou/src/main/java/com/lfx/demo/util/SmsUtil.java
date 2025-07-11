package com.lfx.demo.util;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @Description 短信发送服务
 */
@Component
public class SmsUtil {
    public String toSendMes(String phoneNumber, String code) {
        //1.连接阿里云
        /**
         "<your-region-id>",           // The region ID 地区标识
         "<your-access-key-id>",       // The AccessKey ID of the RAM account RAM账户的AccessKey ID 阿里云账号可查
         "<your-access-key-secret>",   // The AccessKey Secret of the RAM account RAM 账户的 AccessKey Secret 阿里云账号可查
         **/
        DefaultProfile profile = DefaultProfile.getProfile("cn-beijing", "LTAI5tJZ6GKyipvADPRNCPA7", "NLpHJ1UuGH4xJ7oKiapEe9k8hWDk2p");

        IAcsClient client = new DefaultAcsClient(profile);
        //2.构建请求 自定义参数
        SendSmsRequest request = new SendSmsRequest();
        //接收短信的手机号码
        request.setPhoneNumbers(phoneNumber);
        //短信签名名称
        request.setSignName("测盟汇");
        //短信模板CODE
        request.setTemplateCode("SMS_468625012");
        //模版内容:您正在使用阿里云短信测试服务，体验验证码是：${code}，如非本人操作，请忽略本短信！
        //短信模板变量对应的实际值
        //("{\"code\":\"1234\"}");
        HashMap<String,String> param = new HashMap<>();
        param.put("code", code);
        request.setTemplateParam(JSONObject.toJSONString(param));
        SendSmsResponse response = new SendSmsResponse();
        try {
            //3.发送请求
            response = client.getAcsResponse(request);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
        }

        System.out.println(response.getMessage());
        return response.getMessage();
    }
}
