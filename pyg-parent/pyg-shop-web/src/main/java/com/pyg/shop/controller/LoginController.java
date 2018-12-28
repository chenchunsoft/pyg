package com.pyg.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/7
 * @description:商家页面登陆
 */

@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("name")
    public Map name(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",name);
        return map;
    }
}
