package com.pyg.content.service.impl;
import java.util.List;

import com.pyg.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pyg.mapper.TbContentMapper;
import com.pyg.pojo.TbContent;
import com.pyg.pojo.TbContentExample;
import com.pyg.pojo.TbContentExample.Criteria;


import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 * @author Administrator
 *只要进行 增删改的操作,都需要更新redis缓存信息,保证首页展示的图片与数据库一致.
 */
@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbContent> findAll() {
		return contentMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbContent> page=   (Page<TbContent>) contentMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(TbContent content) {
		contentMapper.insert(content);
		//清除缓存  删除指定组的缓存信息
		redisTemplate.boundHashOps("content").delete(content.getCategoryId());
	}


	/**
	 * 修改
	 * 可能修改前和修改后的分类ID不一样
	 */
	@Override
	public void update(TbContent content){
		//这个时候传过来的contentID 只是分类改了,其他的都没有改变,可以根据它的ID获得原来的分组信息.

		//根据原来的广告content的getID()查询的结果 .getcategoryId()获原始的的分类ID
		Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
		//清除原来分类ID的缓存
		redisTemplate.boundHashOps("content").delete(categoryId);
		//更新数据库中的信息
		contentMapper.updateByPrimaryKey(content);
		//清除现在分类ID的缓存.
		if(categoryId.longValue()!=content.getCategoryId().longValue()){
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
	}

	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbContent findOne(Long id){
		return contentMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//清除缓存
			Long categoryId = contentMapper.selectByPrimaryKey(id).getCategoryId();//广告分类ID
			redisTemplate.boundHashOps("content").delete(categoryId);
			contentMapper.deleteByPrimaryKey(id);
	}
	}

	
	
		@Override
	public PageResult findPage(TbContent content, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbContentExample example=new TbContentExample();
		Criteria criteria = example.createCriteria();
		
		if(content!=null){			
						if(content.getTitle()!=null && content.getTitle().length()>0){
				criteria.andTitleLike("%"+content.getTitle()+"%");
			}
			if(content.getUrl()!=null && content.getUrl().length()>0){
				criteria.andUrlLike("%"+content.getUrl()+"%");
			}
			if(content.getPic()!=null && content.getPic().length()>0){
				criteria.andPicLike("%"+content.getPic()+"%");
			}
			if(content.getStatus()!=null && content.getStatus().length()>0){
				criteria.andStatusLike("%"+content.getStatus()+"%");
			}
	
		}
		
		Page<TbContent> page= (Page<TbContent>)contentMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
//	注入redis
	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public List<TbContent> findByCategoryId(Long categoryId) {

		List<TbContent> contentList= (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
		if(contentList==null){
			System.out.println("从数据库读取数据放入缓存");

			TbContentExample contentExample=new TbContentExample();
			Criteria criteria2 = contentExample.createCriteria();
			criteria2.andCategoryIdEqualTo(categoryId);
			criteria2.andStatusEqualTo("1");//开启状态
			contentExample.setOrderByClause("sort_order");//排序
			contentList = contentMapper.selectByExample(contentExample);//获取广告列表
			redisTemplate.boundHashOps("content").put(categoryId, contentList);//存入缓存

		}else{
			System.out.println("从缓存读取数据");
		}

		return contentList;
	}

}
