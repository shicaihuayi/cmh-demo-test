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
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * @Description 短信发送服务
 */
@Component
@RequiredArgsConstructor
public class SmsUtil {
    private static final String REGION_ID = "cn-beijing";
    private static final String ACCESS_KEY_ID = "LTAI5tJZ6GKyipvADPRNCPA7";
    private static final String ACCESS_KEY_SECRET = "NLpHJ1UuGH4xJ7oKiapEe9k8hWDk2p";
    
    private final IAcsClient client;

    public String toSendMes(String phoneNumber, String code) {
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
        HashMap<String,String> param = new HashMap<>();
        param.put("code", code);
        request.setTemplateParam(JSONObject.toJSONString(param));
        
        try {
            //3.发送请求
            SendSmsResponse response = client.getAcsResponse(request);
            return response.getMessage();
        } catch (ServerException e) {
            e.printStackTrace();
            return null;
        } catch (ClientException e) {
            System.out.println("ErrCode:" + e.getErrCode());
            System.out.println("ErrMsg:" + e.getErrMsg());
            System.out.println("RequestId:" + e.getRequestId());
            return null;
        }
    }
}
