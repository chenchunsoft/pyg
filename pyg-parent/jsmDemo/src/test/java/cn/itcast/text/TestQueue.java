package cn.itcast.text;

import cn.itcast.demo.QueueProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/17
 * @description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:applicationContext-jms-producer.xml")
public class TestQueue {
    @Autowired
    private QueueProducer queueProducer;

    @Test
    public void testSend(){
        queueProducer.sendTextMessage("spring JMS 点对点");
    }
}
