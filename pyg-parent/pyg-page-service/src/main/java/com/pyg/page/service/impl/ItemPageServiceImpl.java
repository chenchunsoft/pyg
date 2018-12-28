package com.pyg.page.service.impl;


import com.pyg.mapper.TbGoodsDescMapper;
import com.pyg.mapper.TbGoodsMapper;
import com.pyg.mapper.TbItemCatMapper;
import com.pyg.mapper.TbItemMapper;
import com.pyg.page.service.ItemPageService;
import com.pyg.pojo.TbGoods;
import com.pyg.pojo.TbGoodsDesc;
import com.pyg.pojo.TbItem;
import com.pyg.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/14
 * @description:
 */
@Service
public class ItemPageServiceImpl implements ItemPageService{
    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;
    /**
     *
     * @param goodsId
     * @return
     */
    @Override
    public boolean genItemHtml(Long goodsId) {
       try{
           //获得配置对象
           Configuration configuration = freeMarkerConfig.getConfiguration();
           //通过配置对象获得模板
           Template template = configuration.getTemplate("item.ftl");
           template.setEncoding("UTF-8");


           //创建数据模型
           Map dataModel = new HashMap<>();
           //1. 加载商品表数据
           TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
           dataModel.put("goods",goods);
           //2.加载商品扩展表数据
           TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
           dataModel.put("goodsDesc",goodsDesc);

           //读取商品一级分类
           String name1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
           String name2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
           String name3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",name1);
            dataModel.put("itemCat2",name2);
            dataModel.put("itemCat3",name3);
            //读取SKU列表数据。
           TbItemExample example = new TbItemExample();
           TbItemExample.Criteria criteria = example.createCriteria();
           criteria.andGoodsIdEqualTo(goodsId);//陪陪skuID
           criteria.andStatusEqualTo("1");
           example.setOrderByClause("is_default desc");//按照是否默认字段进行默认排序 目的是返回第一条数据为默认SKU
           List<TbItem> itemList = itemMapper.selectByExample(example);



           File fileout = new File(pagedir+goodsId+".html");
           Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileout), "UTF-8"));
           dataModel.put("itemList",itemList);
           //用模板对象template将dataModel与out都传过去.
           template.process(dataModel,out);


           
           out.close();
           return true;

       }catch (Exception e){
           System.out.println(e);
           return false;
       }

    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for(Long goodsId:goodsIds){
                //删除文件
                new File(pagedir+goodsId+".html").delete();
                System.out.println("文件删除了 id: "+goodsId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
