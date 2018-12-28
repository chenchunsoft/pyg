package com.pyg.web;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pyg.pojo.TbBrand;
import com.pyg.service.BrandService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author:ccsoftlucifer
 * @date:2018/11/27
 * @description:
 */
@RestController
@RequestMapping("brand")
public class BrandController {
    @Reference
    private BrandService brandService;

    /**
     * 查找所有数据
     * @return
     */
    @RequestMapping("findAll")
    public List<TbBrand> findAll(){
        return brandService.findAll();
    }

    /**
     * 分页功能
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page,int rows){
        return  brandService.findPage(page,rows);
    }

    /**
     * 新增Brand品牌信息
     * @param brand
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TbBrand brand){
        try {
            brandService.add(brand);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

    /**
     * 查找知道指定的品牌
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public TbBrand findOne(Long id){
        return brandService.findOne(id);
    }

    /**
     * 更新
     * @param brand
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody TbBrand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }

    /**
     * 模糊查询
     * @param brand
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbBrand brand,int page,int rows){
        return brandService.findPage(brand,page,rows);
    }

    /**
     * 获得下拉列表数据
     * @return
     */
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
        return  brandService.selectOptionList();
    }

}
