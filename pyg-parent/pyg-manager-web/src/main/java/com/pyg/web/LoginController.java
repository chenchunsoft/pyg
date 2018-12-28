package com.pyg.web;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/2
 * @description:
 */
@RestController
@RequestMapping("/login")
public class LoginController {
    /**
     * 登陆时用户姓名显示
     * @return
     */
    @RequestMapping("name")
    public Map name(){
        System.out.println("LoginController被访问了!!!");
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",name);
        return map;
    }
}
