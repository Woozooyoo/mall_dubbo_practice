package com.atguigu.gmall1129.usermanage.controller;

import com.atguigu.gmall1129.bean.UserInfo;
import com.atguigu.gmall1129.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @param
 * @return
 */

@RestController
public class UserController {

    @Autowired
    UserService userService;

	//增
    @PostMapping("/user")
    public String addUser(UserInfo userInfo){
        userService.addUserInfo(userInfo);
        return "success";
    }

	//查
    @GetMapping("/user")
    public UserInfo getUserInfo(@RequestParam("id") String id){
        UserInfo userInfo = userService.getUserInfo(id);
        return userInfo;
    }

    //批量查
    @RequestMapping("/users")
    public ResponseEntity<List<UserInfo>> getUserList(UserInfo userInfo){
        List<UserInfo> userInfoList = userService.getUserList(userInfo);
        return ResponseEntity.ok().body(userInfoList);
    }

    //改
    @RequestMapping(value = "/user" ,method = RequestMethod.PUT)
    public    ResponseEntity<Void> update(UserInfo userInfo){
        userService.updateUser(userInfo);
        return ResponseEntity.ok().build();
    }

/*    @RequestMapping(value = "/user" ,method = RequestMethod.DELETE)
    public    ResponseEntity<Void> delete(UserInfo userInfo){
        userManageService.delete(userInfo);
        return ResponseEntity.ok().build();
    }*/




}
