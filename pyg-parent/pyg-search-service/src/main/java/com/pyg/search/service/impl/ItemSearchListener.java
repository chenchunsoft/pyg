package com.pyg.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pyg.pojo.TbItem;
import com.pyg.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/19
 * @description:
 */
@Component
public class ItemSearchListener implements MessageListener{
    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message)  {
        System.out.println("监听接收到消息...");
        try {
            TextMessage textMessage=(TextMessage)message;
            String text = textMessage.getText();
            List<TbItem> list = JSON.parseArray(text,TbItem.class);
            for(TbItem item:list){
                System.out.println(item.getId()+" "+item.getTitle());
                Map specMap= JSON.parseObject(item.getSpec());

                item.setSpecMap(specMap);//给带注解的字段赋值
            }
            itemSearchService.importList(list);//导入
            System.out.println("成功导入到索引库");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


