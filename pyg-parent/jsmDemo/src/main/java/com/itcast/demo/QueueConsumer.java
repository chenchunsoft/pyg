package com.itcast.demo;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/17
 * @description:点对点方式 如果有个多个顾客 谁手快谁拿
 */
public class QueueConsumer {
    public static void main(String[] args) throws Exception {

        //1.创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.25.168:61616");
        //2.获取连接
        Connection connection = connectionFactory.createConnection();
//3.启动连接
        connection.start();
        //4.获取 session (参数 1：是否启动事务,参数 2：消息确认模式)
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //5.创建队列对象
        Queue queue = session.createQueue("test-queue");
        //6.创建消息消费   消费者对象
        MessageConsumer consumer = session.createConsumer(queue);
        //7.监听消息  匿名内部类方式
        consumer.setMessageListener(new MessageListener() {
            public void onMessage (Message message){
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("接收到消息：" + textMessage.getText());
                } catch (JMSException e) {

                    e.printStackTrace();
                }
            }
        });
        //8.等待键盘输入
        System.in.read();
        //9.关闭资源
        consumer.close();
        session.close();
        connection.close();
    }
}
