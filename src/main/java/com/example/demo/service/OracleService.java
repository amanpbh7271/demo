package com.example.demo.service;

import com.example.demo.model.IncDetails;
import com.example.demo.model.Users;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Time;



@Service
public class OracleService {
    private static final String JDBC_URL = "jdbc:oracle:thin:@INPNQSMRTOP01:1521:SmartOps";
    private static final String USERNAME = "INCBLST";
    private static final String PASSWORD = "Water_4567";

    private static final String USERS_TABLE = "users";
    private static final String NOTIFICATIONS_TABLE = "notifications";
    private static final String NOTIFICATION_MANAGER_TABLE = "notification_manager";

    private Connection connection;

    public OracleService() {
        try {
            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // Create connection
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUserDetails(String username) {
        try {
            String sql = "SELECT * FROM users WHERE username = ?";

            // Prepare the SELECT statement
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);

                // Execute the SELECT statement
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve user details from the result set
                        String name = resultSet.getString("username");
                        String mobile = resultSet.getString("mobile");
                        String password = resultSet.getString("password");
                        String account = resultSet.getString("account");

                        // Construct a JSON string
                        String json = "{\"username\":\"" + name + "\",\"mobile\":\"" + mobile + "\",\"password\":\"" + password + "\",\"account\":\"" + account + "\"}";

                        // Return the JSON string
                        return json;
                    } else {
                        // If no user found, return an appropriate message
                        return "{\"error\": \"User details not found for username: " + username + "\"}";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "{\"error\": \"Error occurred: " + e.getMessage() + "\"}";
        }
    }



    public String insertUserDetails(Users userDetails) {
        try {
            // Check if the user already exists
            String checkSql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE username = ?";
            PreparedStatement checkStatement = connection.prepareStatement(checkSql);
            checkStatement.setString(1, userDetails.getUsername());
            ResultSet resultSet = checkStatement.executeQuery();
            resultSet.next();
            int rowCount = resultSet.getInt(1);

            String sql;
            if (rowCount > 0) {
                // User exists, perform update
                sql = "UPDATE " + USERS_TABLE + " SET password = ?, mobile = ?, account = ? WHERE username = ?";
            } else {
                // User does not exist, perform insert
                sql = "INSERT INTO " + USERS_TABLE + " (password,mobile,account, username ) VALUES (?, ?, ?, ?)";
            }

            PreparedStatement statement = connection.prepareStatement(sql);
            int parameterIndex = 1; // Start setting parameters from index 1

            // For both insert and update operations, set password and mobile
            statement.setString(parameterIndex++, userDetails.getPassword());
            statement.setString(parameterIndex++, userDetails.getMobNumber());

            // Convert account list to JSON string
            String accountJson = new ObjectMapper().writeValueAsString(userDetails.getAccount());

            // Set account JSON string to the PreparedStatement
            statement.setString(parameterIndex++, accountJson);

            if (rowCount > 0) {
                // For update, set username for WHERE clause
                statement.setString(parameterIndex++, userDetails.getUsername());
            } else {
                // For insert, set username as well
                statement.setString(parameterIndex++, userDetails.getUsername());
            }

            // Execute insert or update
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                return "User details inserted/updated successfully";
            } else {
                return "Failed to insert/update user details";
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return "Error occurred while inserting/updating user details";
        }
    }


    public boolean authenticateUser(String username, String password) {
        // Initialize connection
        try  {
            // Prepare SQL statement
            String sql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if the user exists
                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("password");
                        // Compare the stored password with the provided password
                        return password.equals(storedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public Users getUserByUsername(String username) {
        Users user = null;
        try  {
            // Prepare SQL statement
            String sql = "SELECT username, password, mobile FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    // Check if the user exists
                    if (resultSet.next()) {
                        user = new Users();
                        user.setUsername(resultSet.getString("username"));
                        user.setPassword(resultSet.getString("password"));
                        user.setMobNumber(resultSet.getString("mobile"));
                        // You can map other fields as needed
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public String insertIncDetails(IncDetails incDetails) {
        try {
            Date currentDate = new Date();
            java.sql.Date currentSqlDate = new java.sql.Date(currentDate.getTime());
            Time currentTime = new Time(currentDate.getTime());
            // Convert the JSON object to a string
            String preUpdatesJson = new ObjectMapper().writeValueAsString(incDetails.getPreUpdates());

            // Prepare the MERGE statement
            String sql = "MERGE INTO " + NOTIFICATIONS_TABLE + " dest " +
                    "USING (SELECT ? AS \"incNumber\" FROM dual) src " +
                    "ON (dest.\"incNumber\" = src.\"incNumber\") " +
                    "WHEN MATCHED THEN " +
                    "UPDATE SET " +
                    "dest.\"date\" = ?, " +
                    "dest.\"manager\" = ?, " +
                    "dest.\"workAround\" = ?, " +
                    "dest.\"businessImpact\" = ?, " +
                    "dest.\"bridgeDetails\" = ?, " +
                    "dest.\"priority\" = ?, " +
                    "dest.\"issueOwnedBy\" = ?, " +
                    "dest.\"nextUpdate\" = ?, " +
                    "dest.\"preUpdates\" = ?, " +
                    "dest.\"time\" = ?, " +
                    "dest.\"account\" = ?, " +
                    "dest.\"status\" = ? " +
                    "WHEN NOT MATCHED THEN " +
                    "INSERT (\"incNumber\", \"date\", \"manager\", \"workAround\", \"businessImpact\", \"bridgeDetails\", \"priority\", \"issueOwnedBy\", \"nextUpdate\", \"preUpdates\", \"time\", \"account\", \"status\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            // Set the parameters and execute the statement
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set parameters for both UPDATE and INSERT
                statement.setString(1, incDetails.getIncNumber());
                statement.setDate(2, currentSqlDate);
                statement.setString(3, incDetails.getManager());
                statement.setString(4, incDetails.getWorkAround());
                statement.setString(5, incDetails.getBusinessImpact());
                statement.setString(6, incDetails.getBridgeDetails());
                statement.setString(7, incDetails.getPriority());
                statement.setString(8, incDetails.getIssueOwnedBy());
                statement.setString(9, incDetails.getNextUpdate());
                statement.setString(10, preUpdatesJson); // Insert the preUpdates as a string
                statement.setString(11, currentTime.toString());
                statement.setString(12, incDetails.getAccount());
                statement.setString(13, incDetails.getStatus());
                // Set parameters for the INSERT part
                statement.setString(14, incDetails.getIncNumber());
                statement.setDate(15, currentSqlDate);
                statement.setString(16, incDetails.getManager());
                statement.setString(17, incDetails.getWorkAround());
                statement.setString(18, incDetails.getBusinessImpact());
                statement.setString(19, incDetails.getBridgeDetails());
                statement.setString(20, incDetails.getPriority());
                statement.setString(21, incDetails.getIssueOwnedBy());
                statement.setString(22, incDetails.getNextUpdate());
                statement.setString(23, preUpdatesJson); // Insert the preUpdates as a string
                statement.setString(24, currentTime.toString());
                statement.setString(25, incDetails.getAccount());
                statement.setString(26, incDetails.getStatus());

                // Execute the MERGE statement
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    return "Incident details inserted/updated successfully";
                } else {
                    return "Failed to insert/update incident details";
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return "Error occurred while inserting/updating incident details: " + e.getMessage();
        }
    }


    public String getIncDetailsForManager(String manager) {
        String queryString = "SELECT * FROM notifications WHERE \"manager\" = '" + manager + "' AND \"status\"= 'Open' ORDER BY \"date\" DESC, \"time\" DESC";
        System.out.println("Query: " + queryString);

        // Execute the SQL query using JDBC
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(queryString);

            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            while (resultSet.next()) {
                ObjectNode rowObject = objectMapper.createObjectNode();
                rowObject.put("incNumber", resultSet.getString("incNumber"));
                rowObject.put("account", resultSet.getString("account"));
                rowObject.put("nextUpdate", resultSet.getString("nextUpdate"));
                rowObject.put("status", resultSet.getString("status"));
                rowObject.put("businessImpact", resultSet.getString("businessImpact"));
                rowObject.put("workAround", resultSet.getString("workAround"));
                rowObject.put("bridgeDetails", resultSet.getString("bridgeDetails"));
                rowObject.put("priority", resultSet.getString("priority"));
                rowObject.put("manager",resultSet.getString("manager"));

// Format date without time component
                Date date = resultSet.getDate("date");
                rowObject.put("date", date != null ? dateFormat.format(date) : null);

                // Include the time component if needed
                rowObject.put("time", resultSet.getString("time"));


                rowObject.put("preUpdates", resultSet.getString("preUpdates"));
                rowObject.put("issueOwnedBy",resultSet.getString("issueOwnedBy"));
                resultArray.add(rowObject);
            }

            String res = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
            System.out.println("Result: " + res);
            return res;

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    public String getInsDetailsWithIncId(String incNumber) {
        try {
            // Prepare the SQL query
            String queryString = "SELECT * FROM notifications WHERE  \"incNumber\"  = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(queryString);
            preparedStatement.setString(1, incNumber);

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Create an ObjectMapper to convert the result to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();

            // Iterate over the result set and build the JSON array
            while (resultSet.next()) {
                ObjectNode rowObject = objectMapper.createObjectNode();
                rowObject.put("incNumber", resultSet.getString("incNumber"));
                rowObject.put("account", resultSet.getString("account"));
                rowObject.put("nextUpdate", resultSet.getString("nextUpdate"));
                rowObject.put("status", resultSet.getString("status"));
                rowObject.put("businessImpact", resultSet.getString("businessImpact"));
                rowObject.put("workAround", resultSet.getString("workAround"));
                rowObject.put("bridgeDetails", resultSet.getString("bridgeDetails"));
                rowObject.put("priority", resultSet.getString("priority"));
                rowObject.put("date", resultSet.getString("date"));
                rowObject.put("time", resultSet.getString("time"));
                rowObject.put("issueOwnedBy",resultSet.getString("issueOwnedBy"));
                rowObject.put("manager",resultSet.getString("manager"));
                // Convert the preUpdates string to a JSON array
                try {
                    ArrayNode preUpdatesArray = (ArrayNode) objectMapper.readTree(resultSet.getString("preUpdates"));
                    rowObject.set("preUpdates", preUpdatesArray);
                } catch (IOException e) {
                    // Handle the exception
                    e.printStackTrace();
                    rowObject.putNull("preUpdates");
                }

                resultArray.add(rowObject);
            }

            // Convert the JSON array to a string
            String res = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
            System.out.println("Result: " + res);
            return res;

        } catch (SQLException | JsonProcessingException e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }

    public String getManagerForAccount(String account) {
        try {
            // Prepare the SQL query
            String queryString = "SELECT * FROM NotificationManager WHERE name = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(queryString);
            preparedStatement.setString(1, account);

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Create a StringBuilder to build the JSON string
            StringBuilder jsonResult = new StringBuilder("[ ");

            // Iterate over the result set and build the JSON string
            boolean isFirst = true;
            while (resultSet.next()) {
                if (!isFirst) {
                    jsonResult.append(", ");
                }
                jsonResult.append("{ ");
                jsonResult.append("\"name\": \"").append(account).append("\", ");

                // Parse the managersString
                String managersString = resultSet.getString("managers");
                jsonResult.append("\"managers\": ").append(managersString).append(", ");

                jsonResult.append("\"type\": \"account\" ");
                jsonResult.append("}");
                isFirst = false;
            }
            jsonResult.append(" ]");

            // Print the JSON string
            String res = jsonResult.toString();
            System.out.println("Result: " + res);
            return res;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }


    public String getAccountForUser(String user) {
        try {
            // Prepare the SQL query
            String queryString = "SELECT account FROM users WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(queryString);
            preparedStatement.setString(1, user);

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process the result
            StringBuilder resultBuilder = new StringBuilder();
            while (resultSet.next()) {
                String account = resultSet.getString("account");
                resultBuilder.append(account).append("\n");
            }

            // Convert the result to a string
            String result = resultBuilder.toString();
            System.out.println("Result of query: " + result);
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
    }




    // Implement other methods similarly

    // Remember to close connection when done
    public void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


