package com.lfx.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/app/home")
public class AppHomeController {

    @GetMapping("/about")
    public Map<String, Object> getAbout() {
        Map<String, Object> about = new HashMap<>();
        about.put("title", "关于测盟汇");
        about.put("content", "测盟汇是一个专业的软件测试交流平台，致力于推动软件测试行业的发展。我们为测试从业者提供最新的行业动态、专业的技术培训、高质量的会议研讨，以及丰富的学习资源。通过测盟汇平台，您可以获取前沿的测试技术信息，参与行业内的深度交流，提升自己的专业技能。");
        return about;
    }

    @GetMapping("/members")
    public List<Map<String, Object>> getMembers() {
        // 返回空列表，后续可以从数据库获取
        return new ArrayList<>();
    }

    @GetMapping("/banners")
    public List<Map<String, Object>> getBanners() {
        List<Map<String, Object>> banners = new ArrayList<>();
        
        // 添加默认的banner数据
        Map<String, Object> banner1 = new HashMap<>();
        banner1.put("id", 1);
        banner1.put("image", "/static/images/banner1.jpg");
        banner1.put("title", "软件测试技术交流");
        banners.add(banner1);

        Map<String, Object> banner2 = new HashMap<>();
        banner2.put("id", 2);
        banner2.put("image", "/static/images/banner2.jpg");
        banner2.put("title", "测试专业技能提升");
        banners.add(banner2);

        Map<String, Object> banner3 = new HashMap<>();
        banner3.put("id", 3);
        banner3.put("image", "/static/images/banner3.jpg");
        banner3.put("title", "行业动态分享");
        banners.add(banner3);

        return banners;
    }
} 