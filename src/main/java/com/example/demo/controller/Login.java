package com.example.demo.controller;



import com.example.demo.model.IncDetails;
import com.example.demo.model.Users;
import com.example.demo.repository.CouchbaseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:3003")
@RestController
@RequestMapping("/api")
public class Login {
    private static final Logger logger = LoggerFactory.getLogger(Login.class);

    @Autowired
    private CouchbaseService couchbaseService;


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
    @PostMapping("/login")
    public String login(@RequestBody Users user) throws JsonProcessingException {

        //logger.debug("user name is "+user.getUsername());
//        JsonDocument jsonDocument = couchbaseService.getDocumentUser(user.getUsername());
//        String result = jsonDocument.toString();
//        System.out.println("result is "+ result);

        //  User result = userRepository.findByUserName(user.getUsername());
        //  JsonDocument jsonDocument =usersBucket.get(user.getUsername());
        //  System.out.print("user name is "+jsonDocument);
//        User user1= userRepository.findByUserName(user.getUsername());

//        Optional<User> user1=userRepository.findById("583d7542-7f25-431a-979a-79b300554b0d") ;
//
//        System.out.println(user1);


        // System.out.print("user name is "+user.getUsername());
        //   userService.findByUserName("583d7542-7f25-431a-979a-79b300554b0d");



        return couchbaseService.getDocumentWithId(user.getUsername());

//        System.out.println("succuss");
//        return "login succsufully";
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
}
