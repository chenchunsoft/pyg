package com.pyg.service;

import com.pyg.pojo.TbBrand;
import entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/11/27
 * @description:接口只依赖于pojo不能依赖于dao
 */
public interface BrandService {
    /**
     * 查找所有
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 分页功能
     * @param pageNum
     * @param pagesize
     * @return
     */
    public PageResult findPage(int pageNum,int pagesize);

    /**
     * 增加
     * @param brand
     */
    public void add(TbBrand brand);

    /**
     * 根据ID查询实体,然后在update()方法中对该查询到的实体进行删除.
     * @param id
     * @return
     */
    public TbBrand findOne(Long id);

    /**
     * 修改brand品牌信息
     * @param tbBrand
     */
    public void update(TbBrand tbBrand);

    /**
     * 删除brand品牌信息
     * 循环数组删除
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 指定品牌 分页
     * @param brand
     * @param pageNum
     * @param pageSize
     * @return
     */
  public PageResult findPage(TbBrand brand,int pageNum ,int pageSize);

    /**
     * 品牌下拉框数据
     * @return
     */
  List<Map> selectOptionList();
}
