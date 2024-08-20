package com.example.logtest.controller;



import com.example.logtest.model.INC_USERS;
import com.example.logtest.model.IncDetails;
import com.example.logtest.model.Users;
import com.example.logtest.service.OracleService;
import com.example.logtest.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class AllApi {

    @Autowired
    public OracleService oracleService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/test")
    public String test() {
        return "this is test api";
    }



    @GetMapping("/getUserDetails/{username}")
    public String createUser(@PathVariable String username) {
        System.out.println("Users form "+username);
        return oracleService.getUserDetails(username);
        // return userService.saveUser(user);
    }
//    @PostMapping("/insertUser")
//    public String insertUserDetails(@RequestBody Users userDetails) {
//        // Call the CouchbaseService method to insert user details
//
//        return oracleService.insertUserDetails(userDetails) ;
//    }

    @PostMapping("/insertUser")
    public String insertUserDetails(@RequestBody INC_USERS userDetails) {
        // Call the CouchbaseService method to insert user details
        System.out.println("userDetails form insertusers"+userDetails);
        return oracleService.insertOrUpdateUserDetails(userDetails) ;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        boolean isAuthenticated = oracleService.doNTNETAuthentication(user.getUsername(), user.getPassword());
        if (isAuthenticated) {
            // Fetch user details from your service
            INC_USERS userDetails = oracleService.getUserByUsername(user.getUsername());
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

    @GetMapping("/incidentsForAccounts")
    public ResponseEntity<String> getIncidentsForAccounts(@RequestParam List<String> accounts) {
        try {
            String incidents = oracleService.getIncidentsForAccounts(accounts);
            return new ResponseEntity<>(incidents, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
    @GetMapping("/accountForUser/{userId}")
    public String getAccountForUser(@PathVariable String userId){
        System.out.println("user "+userId);
        return oracleService.getAccountForUser(userId);
    }

    @GetMapping("/timeForPriority/{priority}")
    public String getTimeForAccountAndPriority(@PathVariable String priority){

        System.out.println("Priority "+priority);
        return  oracleService.getTimeForPriority(priority);
    }


    @GetMapping("/listOfIncFromAccountAndPriority/{priority}/{account}")
    public String getIncidentsByPriorityAndAccount(@PathVariable String priority, @PathVariable String account) {
        return oracleService.getIncidentsByPriorityAndAccount(priority, account);
    }

    @GetMapping("/regionForUser/{userId}")
    public String getRegionForUser(@PathVariable String userId){
        System.out.println("User Id"+userId);
        return oracleService.getUserForRegion(userId);
    }

    @GetMapping("/userAccounts/{userId}/{regionId}")
    public String getUserAccounts(@PathVariable String userId, @PathVariable int regionId) {
        System.out.println("User Id: " + userId + ", Region Id: " + regionId);
        return oracleService.getUserAccounts(userId, regionId);
    }


    @GetMapping("/managerForAccounts/{accountName}")
    public String getManagerForAccounts(@PathVariable String accountName) {
        System.out.println("Account Name: " + accountName);
        return oracleService.getManagerForAccounts(accountName);
    }



    @GetMapping("/getAllRegions")
    public String getAllRegions(){
        System.out.println("All Regions" );
        return oracleService.getAllRegions();
    }

    @GetMapping("/getAllAccountsForRegions/{regionId}")
    public String getAllAccountsForRegions(@PathVariable String regionId) {
        System.out.println("Region IDs: " + regionId);
        // Convert regionIds from comma-separated string to a list or array
        String[] regionArray = regionId.split(",");
        // Call the service to get accounts for these regions
        return oracleService.getAccountsForRegions(regionArray);
    }



}
