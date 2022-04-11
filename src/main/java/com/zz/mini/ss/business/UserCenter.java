package com.zz.mini.ss.business;

import com.zz.mini.ss.db.CacheDb;
import org.springframework.stereotype.Service;

@Service
public class UserCenter {

    public void addUser(String username) {
        CacheDb.db.userCaches.add(username);
    }
}
