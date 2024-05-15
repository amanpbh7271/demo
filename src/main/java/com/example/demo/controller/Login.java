package com.example.demo.controller;



import com.example.demo.model.IncDetails;
import com.example.demo.model.Users;
import com.example.demo.util.JwtUtil;
import com.example.demo.repository.CouchbaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @Autowired
    private CouchbaseService couchbaseService;


    @Autowired
    private JwtUtil jwtUtil;


    // endpoint to insert user details
    @PostMapping("/insertUser")
    public String insertUserDetails(@RequestBody Users userDetails) {
        // Call the CouchbaseService method to insert user details
        return couchbaseService.insertUserDetails(userDetails);
    }

    // New endpoint to retrieve user details
    @GetMapping("/getUserDetails/{username}")
    public String getUserDetails(@PathVariable String username) {
        // Call the CouchbaseService method to retrieve user details
        return couchbaseService.getUserDetails(username);
    }
//    @PostMapping("/login")
//    public String login(@RequestBody Users user) throws JsonProcessingException {
//
//        return couchbaseService.getDocumentWithId(user.getUsername());
//
//
//    }




    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users user) {
        boolean isAuthenticated = couchbaseService.authenticateUser(user.getUsername(), user.getPassword());
        if (isAuthenticated) {
            // Fetch user details
            Users userDetails = couchbaseService.getUserByUsername(user.getUsername());
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



    @GetMapping ("/logout/{User}")
    public String logout(@PathVariable String User){
        return User +" logout successfully";
    }
    @PostMapping("/saveInc")
    public  String saveInc(@RequestBody IncDetails incDetails){
       System.out.println("inc Details"+incDetails);
       return couchbaseService.insertIncDetails(incDetails);

    }

    @GetMapping("/incDetailsForManager/{manager}")
    public String incDetailsForManager(@PathVariable String manager){
      System.out.println("Manger name is"+ manager);
       return couchbaseService.getIncDetailsForManager(manager);

    }
    @GetMapping("/incDetails/{incNumber}")
    public String insDetailsWithIncId(@PathVariable String incNumber){
        System.out.println("ind Id is"+incNumber);
        return couchbaseService.getInsDetailsWithIncId(incNumber) ;
    }
    @GetMapping("/managerForAccount/{account}")
    public String mangerForAccount(@PathVariable String account){
        System.out.println("account name in managerForAccount"+account);
        return couchbaseService.getManagerForAccount(account);
    }

    @GetMapping("/listOfIncFromAccountAndPriority/{account}/{priority}")
    public String getListOfIncFromAccountAndPriority(@PathVariable String account, @PathVariable String priority){
        System.out.println("Account: " + account + ", Priority: " + priority);
        return couchbaseService.getListOfIncFromAccount(account, priority);
    }

    @GetMapping("/accountForUser/{user}")
    public String getAccountForUser(@PathVariable String user){
        System.out.println("user "+user);
        return couchbaseService.getAccountForUser(user);
    }

}
