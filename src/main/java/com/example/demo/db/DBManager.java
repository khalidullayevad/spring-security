package com.example.demo.db;



import com.example.demo.entities.Roles;
import com.example.demo.entities.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DBManager {
    private static ArrayList<Users> users = new ArrayList<>();
    private static ArrayList<Roles> roles = new ArrayList<>();

    private static Long userId = 1L;

    static {
        roles.add(new Roles(1L, "ROLE_USER"));
        roles.add(new Roles(2L, "ROLE_ADMIN"));
        roles.add(new Roles(3L, "ROLE_MODERATOR"));
    }

    static {
        users.add(new Users(1l,"diana", "$2a$12$sFnLwfgZqzINdlHxxjj2vOPCrghJKT9ydU2Rjd.ovQB7481u9YbYK",roles));
    }

    public static List<Users> getAllUsers() {
        return users;
    }

    public static Users findByEmail(String s) {
        for (Users u : users) {
            if (u.getEmail().equals(s)) {
                return u;
            }
        }
        return null;
    }


    public static boolean existsByEmail(String email) {
        for (Users u : users) {
            if (u.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }



    public static Optional<Roles> findByName(String name){
        for(Roles r: roles){
            if (r.getName().equals(name)) {

                return Optional.of(r);
            }

        }
        return null;
    }

    public static Users saveUser(Users u){
        System.out.println("Saved: " + u.getUsername()+" "+u.getPassword());
        if(u!=null) {
            u.setId(userId);
            users.add(u);
            userId++;
            return u;
        }
        return null;
    }

    public static Users editUser(Users u){

        System.out.println("Saved: " + u.getUsername()+" "+u.getPassword());
        for(Users t: users){
            if(t.getId() == u.getId()){
                t.setPassword(u.getPassword());
            }
        }
        return u;
    }


}