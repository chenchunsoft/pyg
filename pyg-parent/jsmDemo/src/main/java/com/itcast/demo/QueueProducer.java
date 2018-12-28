package com.itcast.demo;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/17
 * @description:点对点方式 如果有个多个顾客 谁手快谁拿
 */
public class QueueProducer {
    public static void main(String[] args) throws Exception{
        //1.创建连接工厂  连接对象需要连接工厂来创建 //http://192.168.25.168:8161
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.168:61616");
        //2.创建连接
        Connection connection = connectionFactory.createConnection();
        //3.启动连接
        connection.start();
        //4.获取session绘画对象  参数1:是否启动事物 参数2:消息确认方式
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建一个队列对象
        Queue queue = session.createQueue("test-queue");
        Destination a;
        //6.创建消息生产者对象
        MessageProducer producer = session.createProducer(queue);
        //7.创建消息对象 (文本)
        TextMessage textMessage = session.createTextMessage("欢迎来到神奇的PYG世界");
        //8.发送消息
        producer.send(textMessage);

        //9.关闭资源
        producer.close();
        session.close();
        connection.close();
    }
}
