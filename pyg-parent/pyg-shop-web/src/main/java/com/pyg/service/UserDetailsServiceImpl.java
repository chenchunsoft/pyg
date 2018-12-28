package com.pyg.service;

import com.pyg.pojo.TbSeller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author:ccsoftlucifer
 * @date:2018/12/4
 * @description:认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService{
    private  SellerService sellerService;
    public void setSellerService(SellerService sellerService){
        this.sellerService = sellerService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了:"+this.getClass().getName());
        //构建角色列表
        List<GrantedAuthority> grantedAuthorityList = new ArrayList<>();
        grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //得到商家对象
        TbSeller seller = sellerService.findOneByStringId(username);
        if (seller!=null){
            if (seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantedAuthorityList);
            }else {
                return null;
            }
        }else{
            return null;
        }
    }
}
