package com.pyg.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pyg.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;
import com.pyg.search.service.ItemSearchService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author:ccsoftlucifer
 * @date:2018/12/11
 * @description:
 */
@Service(timeout = 10000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;//需要去通过solrtemplate对solr进行操作.
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map searchMap) {
        Map<String,Object> map=new HashMap<>();

        //关键字空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));


        //1.查询列表
        map.putAll(searchList(searchMap));
        //2.分组查询 商品分类列表

        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3.查询品牌和规格列表
        String categoryName=(String)searchMap.get("category");
        if(!"".equals(categoryName)){//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        }else{//如果没有分类名称，按照第一个查询
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    /**
     * 导入数据
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    /**
     * 删除数据
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        System.out.println("删除商品ID"+goodsIdList);
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }

    private Map searchList(Map searchMap){
        Map map=new HashMap();
        HighlightQuery query=new SimpleHighlightQuery();
        //创建一个高亮选项
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
        //设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");

        //创建并且设置好了高亮选项后,将高亮选项给查询选项
        query.setHighlightOptions(highlightOptions);
        //1.1 按照关键字筛选
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //1.2 按分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);

        }
        //1.3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4 过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5 按价格筛选
        //如果价格不为空
        if (!"".equals(searchMap.get("price"))){
            //String '1500-2000'  按照-拆分成string数组
            String[] price = ((String) searchMap.get("price")).split("-");
            //如果起始条件不为0 设置起始条件
            if (!price[0].equals("0")){
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //如果结束条件不为*,设置结束条件
            if (!price[1].equals("*")){
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                FilterQuery filterQuery = new SimpleFacetQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.6 分页查询
        Integer pageNo = (Integer)searchMap.get("pageNo");//提取页码
        if (pageNo==null){
            pageNo=1;
        }
        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if (pageSize==null){
            pageSize=20;
        }
        //从第几条记录开始查询
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);
        //1.7排序
        String sortValue=(String) searchMap.get("sort");//ASC DESC
        String sortField=(String) searchMap.get("sortField");//排序字段
        if (sortValue!=null && !sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        //*********************获取高亮结果集
        //返回一个高亮页对象
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //遍历高亮入口集合.
        for (HighlightEntry<TbItem> h : page.getHighlighted()) {
            TbItem item = h.getEntity();//获取原实体类
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
                //通过高亮入口集合获取高亮列表,get(0)获得第一列 再获得值,本次数据每列只有一个值所以用get(0)
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮结果

            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());
        map.put("total",page.getTotalElements());
        return map;
    }

    /**
     * 查找分类列表
     * @param searchMap
     * @return
     */
    private List searchCategoryList(Map searchMap){
        List<String> list=new ArrayList();
        Query query=new SimpleQuery();//按照关键字查询 相当于where条件
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);//设置分组选项
        //创建一个分组选项 根据哪一个域进行分组.
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //获得分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);//根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");//得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();//得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }

    /**
     * 查询品牌和规格列表
     * @param category 分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category){
        Map map=new HashMap();
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);/// 获取模板 ID
        if(typeId!=null){
            //根据模板 ID 查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);//返回值添加品牌列表
            //根据模板 ID 查询规格列表
            List specList = (List)redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;

    }
}
