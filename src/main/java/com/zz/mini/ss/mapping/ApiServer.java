package com.zz.mini.ss.mapping;

import com.zz.mini.ss.business.UserCenter;
import com.zz.mini.ss.net.OkTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiServer {

    @Autowired
    private UserCenter userCenter;

    @RequestMapping("/login")
    public String login(
            @RequestParam(name = "username") String username
    ) {
        System.out.println("login:" + username);
        System.out.println(userCenter);
        return "{\"data\":true}";
    }

    @RequestMapping("/live")
    public String live() {
        OkTest.Companion.test();
        return "ok";
    }

}
