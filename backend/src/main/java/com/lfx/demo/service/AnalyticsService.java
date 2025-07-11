package com.lfx.demo.service;

import com.lfx.demo.mapper.AnalyticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsMapper analyticsMapper;

    public Map<String, Object> getConferenceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConferences", analyticsMapper.getTotalConferences());
        stats.put("totalParticipants", analyticsMapper.getTotalConferenceParticipants());
        stats.put("trend", analyticsMapper.getMonthlyConferenceParticipation());
        stats.put("typeDistribution", analyticsMapper.getConferenceTypeDistribution());
        return stats;
    }

    public Map<String, Object> getCourseEnrollmentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCourses", analyticsMapper.getTotalCourses());
        stats.put("totalEnrollments", analyticsMapper.getTotalCourseEnrollments());
        stats.put("trend", analyticsMapper.getMonthlyCourseEnrollment());
        stats.put("categoryDistribution", analyticsMapper.getCourseCategoryDistribution());
        return stats;
    }

    public Map<String, Object> getComprehensiveReport() {
        Map<String, Object> report = new HashMap<>();
        
        // 概览数据
        report.put("totalConferences", analyticsMapper.getTotalConferences());
        report.put("totalParticipants", analyticsMapper.getTotalConferenceParticipants());
        report.put("totalCourses", analyticsMapper.getTotalCourses());
        report.put("totalEnrollments", analyticsMapper.getTotalCourseEnrollments());
        
        // 会议趋势
        report.put("conferenceTrend", analyticsMapper.getMonthlyConferenceParticipation());
        // 课程趋势
        report.put("courseTrend", analyticsMapper.getMonthlyCourseEnrollment());
        
        // 会议类型分布
        report.put("conferenceTypeDistribution", analyticsMapper.getConferenceTypeDistribution());
        
        // 课程分类分布
        report.put("courseCategoryDistribution", analyticsMapper.getCourseCategoryDistribution());
        
        return report;
    }
} 