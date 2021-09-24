package com.example.demo.services.impl;


import com.example.demo.db.DBManager;
import com.example.demo.entities.Users;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Users user = DBManager.findByEmail(s);
        if(user != null){
            return user;
        }else {
            throw new UsernameNotFoundException("USER NOT FOUND");
        }
    }
}
