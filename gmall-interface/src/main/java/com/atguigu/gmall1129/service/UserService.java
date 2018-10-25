package com.atguigu.gmall1129.service;

import com.atguigu.gmall1129.bean.UserAddress;
import com.atguigu.gmall1129.bean.UserInfo;

import java.util.List;

/**
 * @param
 * @return
 */
public interface UserService {


    public  void addUserInfo(UserInfo userInfo);

    public UserInfo getUserInfo(String id);

    public List<UserInfo> getUserList(UserInfo userInfo);

    public void updateUser(UserInfo userInfo);


    public List<UserAddress> getUserAddressList(String userId);

//    public UserInfo login(UserInfo userInfo);
//
//    public boolean verify(String userId);
}
