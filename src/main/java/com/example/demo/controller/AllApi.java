package com.example.demo.controller;


import com.example.demo.model.IncDetails;
import com.example.demo.model.Managers;
import com.example.demo.model.Users;
import com.example.demo.service.OracleService;
import com.example.demo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class AllApi {

    @Autowired
    public OracleService oracleService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/getUserDetails/{username}")
    public String createUser(@PathVariable String username) {
        System.out.println("Users form "+username);
        return oracleService.getUserDetails(username);
        // return userService.saveUser(user);
    }
    @PostMapping("/insertUser")
    public String insertUserDetails(@RequestBody Users userDetails) {
        // Call the CouchbaseService method to insert user details

        return oracleService.insertUserDetails(userDetails) ;
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        boolean isAuthenticated = oracleService.authenticateUser(user.getUsername(), user.getPassword());
        if (isAuthenticated) {
            // Fetch user details
            Users userDetails = oracleService.getUserByUsername(user.getUsername());
            if (userDetails != null) {
                // Generate JWT token
                final String token = jwtUtil.generateToken(user.getUsername());
                // Create a response DTO containing token and user details
                AuthResponse authResponse = new AuthResponse(token, userDetails);
                // Return the response entity with status code OK (200) and the response body containing the token and user details
                return ResponseEntity.ok(authResponse);
            } else {
                // If userDetails is null, return internal server error
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to fetch user details");
            }
        } else {
            // If authentication fails, return unauthorized status code
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
    }


    @PostMapping("/saveInc")
    public  String saveInc(@RequestBody IncDetails incDetails){
        System.out.println("inc Details"+incDetails);
        return  oracleService.insertIncDetails(incDetails);

    }

    @GetMapping("/incDetailsForManager/{manager}")
    public String incDetailsForManager(@PathVariable String manager){
        System.out.println("Manger name is"+ manager);
        return oracleService.getIncDetailsForManager(manager);

    }

    @GetMapping("/incDetails/{incNumber}")
    public String insDetailsWithIncId(@PathVariable String incNumber){
        System.out.println("ind Id is"+incNumber);
        return oracleService.getInsDetailsWithIncId(incNumber) ;
    }

    @GetMapping("/managerForAccount/{account}")
    public String mangerForAccount(@PathVariable String account){
        System.out.println("account name in managerForAccount"+account);
        return oracleService.getManagerForAccount(account);
    }
    @GetMapping("/accountForUser/{user}")
    public String getAccountForUser(@PathVariable String user){
        System.out.println("user "+user);
        return oracleService.getAccountForUser(user);
    }

}
