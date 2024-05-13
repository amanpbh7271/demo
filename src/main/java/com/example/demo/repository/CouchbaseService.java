package com.example.demo.repository;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.example.demo.model.IncDetails;
import com.example.demo.model.Preupdate;
import com.example.demo.model.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.util.ArrayList;
import java.util.List;


@Service
public class CouchbaseService {
    @Value("${couchbase.host}")
    private String host;

    @Value("${couchbase.auth.username}")
    private String username;

    @Value("${couchbase.auth.password}")
    private String password;


    private static final String NOTIFICATIONS_BUCKET = "notifications";
    private static final String USERS_BUCKET = "users"; // Add the name of the users bucket

    private static final String NotificationManager_BUCKET ="NotificationManager";
    Bucket usersBucket;
    Bucket notificationsBucket;

    Bucket notificationManagerBucket;

    @PostConstruct
    private void postConstructor() {
        Cluster cluster = CouchbaseCluster.create(host);
        cluster.authenticate(username, password);
        usersBucket = cluster.openBucket(USERS_BUCKET);
        notificationsBucket = cluster.openBucket(NOTIFICATIONS_BUCKET);
        usersBucket = cluster.openBucket(USERS_BUCKET); // Open the users bucket
        notificationManagerBucket = cluster.openBucket(NotificationManager_BUCKET);
    }

    public JsonDocument getDocumentUser(String user){
        return usersBucket.get(user);
    }

    public String getUserDetails(String username) {
        // Retrieve the JsonDocument corresponding to the username from the users bucket
        JsonDocument document = usersBucket.get(username);

        // Check if the document exists
        if (document != null) {
            // Return the content of the document as a JSON string
            return document.content().toString();
        } else {
            // If the document does not exist, return an appropriate message
            return "User details not found for username: " + username;
        }
    }
    public String insertUserDetails(Users userDetails) {
        // Create a JsonObject from UserDetails object
        JsonObject userJson = JsonObject.create()
                .put("username", userDetails.getUsername())
                .put("password", userDetails.getPassword())
                .put("mobile", userDetails.getMobNumber()); // Add other fields as needed

        // Insert the JsonObject into the users bucket
        JsonDocument inserted = usersBucket.upsert(JsonDocument.create(userDetails.getUsername(), userJson));

        // Return the inserted document as a string
        return inserted.toString();
    }
    public String getDocumentWithId(String id) throws JsonProcessingException {
        String queryString = "SELECT * FROM `users` WHERE name = '"+id+"'" ;
        N1qlQueryResult result = usersBucket.query(N1qlQuery.simple(queryString));

        ObjectMapper objectMapper = new ObjectMapper();

         List<N1qlQueryRow> list  =result.allRows();
         String finalres=null;
         for(N1qlQueryRow n1qlQueryRow: list) {
             finalres=  n1qlQueryRow.value().toString();
         }
        System.out.println("result is "+ list);
         //String json =objectMapper.writeValueAsString(list);

       if (finalres!=null)
        return finalres;
       else
           return "Please put Correct Username and Password";
    }

    public String insertIncDetails(IncDetails incDetails) {
        JsonObject inc = JsonObject.create();
         inc.put("incNumber",incDetails.getIncNumber());
         inc.put("account",incDetails.getAccount());
         inc.put("nextUpdate",incDetails.getNextUpdate());
         inc.put("status",incDetails.getStatus());
         inc.put("businessImpact",incDetails.getBusinessImpact());
         inc.put("workAround",incDetails.getWorkAround());
         inc.put("issueOwnedBy",incDetails.getIssueOwnedBy());
         inc.put("bridgeDetails",incDetails.getBridgeDetails());
         inc.put("priority",incDetails.getPriority());
         inc.put("manager",incDetails.getManager());
         inc.put("date",incDetails.getDate());
         inc.put("time",incDetails.getTime());

        if (incDetails.getPreUpdates() != null) {
            System.out.println("in a preupdates"+ incDetails.getPreUpdates());
            JsonArray  preUpdates = JsonArray.create();
            for (Preupdate preUpdate : incDetails.getPreUpdates()) {

                JsonObject preUpdateJson = JsonObject.create()
                        .put("timestamp", preUpdate.getTimestamp())
                        .put("message", preUpdate.getMessage());
                preUpdates.add(preUpdateJson);
            }
            inc.put("preUpdates", preUpdates);
        }
        System.out.println(inc);

         JsonDocument res =notificationsBucket.upsert(JsonDocument.create(incDetails.getIncNumber(), inc));

         System.out.println("submitted");
        return res.toString();
    }
    public  String getIncDetailsForManager(String manager){

        String queryString = "SELECT * FROM `notifications` WHERE  manager = '"+manager+"' and status='Open' ORDER BY date DESC, time DESC" ;
       System.out.println("query is "+ queryString);
        N1qlQueryResult result = notificationsBucket.query(N1qlQuery.simple(queryString));
        List<N1qlQueryRow> list  =result.allRows();

        String res = list.toString();
        System.out.println("result of query is "+ res);
        return res;
    }

    public String getInsDetailsWithIncId(String incNumber){


        String queryString = "SELECT * FROM `notifications` WHERE incNumber = '" + incNumber + "' ORDER BY date DESC, time DESC";
        System.out.println("Query: " + queryString);
        N1qlQueryResult result = notificationsBucket.query(N1qlQuery.simple(queryString));


        List<N1qlQueryRow> list = result.allRows();
        String res = list.toString();
        System.out.println("Result of query: " + res);
        return res;


    }
    public String getManagerForAccount(String account){
        String queryString = "SELECT * FROM `NotificationManager` WHERE name = '" + account + "'";
        System.out.println("Query: " + queryString);
        N1qlQueryResult result = notificationManagerBucket.query(N1qlQuery.simple(queryString));
        List<N1qlQueryRow> list = result.allRows();
        String res = list.toString();
        System.out.println("Result of query: " + res);
        return res;
    }


    public String getListOfIncFromAccount(String account, String priority) {
        String queryString = "SELECT incNumber FROM `notifications` WHERE account = '" + account + "' AND priority = '" + priority + "' AND status = 'Open'";
        System.out.println("Query: " + queryString);
        N1qlQueryResult result = notificationsBucket.query(N1qlQuery.simple(queryString));
        List<N1qlQueryRow> list = result.allRows();
        String res = list.toString();
        System.out.println("Result of query: " + res);
        return res;
    }

    public String getAccountForUser(String user){
         String queryString ="SELECT account from users where username= '"+ user +"'";
        System.out.println("Query: " + queryString);
        N1qlQueryResult result = usersBucket.query(N1qlQuery.simple(queryString));
        List<N1qlQueryRow> list = result.allRows();
        String res = list.toString();
        System.out.println("Result of query: " + res);
         return res;

    }
}
