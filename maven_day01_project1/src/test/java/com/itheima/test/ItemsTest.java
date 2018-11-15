package com.itheima.test;

import com.itheima.dao.ItemsDao;
import com.itheima.domain.Items;
import com.itheima.service.ItemsService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author:ccsoftlucifer
 * @date:2018/11/14
 * @descrption:
 */
public class ItemsTest {
    @Test
    public void findById(){
        System.out.println("DAO测试开始");
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        //从容器中拿到所需要的dao的代理对象
        ItemsDao itemsDao = ac.getBean(ItemsDao.class);
        Items items = itemsDao.findById(1);
        System.out.println(items.getName());
    }
    @Test
    public void findById1(){
        ApplicationContext ac = new ClassPathXmlApplicationContext("applicationContext.xml");
        ItemsService bean = ac.getBean(ItemsService.class);
        Items items = bean.findById(1);
        System.out.println(items.getName());
    }
}
