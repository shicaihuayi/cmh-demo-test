package com.lfx.demo.controller;

import com.lfx.demo.service.ConferService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app/conference")
public class AppConferController {
    private static final Logger logger = LoggerFactory.getLogger(AppConferController.class);
    
    @Autowired
    private ConferService conferService;

    /**
     * 获取已审核通过的会议列表（小程序版本）
     */
    @RequestMapping("/list")
    public Map<String, Object> getApprovedConferences(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) String category) {
        logger.info("(/app/conference/list) [小程序] 获取会议列表 -> 页码: {}, 每页数量: {}, 类型: {}", page, size, category);
        
        Map<String, Object> map = new HashMap<>();
        try {
            // 调用ConferService的方法获取已审核通过的会议
            List<Map<String, Object>> conferences = conferService.getApprovedConferencesForApp(page, size, category);
            int total = conferService.getApprovedConferencesCount(category);
            
            map.put("isOk", true);
            map.put("msg", "获取成功");
            map.put("conferences", conferences);
            map.put("total", total);
            map.put("currentPage", page);
            map.put("pageSize", size);
            
        } catch (Exception e) {
            logger.error("获取会议列表失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 根据会议名称获取会议详情（小程序版本）
     */
    @RequestMapping("/detail")
    public Map<String, Object> getConferenceDetail(@RequestParam String conferName) {
        logger.info("(/app/conference/detail) [小程序] 获取会议详情 -> 会议名称: {}", conferName);
        
        Map<String, Object> map = new HashMap<>();
        if (conferName == null || conferName.trim().isEmpty()) {
            map.put("isOk", false);
            map.put("msg", "参数错误：会议名称不能为空");
            return map;
        }
        
        try {
            Map<String, Object> conference = conferService.getApprovedConferenceDetailForApp(conferName);
            if (conference != null) {
                map.put("isOk", true);
                map.put("msg", "获取成功");
                map.put("conference", conference);
            } else {
                map.put("isOk", false);
                map.put("msg", "会议不存在或未审核通过");
            }
        } catch (Exception e) {
            logger.error("获取会议详情失败", e);
            map.put("isOk", false);
            map.put("msg", "获取失败");
        }
        return map;
    }

    /**
     * 搜索会议（小程序版本）
     */
    @RequestMapping("/search")
    public Map<String, Object> searchConferences(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        logger.info("(/app/conference/search) [小程序] 搜索会议 -> 关键词: {}", keyword);
        
        Map<String, Object> map = new HashMap<>();
        try {
            List<Map<String, Object>> conferences = conferService.searchApprovedConferencesForApp(keyword, page, size);
            int total = conferService.getSearchApprovedConferencesCount(keyword);
            
            map.put("isOk", true);
            map.put("msg", "搜索成功");
            map.put("conferences", conferences);
            map.put("total", total);
            map.put("keyword", keyword);
            
        } catch (Exception e) {
            logger.error("搜索会议失败", e);
            map.put("isOk", false);
            map.put("msg", "搜索失败");
        }
        return map;
    }
} 
