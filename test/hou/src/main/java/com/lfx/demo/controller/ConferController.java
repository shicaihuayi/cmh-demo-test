package com.lfx.demo.controller;

import com.lfx.demo.entity.Conference;
import com.lfx.demo.mapper.ConferMapper;
import com.lfx.demo.service.ConferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/confer")
public class ConferController {
    @Autowired
    private ConferService biz;
    private ConferMapper mapper;
    @RequestMapping("/list")
    public Map findAll(){

        List<Conference> confers = biz.getConferList();
        Map map = new HashMap();
        map.put("isOk",true);
        map.put("confers", confers);
        map.put("msg","会议列表数据加载成功！");
        return map;
    }

    @RequestMapping("/search")
    public Map search(@RequestBody Map<String, String> searchParams) {
        String conferName = searchParams.get("conferName");
        String creater = searchParams.get("creater");
        String stime = searchParams.get("stime");
        String content = searchParams.get("content");
        List<Conference> list = biz.getConfer(conferName, creater, stime, content);
        Map<String, Object> map = new HashMap<>();
        boolean isOk = false;
        if(list!=null){
            isOk =true;
        }
        if(isOk){
            map.put("isOk",true);
            map.put("confers", list);
            map.put("msg","查询成功！");
        }else{
            map.put("isOk",false);
            map.put("msg","查询失败，未找到该菜品！");
        }
        return map;
    }
    @PostMapping("/del")
    public Map del(@RequestBody List<String> conferName){
        System.out.println("del!!!!");
        System.out.println(conferName);
        boolean isOk = biz.removeConfer(conferName);
        Map map = new HashMap();
        if(isOk){
            System.out.println("delyes!!!!");
            map.put("isOk",true);
            map.put("msg","删除成功");
        }else{
            System.out.println("delno!!!!");
            map.put("isOk",false);
            map.put("msg","删除失败");
        }
        return map;
    }
    @PostMapping("/add")
    public Map add(@RequestBody Conference confer){
        boolean isOk = biz.addConfer(confer);
        Map map = new HashMap();
        if(isOk){
            map.put("isOk",true);
            map.put("msg","添加成功");
        }else{
            map.put("isOk",false);
            map.put("msg","添加失败,请检查是否登录");
        }
        return map;
    }
    @PostMapping("/update")
    public Map update(@RequestBody Conference confer){
        if(confer==null) System.out.println("confer为空!");
        boolean isOk = biz.modifyConfer(confer);
        Map map = new HashMap();
        if(isOk){
            map.put("isOk",true);
            map.put("msg","修改成功");
        }else{
            map.put("isOk",false);
            map.put("msg","修改失败");
        }
        return map;
    }
        public void setBiz(ConferService biz) {
        this.biz = biz;
    }
}