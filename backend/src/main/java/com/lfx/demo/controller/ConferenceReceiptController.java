package com.lfx.demo.controller;

import com.lfx.demo.entity.ConferenceReceipt;
import com.lfx.demo.service.ConferenceReceiptService;
import com.lfx.demo.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 会议回执控制器
 */
@Slf4j
@RestController
@RequestMapping("/app/confer")
public class ConferenceReceiptController {
    
    @Autowired
    private ConferenceReceiptService conferenceReceiptService;
    
    /**
     * 提交会议回执
     * @param receiptData 回执数据
     * @param request HTTP请求
     * @return 提交结果
     */
    @PostMapping("/receipt")
    public AjaxResult submitReceipt(@RequestBody Map<String, Object> receiptData, HttpServletRequest request) {
        try {
            log.info("收到会议回执提交请求：{}", receiptData);
            
            // 从请求头获取用户信息
            String userIdStr = request.getHeader("X-User-Id");
            Long userId = null;
            if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                try {
                    userId = Long.parseLong(userIdStr);
                } catch (NumberFormatException e) {
                    log.warn("用户ID格式错误：{}", userIdStr);
                }
            }
            
            // 构建回执对象
            ConferenceReceipt receipt = new ConferenceReceipt();
            receipt.setConferenceName((String) receiptData.get("conferenceName"));
            receipt.setUserId(userId);
            receipt.setCompany((String) receiptData.get("company"));
            receipt.setName((String) receiptData.get("name"));
            receipt.setGender((String) receiptData.get("gender"));
            receipt.setPhone((String) receiptData.get("phone"));
            receipt.setEmail((String) receiptData.get("email"));
            receipt.setArrivalMethod((String) receiptData.get("arrivalMethod"));
            receipt.setArrivalFlightOrTrain((String) receiptData.get("arrivalFlightOrTrain"));
            
            // 处理到达时间
            String arrivalTimeStr = (String) receiptData.get("arrivalTime");
            if (arrivalTimeStr != null && !arrivalTimeStr.trim().isEmpty()) {
                receipt.setArrivalTime(conferenceReceiptService.parseTime(arrivalTimeStr));
            }
            
            // 验证必填字段
            if (receipt.getConferenceName() == null || receipt.getConferenceName().trim().isEmpty()) {
                return AjaxResult.error("会议名称不能为空");
            }
            if (receipt.getCompany() == null || receipt.getCompany().trim().isEmpty()) {
                return AjaxResult.error("单位名称不能为空");
            }
            if (receipt.getName() == null || receipt.getName().trim().isEmpty()) {
                return AjaxResult.error("姓名不能为空");
            }
            if (receipt.getPhone() == null || receipt.getPhone().trim().isEmpty()) {
                return AjaxResult.error("手机号码不能为空");
            }
            
            // 验证手机号格式
            if (!receipt.getPhone().matches("^1[3-9]\\d{9}$")) {
                return AjaxResult.error("手机号码格式不正确");
            }
            
            // 检查是否已经提交过回执
            if (userId != null && conferenceReceiptService.hasSubmittedReceipt(userId, receipt.getConferenceName())) {
                return AjaxResult.error("您已经为该会议提交过回执，请勿重复提交");
            }
            
            // 提交回执
            boolean success = conferenceReceiptService.submitReceipt(receipt);
            
            if (success) {
                return AjaxResult.success("回执提交成功");
            } else {
                return AjaxResult.error("回执提交失败，请稍后重试");
            }
            
        } catch (Exception e) {
            log.error("提交会议回执异常", e);
            return AjaxResult.error("系统异常，请稍后重试");
        }
    }
    
    /**
     * 获取会议回执列表
     * @param conferenceName 会议名称
     * @return 回执列表
     */
    @GetMapping("/receipts")
    public AjaxResult getReceipts(@RequestParam String conferenceName) {
        try {
            List<ConferenceReceipt> receipts = conferenceReceiptService.getReceiptsByConferenceName(conferenceName);
            return AjaxResult.success("获取成功", receipts);
        } catch (Exception e) {
            log.error("获取会议回执列表异常", e);
            return AjaxResult.error("获取失败");
        }
    }
    
    /**
     * 统计会议报名人数
     * @param conferenceName 会议名称
     * @return 报名人数
     */
    @GetMapping("/count")
    public AjaxResult countReceipts(@RequestParam String conferenceName) {
        try {
            int count = conferenceReceiptService.countReceiptsByConferenceName(conferenceName);
            return AjaxResult.success("获取成功", count);
        } catch (Exception e) {
            log.error("统计会议报名人数异常", e);
            return AjaxResult.error("获取失败");
        }
    }
    
    /**
     * 获取所有会议的回执统计信息
     * @return 统计信息
     */
    @GetMapping("/stats")
    public AjaxResult getStats() {
        try {
            List<Object> stats = conferenceReceiptService.getConferenceReceiptStats();
            return AjaxResult.success("获取成功", stats);
        } catch (Exception e) {
            log.error("获取会议回执统计信息异常", e);
            return AjaxResult.error("获取失败");
        }
    }
} 