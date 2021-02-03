package com.itheima.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.itheima.domain.User;

/**
 * @author caijia
 * @Date 2020年12月22日 17:53:00
 */
@Controller
public class TestController{

    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("message","hello thymlelaf!!!");
        List<User> list = new ArrayList<>();
        list.add(new User(1,"张三","成都"));
        list.add(new User(2,"李四","上海"));
        list.add(new User(3,"王五","南京"));

        model.addAttribute("list", list);
        return "demo1";
    }
}
