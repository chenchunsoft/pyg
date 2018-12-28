package cn.itcast.demo;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/21
 * @description:
 */
@RestController
public class UserController {
    @RequestMapping("/findLoginUser")
    public void findLoginUser(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(name);
    }

}
