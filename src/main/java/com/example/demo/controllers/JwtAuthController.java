package com.example.demo.controllers;


import com.example.demo.db.DBManager;
import com.example.demo.entities.Roles;
import com.example.demo.entities.Users;
import com.example.demo.jwt.JwtTokenGenerator;
import com.example.demo.models.JwtRequest;
import com.example.demo.models.JwtResponse;
import com.example.demo.models.UserDTO;
import com.example.demo.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
public class JwtAuthController {

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;
    private static Authentication authentication;

    @Autowired
    private UserService userService;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private Users getUser() {

        if (authentication != null) {
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                Users user = (Users) authentication.getPrincipal();
                return user;
            }
        }
        return null;
    }

//{"email":"diana", "password":"diana"}
    @RequestMapping(value = "/auth")
    public ResponseEntity<?> auth(@RequestBody JwtRequest request, HttpServletRequest req) throws Exception{
        authenticate(request.getEmail(), request.getPassword(),req);
        final UserDetails userDetails =
                userService.loadUserByUsername(request.getEmail());
        final String token = jwtTokenGenerator.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));

    }

    public void authenticate(String email, String password,HttpServletRequest req) throws Exception{
        try{
          
            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password)));

            HttpSession session = req.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);
            authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(SecurityContextHolder.getContext().getAuthentication() +"Authentication");

        }catch (DisabledException e){
            throw new Exception("USER_DISABLED", e);
        }catch (BadCredentialsException e){
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    @RequestMapping(value="/check")
    public Boolean check() {
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Check"+authentication );
        if (authentication == null) return false;
        return true;
    }

    @RequestMapping(value="/logout")
    public String logoutPage() {

        if (authentication != null){
             authentication = null;
        }
        return "logout successfully";
    }

    @RequestMapping("/updatePassword")
    public String updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {

        Users users = getUser();
        if (encoder.matches(oldPassword, users.getPassword())) {
            users.setPassword(encoder.encode(newPassword));
            DBManager.editUser(users);
            return "success";
        } else {
            return "are not equal";
        }
    }


    @RequestMapping(value = "/profile")
    public ResponseEntity<?> profilePage() {
        Users user = getUser();
        return new ResponseEntity<>(new UserDTO(user.getId(), user.getEmail(), user.getRoles()), HttpStatus.OK);
    }

    @RequestMapping("/signup")
    public ResponseEntity<?> registerUser( @RequestParam String email, @RequestParam String password ) {
       if(DBManager.getAllUsers()!=null) {

           if (DBManager.existsByEmail(email)) {
               return ResponseEntity
                       .badRequest()
                       .body("Error: Email is already in use!");
           }
       }
       Users user = new Users(email,encoder.encode(password));
       List<Roles> roles = new ArrayList<>();

        Roles userRole = DBManager.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        DBManager.saveUser(user);
        List<Users> users = DBManager.getAllUsers();
        for(Users u: users){
            System.out.println(u.getPassword()+" " +u.getUsername());
        }

        return ResponseEntity.ok("User registered successfully!");
    }

}