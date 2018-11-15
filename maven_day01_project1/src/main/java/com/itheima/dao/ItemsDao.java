package com.itheima.dao;

import com.itheima.domain.Items;
import sun.rmi.server.InactiveGroupException;

/**
 * @author:ccsoftlucifer
 * @date:2018/11/14
 * @description:
 */
public interface ItemsDao {
    public Items findById(Integer id);
}
