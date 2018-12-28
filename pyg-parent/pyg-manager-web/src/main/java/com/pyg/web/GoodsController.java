package com.pyg.web;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;

import com.pyg.pojo.TbItem;
import com.pyg.pojogroup.Goods;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbGoods;
import com.pyg.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	/*@Reference(timeout = 100000)
	private ItemSearchService itemSearchService;*/


//	@Reference(timeout = 40000)
//	private ItemPageService itemPageService;
//
	/**
	 * 用于导入solr索引库的消息
	 */
	@Autowired
	private Destination queueSolrDestination;//用于发送 solr 导入的消息
	@Autowired
	private JmsTemplate jmsTemplate;
	/*
	* 用来生成商品详情页的消息
	* 与spring-jms.xml 中的<bean id="topicPageDestination" 相对应.
	* */
	@Autowired
	private Destination topicPageDestination;
	/**
	 * //用于删除静态网页的消息
	 *
	 */
	@Autowired
	private Destination topicPageDeleteDestination;

	/**
	 * 生成静态页测试
	 * @param goodsId
	 */
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){


//		itemPageService.genItemHtml(goodsId);
	}



	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	
//	/**
//	 * 增加
//	 * @param goods
//	 * @return
//	 */
//	@RequestMapping("/add")
//	public Result add(@RequestBody TbGoods goods){
//		try {
//			goodsService.add(goods);
//			return new Result(true, "增加成功");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new Result(false, "增加失败");
//		}
//	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}

	/**
	 * bean id="queueSolrDeleteDestination"
	 * 这个ID和spring-jms.xml的配置名称必须保持一致.
	 */
	@Autowired
	private Destination queueSolrDeleteDestination;//用户在索引库中删除记录
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final  Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
		//	itemSearchService.deleteByGoodsIds(Arrays.asList(ids));

			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			///删除页面

			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});


			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	/**
	 * 更新状态
	 * @param ids
	 * @param status
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			//按照SPU ID查询SKU列表(状态为1) 审核通过
			if (status.equals("1")){
				//导入到索引库

				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//调用搜索接口实现批量数据导入
				if (itemList.size()>0){
					//导入到solr
				//	itemSearchService.importList(itemList);
					final String jsonString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						@Override
						public Message createMessage(Session session) throws JMSException
						{
							return session.createTextMessage(jsonString);
						}
					});
					System.out.println("来自GoodsController的生产者发送了消息");
				}else {
					System.out.println("没有明确的数据!");
				}
				//生成商品详情页
				for (Long id : ids) {
					//itemPageService.genItemHtml(id);
					//这里使用循环的方式因为genItemHtml()方法接受的参数是id而不是ids 不用修改
					for(final Long goodsId:ids){
						jmsTemplate.send(topicPageDestination, new MessageCreator() {
							@Override
							public Message createMessage(Session session) throws
									JMSException {
								return session.createTextMessage(goodsId+"");
							}
						});

						}
				}

			}
			return new Result(true, "成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "失败");
		}
	}

}
