package com.cqh.miaosha.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cqh.miaosha.dao.UserDao;
import com.cqh.miaosha.domain.User;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    public User getByid(int id) {
        return userDao.getById(id);
    }

    @Transactional
    public boolean tx() {
        User user1 = new User();
        user1.setId(2);
        user1.setName("wefw");
        userDao.insert(user1);

        User user2 = new User();
        user1.setId(1);
        user1.setName("weffsdw");
        userDao.insert(user2);
        return true;
    }
}
