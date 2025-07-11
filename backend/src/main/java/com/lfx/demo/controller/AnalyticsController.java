package com.lfx.demo.controller;

import com.lfx.demo.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/comprehensive-report")
    public Map<String, Object> getComprehensiveReport() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> report = analyticsService.getComprehensiveReport();
            response.put("code", 200);
            response.put("msg", "数据获取成功");
            response.put("data", report);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "数据获取失败: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    @GetMapping("/conference-stats")
    public Map<String, Object> getConferenceStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = analyticsService.getConferenceStats();
            response.put("code", 200);
            response.put("msg", "会议统计数据获取成功");
            response.put("data", stats);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "会议统计数据获取失败: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    @GetMapping("/course-enrollment-stats")
    public Map<String, Object> getCourseEnrollmentStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> stats = analyticsService.getCourseEnrollmentStats();
            response.put("code", 200);
            response.put("msg", "课程报名统计数据获取成功");
            response.put("data", stats);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("msg", "课程报名统计数据获取失败: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
} 