package com.example.logtest.service;



import com.example.logtest.model.INC_USERS;
import com.example.logtest.model.IncDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jcifs.UniAddress;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Service
public class OracleService {
    private static final Logger logger = LoggerFactory.getLogger(OracleService.class);

    private static final String JDBC_URL = "jdbc:oracle:thin:@INPNQSMRTOP01:1521:SmartOps";
    private static final String USERNAME = "INCBLST";
    private static final String PASSWORD = "Water_4567";

    private static final String USERS_TABLE = "users";
    private static final String NOTIFICATIONS_TABLE = "notifications";
    private static final String NOTIFICATION_MANAGER_TABLE = "notification_manager";




    public Connection getConnection() {
        logger.info("Inside OracleService class getConnection method start method");
        try {
            Connection connection = null;
            // Load Oracle JDBC driver
            Class.forName("oracle.jdbc.driver.OracleDriver");
            if (connection != null) {
                return connection;
            }
            else{
                logger.info("Inside OracleService class getConnection method getConnection");
                return  DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            }

        } catch (ClassNotFoundException | SQLException e) {
            logger.error("Inside OracleService class getConnection method getConnection error"+ e.getMessage());
            e.printStackTrace();
            return null;
        }
    }





    public String getUserDetails(String username) {
        logger.info("Inside OracleService class getUserDetails method start method");
        logger.debug("Inside OracleService class getUserDetails method start method");

        Connection con = getConnection();
        try {

            String sql = "SELECT * FROM users WHERE username = ?";

            // Prepare the SELECT statement

            try (PreparedStatement statement = con.prepareStatement(sql)) {
                logger.info("Inside OracleService class getUserDetails method PreparedStatement called");
                statement.setString(1, username);

                // Execute the SELECT statement
                try (ResultSet resultSet = statement.executeQuery()) {
                    logger.info("Inside OracleService class getUserDetails method executeQuery called");
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
            logger.error("Inside OracleService class getUserDetails method exception message "+e.getMessage());
            logger.error("Inside OracleService class getUserDetails method exception "+e);
            e.printStackTrace();
            return "{\"error\": \"Error occurred: " + e.getMessage() + "\"}";
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }

    }



//    public String insertUserDetails(INC_USERS userDetails) {
//        logger.info("Inside OracleService class insertUserDetails method start method");
//        Connection con = getConnection();
//        try {
//            // Check if the user already exists
//            String checkSql = "SELECT COUNT(*) FROM " + USERS_TABLE + " WHERE username = ?";
//            PreparedStatement checkStatement = con.prepareStatement(checkSql);
//            logger.info("Inside OracleService class insertUserDetails method PreparedStatement called");
//
//            checkStatement.setString(1, userDetails.getUsername());
//            ResultSet resultSet = checkStatement.executeQuery();
//            logger.info("Inside OracleService class insertUserDetails method executeQuery called");
//            resultSet.next();
//            int rowCount = resultSet.getInt(1);
//
//            String sql;
//            if (rowCount > 0) {
//                // User exists, perform update
//                sql = "UPDATE " + USERS_TABLE + " SET password = ?, mobile = ?, account = ? WHERE username = ?";
//            } else {
//                // User does not exist, perform insert
//                sql = "INSERT INTO " + USERS_TABLE + " (password,mobile,account, username ) VALUES (?, ?, ?, ?)";
//            }
//
//            PreparedStatement statement = con.prepareStatement(sql);
//            int parameterIndex = 1; // Start setting parameters from index 1
//
//            // For both insert and update operations, set password and mobile
//            statement.setString(parameterIndex++, userDetails.getMobile());
//
//            // Convert account list to JSON string
//            String accountJson = new ObjectMapper().writeValueAsString(userDetails.getAccount());
//
//            // Set account JSON string to the PreparedStatement
//            statement.setString(parameterIndex++, accountJson);
//
//            if (rowCount > 0) {
//                // For update, set username for WHERE clause
//                statement.setString(parameterIndex++, userDetails.getUsername());
//            } else {
//                // For insert, set username as well
//                statement.setString(parameterIndex++, userDetails.getUsername());
//            }
//
//            // Execute insert or update
//            int rowsAffected = statement.executeUpdate();
//            if (rowsAffected > 0) {
//                return "User details inserted/updated successfully";
//            } else {
//                return "Failed to insert/update user details";
//            }
//        } catch (SQLException | JsonProcessingException e) {
//            logger.error("Inside OracleService class insertUserDetails method exception message "+e.getMessage());
//            logger.error("Inside OracleService class insertUserDetails method exception "+e);
//            e.printStackTrace();
//            return "Error occurred while inserting/updating user details";
//        }
//        finally {
//            // Close connection in a finally block
//            closeConnection(con);
//        }
//    }


    public String insertOrUpdateUserDetails(INC_USERS userDetails) {
        Connection con = getConnection();
        try {
            // Check if the user exists
            String checkUserSql = "SELECT user_id FROM INC_USERS WHERE ntnet = ?";
            PreparedStatement checkUserStatement = con.prepareStatement(checkUserSql);
            checkUserStatement.setString(1, userDetails.getNtnet());
            ResultSet rs = checkUserStatement.executeQuery();

            int userId;
            if (rs.next()) {
                // User exists, get the user_id
                userId = rs.getInt("user_id");

                // Update user details
                String updateUserSql = "UPDATE INC_USERS SET username = ?, mobile = ? WHERE user_id = ?";
                PreparedStatement updateUserStatement = con.prepareStatement(updateUserSql);
                updateUserStatement.setString(1, userDetails.getUsername());
                updateUserStatement.setString(2, userDetails.getMobile());
                updateUserStatement.setInt(3, userId);
                updateUserStatement.executeUpdate();

                // Delete existing regions and accounts for the user
                String deleteRegionsSql = "DELETE FROM userregions WHERE userid = ?";
                PreparedStatement deleteRegionsStatement = con.prepareStatement(deleteRegionsSql);
                deleteRegionsStatement.setInt(1, userId);
                deleteRegionsStatement.executeUpdate();

                String deleteAccountsSql = "DELETE FROM useraccounts WHERE userid = ?";
                PreparedStatement deleteAccountsStatement = con.prepareStatement(deleteAccountsSql);
                deleteAccountsStatement.setInt(1, userId);
                deleteAccountsStatement.executeUpdate();
            } else {
                // User does not exist, insert new user
                String maxUserIdSql = "SELECT NVL(MAX(user_id), 0) + 1 FROM INC_USERS";
                PreparedStatement maxUserIdStatement = con.prepareStatement(maxUserIdSql);
                ResultSet maxRs = maxUserIdStatement.executeQuery();
                userId = 0;
                if (maxRs.next()) {
                    userId = maxRs.getInt(1);
                }

                // Ensure the new user_id is unique
                String checkUniqueUserIdSql = "SELECT COUNT(*) FROM INC_USERS WHERE user_id = ?";
                PreparedStatement checkUniqueUserIdStatement = con.prepareStatement(checkUniqueUserIdSql);
                checkUniqueUserIdStatement.setInt(1, userId);
                ResultSet uniqueRs = checkUniqueUserIdStatement.executeQuery();
                if (uniqueRs.next() && uniqueRs.getInt(1) > 0) {
                    // If user_id already exists, increment and check again
                    userId++;
                    checkUniqueUserIdStatement.setInt(1, userId);
                    uniqueRs = checkUniqueUserIdStatement.executeQuery();
                    while (uniqueRs.next() && uniqueRs.getInt(1) > 0) {
                        userId++;
                        checkUniqueUserIdStatement.setInt(1, userId);
                        uniqueRs = checkUniqueUserIdStatement.executeQuery();
                    }
                }

                String insertUserSql = "INSERT INTO INC_USERS (user_id, username, ntnet, mobile) VALUES (?, ?, ?, ?)";
                PreparedStatement insertUserStatement = con.prepareStatement(insertUserSql);
                insertUserStatement.setInt(1, userId);
                insertUserStatement.setString(2, userDetails.getUsername());
                insertUserStatement.setString(3, userDetails.getNtnet());
                insertUserStatement.setString(4, userDetails.getMobile());
                insertUserStatement.executeUpdate();
            }

            // Insert regions
            String insertRegionSql = "INSERT INTO userregions (userid, regionId) VALUES (?, ?)";
            PreparedStatement insertRegionStatement = con.prepareStatement(insertRegionSql);
            for (int regionId : userDetails.getRegionIds()) {
                insertRegionStatement.setInt(1, userId);
                insertRegionStatement.setInt(2, regionId);
                insertRegionStatement.addBatch();
            }
            insertRegionStatement.executeBatch();

            // Insert accounts
            String insertAccountSql = "INSERT INTO useraccounts (userid, accountId) VALUES (?, ?)";
            PreparedStatement insertAccountStatement = con.prepareStatement(insertAccountSql);
            for (int accountId : userDetails.getAccountIds()) {
                insertAccountStatement.setInt(1, userId);
                insertAccountStatement.setInt(2, accountId);
                insertAccountStatement.addBatch();
            }
            insertAccountStatement.executeBatch();

            return "User details inserted/updated successfully";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred while inserting/updating user details";
        } finally {
            closeConnection(con);
        }
    }





    public boolean authenticateUser(String username, String password) {

        logger.info("Inside OracleService class authenticateUser method start method");
        // Initialize connection
        Connection con = getConnection();
        try  {
            // Prepare SQL statement
            // String sql = "SELECT password FROM users WHERE username = ?";
            String sql = "SELECT password_h FROM INC_USERS WHERE username = ?";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                logger.info("Inside OracleService class authenticateUser method PreparedStatement called");
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    logger.info("Inside OracleService class authenticateUser method executeQuery called");
                    // Check if the user exists
                    if (resultSet.next()) {
                        String storedPassword = resultSet.getString("password_h");
                        // Compare the stored password with the provided password
                        return password.equals(storedPassword);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Inside OracleService class authenticateUser method exception message "+e.getMessage());
            logger.error("Inside OracleService class authenticateUser method exception "+e);
            e.printStackTrace();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }
        return false;
    }


    public INC_USERS getUserByUsername(String username) {
        logger.info("Inside OracleService class getUserByUsername method start method");
        //  Users user = null;
        INC_USERS user = null;
        Connection con = getConnection();
        try  {
            // Prepare SQL statement
            //  String sql = "SELECT username, password, mobile FROM users WHERE username = ?";
            String sql = "SELECT user_ID,username , ntNet ,mobile FROM INC_USERS WHERE  ntNet= ?";
            try (PreparedStatement statement = con.prepareStatement(sql)) {
                logger.info("Inside OracleService class insertUserDetails method getUserByUsername called");
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    logger.info("Inside OracleService class getUserByUsername method executeQuery called");
                    // Check if the user exists
                    if (resultSet.next()) {
                        user = new INC_USERS();
                        user.setUser_id(resultSet.getInt("user_id"));
                        user.setUsername(resultSet.getString("username"));
                        user.setMobile(resultSet.getString("mobile"));

                        // Fetch accounts as a list

                        // You can map other fields as needed
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Inside OracleService class getUserByUsername method exception message "+e.getMessage());
            logger.error("Inside OracleService class getUserByUsername method exception "+e);
            e.printStackTrace();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }
        return user;
    }


    public String insertIncDetails(IncDetails incDetails) {
        logger.info("Inside OracleService class insertIncDetails method start method");
        Connection con = getConnection();
        try {
            String preUpdatesJson = new ObjectMapper().writeValueAsString(incDetails.getPreUpdates());

            if (incDetails.getIsEditing()) {
                // Delete the old record
                logger.info("Inside OracleService class insertIncDetails method IS editing enabled");
                String deleteSql = "DELETE FROM " + NOTIFICATIONS_TABLE + " WHERE \"incNumber\" = ?";
                try (PreparedStatement deleteStatement = con.prepareStatement(deleteSql)) {
                    deleteStatement.setString(1, incDetails.getIncNumber());
                    deleteStatement.executeUpdate();
                }
            }

            // Insert or update the record
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
                    "dest.\"status\" = ?, " +
                    "dest.\"problemStatement\" = ?, " +
                    "dest.\"region\" = ?, " +
                    "dest.\"impactStartDate\" = ?, " +
                    "dest.\"impactStartTime\" = ?, " +
                    "dest.\"impactEndDate\" = ?, " +
                    "dest.\"impactEndTime\" = ?, " +
                    "dest.\"minutesOfOutage\" = ?, " +
                    "dest.\"rootCause\" = ?, " +
                    "dest.\"affectedServices\" = ?, " +
                    "dest.\"problemIdentified\" = ?, " +
                    "dest.\"escalatedLevel\" = ?, " +
                    "dest.\"expertsContacted\" = ?, " +
                    "dest.\"updateFrequency\" = ?, " +
                    "dest.\"checkedWithOtherAccounts\" = ?, " +
                    "dest.\"coreExpertsInvolved\" = ?, " +
                    "dest.\"etaForResolution\" = ? " +
                    "WHEN NOT MATCHED THEN " +
                    "INSERT (\"incNumber\", \"date\", \"manager\", \"workAround\", \"businessImpact\", \"bridgeDetails\", \"priority\", \"issueOwnedBy\", \"nextUpdate\", \"preUpdates\", \"time\", \"account\", \"status\", \"problemStatement\", \"region\", \"impactStartDate\", \"impactStartTime\", \"impactEndDate\", \"impactEndTime\", \"minutesOfOutage\", \"rootCause\", \"affectedServices\", \"problemIdentified\", \"escalatedLevel\", \"expertsContacted\", \"updateFrequency\", \"checkedWithOtherAccounts\", \"coreExpertsInvolved\", \"etaForResolution\") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = con.prepareStatement(sql)) {
                // Set parameters for MERGE
                statement.setString(1, incDetails.getIsEditing() ? incDetails.getNewIncNumber() : incDetails.getIncNumber());
                statement.setDate(2, incDetails.getDate());
                statement.setString(3, incDetails.getManager());
                statement.setString(4, incDetails.getWorkAround());
                statement.setString(5, incDetails.getBusinessImpact());
                statement.setString(6, incDetails.getBridgeDetails());
                statement.setString(7, incDetails.getPriority());
                statement.setString(8, incDetails.getIssueOwnedBy());
                statement.setString(9, incDetails.getNextUpdate());
                statement.setString(10, preUpdatesJson);
                statement.setString(11, incDetails.getTime());
                statement.setString(12, incDetails.getAccount());
                statement.setString(13, incDetails.getStatus());
                statement.setString(14, incDetails.getProblemStatement());
                statement.setString(15, incDetails.getRegion());
                statement.setDate(16, incDetails.getImpactStartDate());
                statement.setString(17, incDetails.getImpactStartTime());
                statement.setDate(18, incDetails.getImpactEndDate());
                statement.setString(19, incDetails.getImpactEndTime());
                statement.setString(20, incDetails.getMinutesOfOutage());
                statement.setString(21, incDetails.getRootCause());
                statement.setString(22, incDetails.getAffectedServices());
                statement.setString(23, incDetails.getProblemIdentified());
                statement.setString(24, incDetails.getEscalatedLevel());
                statement.setString(25, incDetails.getExpertsContacted());
                statement.setString(26, incDetails.getUpdateFrequency());
                statement.setString(27, incDetails.getCheckedWithOtherAccounts());
                statement.setString(28, incDetails.getCoreExpertsInvolved());
                statement.setString(29, incDetails.getEtaForResolution());

                // Set parameters for INSERT
                statement.setString(30, incDetails.getIsEditing() ? incDetails.getNewIncNumber() : incDetails.getIncNumber());
                statement.setDate(31, incDetails.getDate());
                statement.setString(32, incDetails.getManager());
                statement.setString(33, incDetails.getWorkAround());
                statement.setString(34, incDetails.getBusinessImpact());
                statement.setString(35, incDetails.getBridgeDetails());
                statement.setString(36, incDetails.getPriority());
                statement.setString(37, incDetails.getIssueOwnedBy());
                statement.setString(38, incDetails.getNextUpdate());
                statement.setString(39, preUpdatesJson);
                statement.setString(40, incDetails.getTime());
                statement.setString(41, incDetails.getAccount());
                statement.setString(42, incDetails.getStatus());
                statement.setString(43, incDetails.getProblemStatement());
                statement.setString(44, incDetails.getRegion());
                statement.setDate(45, incDetails.getImpactStartDate());
                statement.setString(46, incDetails.getImpactStartTime());
                statement.setDate(47, incDetails.getImpactEndDate());
                statement.setString(48, incDetails.getImpactEndTime());
                statement.setString(49, incDetails.getMinutesOfOutage());
                statement.setString(50, incDetails.getRootCause());
                statement.setString(51, incDetails.getAffectedServices());
                statement.setString(52, incDetails.getProblemIdentified());
                statement.setString(53, incDetails.getEscalatedLevel());
                statement.setString(54, incDetails.getExpertsContacted());
                statement.setString(55, incDetails.getUpdateFrequency());
                statement.setString(56, incDetails.getCheckedWithOtherAccounts());
                statement.setString(57, incDetails.getCoreExpertsInvolved());
                statement.setString(58, incDetails.getEtaForResolution());

                logger.info("Inside OracleService class insertIncDetails method PreparedStatement called");
                int rowsAffected = statement.executeUpdate();
                logger.info("Inside OracleService class insertIncDetails method executeQuery called");
                if (rowsAffected > 0) {
                    return "Incident details inserted/updated successfully";
                } else {
                    return "Failed to insert/update incident details";
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class insertIncDetails method exception message " + e.getMessage());
            logger.error("Inside OracleService class insertIncDetails method exception ", e);
            return "Error occurred while inserting/updating incident details";
        }
    }







    public String getIncDetailsForManager(String manager) {
        logger.info("Inside OracleService class getIncDetailsForManager method start method");
        String queryString = "SELECT * FROM notifications WHERE \"manager\" = '" + manager + "' AND \"status\"= 'Open' ORDER BY \"date\" DESC, \"time\" DESC";

        Connection con = getConnection();

        // Execute the SQL query using JDBC
        try {
            Statement statement = con.createStatement();
            logger.info("Inside OracleService class getIncDetailsForManager method PreparedStatement called");
            ResultSet resultSet = statement.executeQuery(queryString);
            logger.info("Inside OracleService class getIncDetailsForManager method executeQuery called");

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
                rowObject.put("problemStatement", resultSet.getString("problemStatement"));

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

            return res;

        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getIncDetailsForManager method exception message "+e.getMessage());
            logger.error("Inside OracleService class getIncDetailsForManager method exception "+e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }


    public String getIncidentsForAccounts(List<String> accounts) {
        logger.info("Inside OracleService class getIncidentsForAccounts method start method");
        Connection con = getConnection();
        try {
            // Prepare the SQL query
            String queryString = "SELECT \"incNumber\", \"account\", \"priority\", \"date\" " +
                    "FROM notifications " +
                    "WHERE \"account\" IN (" +
                    accounts.stream().map(account -> "'" + account + "'").collect(Collectors.joining(",")) +
                    ") AND \"status\" = 'Open' " +
                    "ORDER BY \"date\" DESC, \"time\" DESC"; // Order by date and time in descending order
            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            logger.info("Inside OracleService class getIncidentsForAccounts method PreparedStatement called");

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getIncidentsForAccounts method executeQuery called");

            // Create an ObjectMapper to convert the result to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();

            // Iterate over the result set and build the JSON array
            while (resultSet.next()) {
                ObjectNode rowObject = objectMapper.createObjectNode();
                rowObject.put("incNumber", resultSet.getString("incNumber"));
                rowObject.put("account", resultSet.getString("account"));
                rowObject.put("priority", resultSet.getString("priority"));

                // Parse the date field
                LocalDate date = resultSet.getDate("date").toLocalDate();
                String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                rowObject.put("date", formattedDate);

                resultArray.add(rowObject);
            }

            // Convert the JSON array to a string
            String res = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);

            return res;

        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getIncidentsForAccounts method exception message " + e.getMessage());
            logger.error("Inside OracleService class getIncidentsForAccounts method exception " + e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }



    public String getInsDetailsWithIncId(String incNumber) {
        logger.info("Inside OracleService class getInsDetailsWithIncId method start method");
        Connection con = getConnection();
        try {
            // Prepare the SQL query
            String queryString = "SELECT * FROM notifications WHERE \"incNumber\" = ?";
            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            logger.info("Inside OracleService class getInsDetailsWithIncId method PreparedStatement called");
            preparedStatement.setString(1, incNumber);

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getInsDetailsWithIncId method executeQuery called");
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

                // Parse the date field
                LocalDate date = resultSet.getDate("date").toLocalDate();
                String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                rowObject.put("date", formattedDate);

                rowObject.put("time", resultSet.getString("time"));
                rowObject.put("issueOwnedBy", resultSet.getString("issueOwnedBy"));
                rowObject.put("manager", resultSet.getString("manager"));
                rowObject.put("problemStatement", resultSet.getString("problemStatement"));
                rowObject.put("region", resultSet.getString("region"));

                LocalDate impactStartDate = resultSet.getDate("impactStartDate").toLocalDate();
                String formattedImpactStartDate = impactStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                rowObject.put("impactStartDate", formattedImpactStartDate);

                rowObject.put("impactStartTime", resultSet.getString("impactStartTime"));
                if (resultSet.getDate("impactEndDate") != null) {
                    LocalDate impactEndDate = resultSet.getDate("impactEndDate").toLocalDate();
                    String formattedImpactEndDate = impactEndDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    rowObject.put("impactEndDate", formattedImpactEndDate);

                    rowObject.put("impactEndTime", resultSet.getString("impactEndTime"));
                    rowObject.put("minutesOfOutage", resultSet.getString("minutesOfOutage"));
                    rowObject.put("rootCause", resultSet.getString("rootCause"));
                }

                // New fields
                rowObject.put("affectedServices", resultSet.getString("affectedServices"));
                rowObject.put("problemIdentified", resultSet.getString("problemIdentified"));
                rowObject.put("escalatedLevel", resultSet.getString("escalatedLevel"));
                rowObject.put("expertsContacted", resultSet.getString("expertsContacted"));
                rowObject.put("updateFrequency", resultSet.getString("updateFrequency"));
                rowObject.put("checkedWithOtherAccounts", resultSet.getString("checkedWithOtherAccounts"));
                rowObject.put("coreExpertsInvolved", resultSet.getString("coreExpertsInvolved"));
                rowObject.put("etaForResolution", resultSet.getString("etaForResolution"));

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

            return res;

        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getInsDetailsWithIncId method exception message " + e.getMessage());
            logger.error("Inside OracleService class getInsDetailsWithIncId method exception " + e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }


    public Boolean doNTNETAuthentication(String username, String password) {
        logger.info("Inside OracleService class doNTNETAuthentication method start method");
        UniAddress uAddress = null;
        NtlmPasswordAuthentication auth = null;
        Boolean isValidUser = false;
        try {
            uAddress = UniAddress.getByName("10.19.212.200"); // Replace with your SSO server address
            logger.info("Inside OracleService class doNTNETAuthentication method uAddress "+ uAddress);
            auth = new NtlmPasswordAuthentication("NTNET", username, password); // Replace "NTNET" with your domain
            logger.info("Inside OracleService class doNTNETAuthentication method auth "+ auth);
            SmbSession.logon(uAddress, auth); // Perform NTLM authentication
            logger.info("Inside OracleService class doNTNETAuthentication method NTLM authentication Successfully");
            isValidUser = true; // Authentication successful
        } catch (UnknownHostException ex) {
            // Handle UnknownHostException (e.g., log or throw custom exception)
            logger.error("Inside OracleService class doNTNETAuthentication method exception message "+ex.getMessage());
            logger.error("Inside OracleService class doNTNETAuthentication method exception "+ex);
            ex.printStackTrace();
        } catch (SmbAuthException ex) {
            // Handle SmbAuthException (e.g., log or throw custom exception)
            logger.error("Inside OracleService class doNTNETAuthentication method exception message "+ex.getMessage());
            logger.error("Inside OracleService class doNTNETAuthentication method exception "+ex);
            ex.printStackTrace();
        } catch (SmbException ex) {
            // Handle SmbException (e.g., log or throw custom exception)
            logger.error("Inside OracleService class doNTNETAuthentication method exception message "+ex.getMessage());
            logger.error("Inside OracleService class doNTNETAuthentication method exception "+ex);
            ex.printStackTrace();
        }
        return isValidUser;
    }


    public String getManagerForAccount(String account) {
        logger.info("Inside OracleService class getManagerForAccount method start method");
        Connection con = getConnection();
        try {
            // Prepare the SQL query
            String queryString = "SELECT * FROM NotificationManager WHERE name = ?";
            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, account);
            logger.info("Inside OracleService class getManagerForAccount method PreparedStatement called");
            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getManagerForAccount method executeQuery called");
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

            return res;

        } catch (SQLException e) {
            logger.error("Inside OracleService class getManagerForAccount method exception message "+e.getMessage());
            logger.error("Inside OracleService class getManagerForAccount method exception "+e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }

    public String getAccountForUser(String userId) {
        logger.info("Inside OracleService class getAccountForUser method start");
        Connection con = getConnection();
        try {
            // Prepare the SQL query
            String queryString = "SELECT a.Account_ID, a.Account_Name " +
                    "FROM UserAccounts ua " +
                    "JOIN Accounts a ON ua.AccountID = a.Account_ID " +
                    "WHERE ua.UserID = ?";
            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, userId);
            logger.info("Inside OracleService class getAccountForUser method PreparedStatement called");

            // Execute the SQL query
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getAccountForUser method executeQuery called");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each row in the result set
                ObjectNode regionObject = objectMapper.createObjectNode();
                regionObject.put("Account_ID", resultSet.getInt("Account_ID"));
                regionObject.put("Account_Name", resultSet.getString("Account_Name"));
                resultArray.add(regionObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);

        } catch (SQLException e) {
            logger.error("Inside OracleService class getAccountForUser method exception message " + e.getMessage());
            logger.error("Inside OracleService class getAccountForUser method exception ", e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }

    public String getTimeForPriority( String priority) {
        logger.info("Inside OracleService class getTimeForPriority method start method");
        String queryString = "SELECT time FROM priority_notifications WHERE priority = ?";
        StringBuilder times = new StringBuilder();
        Connection con = getConnection();

        // Execute the SQL query

        try  {
            PreparedStatement preparedStatement = con.prepareStatement(queryString);

            preparedStatement.setString(1, priority);
            logger.info("Inside OracleService class getTimeForPriority method PreparedStatement called");
            try (ResultSet rs = preparedStatement.executeQuery()) {
                logger.info("Inside OracleService class getTimeForPriority method executeQuery called");
                while (rs.next()) {
                    if (times.length() > 0) {
                        times.append(", ");
                    }
                    times.append(rs.getInt("time"));
                }
            }

        } catch (SQLException e) {
            logger.error("Inside OracleService class getTimeForPriority method exception message "+e.getMessage());
            logger.error("Inside OracleService class getTimeForPriority method exception "+e);
            e.printStackTrace();
            return "Error retrieving times: " + e.getMessage();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }

        return times.toString();
    }

    public String getIncidentsByPriorityAndAccount(String priority, String account) {
        logger.info("Inside OracleService class getIncidentsByPriorityAndAccount method start method");
        logger.debug("Inside OracleService class getIncidentsByPriorityAndAccount method start method");
        Connection con = getConnection();
        try {
            String queryString = "SELECT \"incNumber\" FROM notifications WHERE \"priority\" = ? AND \"account\" = ? AND \"status\" = 'Open'";

            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, priority);
            preparedStatement.setString(2, account);
            logger.info("Inside OracleService class getIncidentsByPriorityAndAccount method PreparedStatement called");
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getIncidentsByPriorityAndAccount method executeQuery called");
            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Add incident number to the resultArray
                resultArray.add(resultSet.getString("incNumber"));
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getIncidentsByPriorityAndAccount method exception message "+e.getMessage());
            logger.error("Inside OracleService class getIncidentsByPriorityAndAccount method exception "+e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        }
        finally {
            // Close connection in a finally block
            closeConnection(con);
        }

    }

    public String getUserForRegion(String userId) {
        logger.info("Inside OracleService class getUserForRegion method start method");
        logger.debug("Inside OracleService class getUserForRegion method start method");
        Connection con = getConnection();
        try {
            String queryString = "SELECT r.Region_ID , r.Region_Name " +
                    "FROM userregions ur " +
                    "JOIN Regions r ON ur.RegionID = r.Region_ID " +
                    "WHERE ur.UserID = ?";

            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, userId);
            logger.info("Inside OracleService class getUserForRegion method PreparedStatement called");
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getUserForRegion method executeQuery called");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each row in the result set
                ObjectNode regionObject = objectMapper.createObjectNode();
                regionObject.put("Region_ID", resultSet.getInt("Region_ID"));
                regionObject.put("Region_Name", resultSet.getString("Region_Name"));
                resultArray.add(regionObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getUserForRegion method exception message " + e.getMessage());
            logger.error("Inside OracleService class getUserForRegion method exception " + e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }


    public String getUserAccounts(String userId, int regionId) {
        logger.info("Inside OracleService class getUserAccounts method start");
        logger.debug("User Id: " + userId + ", Region Id: " + regionId);
        Connection con = getConnection();
        try {
            String queryString = "SELECT a.Account_ID, a.Account_Name " +
                    "FROM UserAccounts ua " +
                    "JOIN Accounts a ON ua.AccountID = a.Account_ID " +
                    "WHERE ua.UserID = ? AND a.Region_ID = ?";

            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, userId);
            preparedStatement.setInt(2, regionId);
            logger.info("PreparedStatement created and parameters set");

            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Query executed");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each row in the result set
                ObjectNode accountObject = objectMapper.createObjectNode();
                accountObject.put("Account_ID", resultSet.getInt("Account_ID"));
                accountObject.put("Account_Name", resultSet.getString("Account_Name"));
                resultArray.add(accountObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Exception: " + e.getMessage(), e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }



    public String getManagerForAccounts(String accountName) {
        logger.info("Inside OracleService class getManagerForAccounts method start method");
        logger.debug("Inside OracleService class getManagerForAccounts method start method");
        Connection con = getConnection();
        try {
            String queryString = "SELECT IU.USERNAME AS Manager_Name " +
                    "FROM useraccounts UA " +
                    "JOIN Accounts A ON UA.AccountID = A.Account_ID " +
                    "JOIN INC_USERS IU ON UA.userid = IU.USER_ID " +
                    "WHERE A.Account_Name = ? " +
                    "ORDER BY UA.AccountID, IU.USERNAME";

            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            preparedStatement.setString(1, accountName);
            logger.info("Inside OracleService class getManagerForAccounts method PreparedStatement called");
            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Inside OracleService class getManagerForAccounts method executeQuery called");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each row in the result set
                ObjectNode managerObject = objectMapper.createObjectNode();
                managerObject.put("Manager_Name", resultSet.getString("Manager_Name"));
                resultArray.add(managerObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Inside OracleService class getManagerForAccounts method exception message " + e.getMessage());
            logger.error("Inside OracleService class getManagerForAccounts method exception ", e);
            e.printStackTrace();
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }



    public String getAllRegions() {
        logger.info("Inside OracleService class getAllRegions method start");
        Connection con = getConnection();
        try {
            String queryString = "SELECT region_id, region_name FROM regions";

            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            logger.info("PreparedStatement created");

            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Query executed");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each row in the result set
                ObjectNode regionObject = objectMapper.createObjectNode();
                regionObject.put("region_id", resultSet.getInt("region_id"));
                regionObject.put("region_name", resultSet.getString("region_name"));

                // Add the JSON object to the result array
                resultArray.add(regionObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Exception: " + e.getMessage(), e);
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }



    public String getAccountsForRegions(String[] regionId) {
        logger.info("Inside OracleService class getAccountsForRegions method");
        Connection con = getConnection();
        if (con == null) {
            logger.error("Database connection could not be established.");
            return "Database connection error";
        }

        try {
            // Construct a SQL query to fetch both account_id and account_name
            StringBuilder queryBuilder = new StringBuilder("SELECT a.account_id, a.account_name " +
                    "FROM accounts a " +
                    "JOIN regions r ON a.region_id = r.region_id " +
                    "WHERE r.region_id IN (");

            // Append placeholders for the number of region IDs
            for (int i = 0; i < regionId.length; i++) {
                queryBuilder.append("?");
                if (i < regionId.length - 1) {
                    queryBuilder.append(",");
                }
            }
            queryBuilder.append(")");

            String queryString = queryBuilder.toString();
            PreparedStatement preparedStatement = con.prepareStatement(queryString);
            logger.info("PreparedStatement created");

            // Set the region IDs in the PreparedStatement
            for (int i = 0; i < regionId.length; i++) {
                preparedStatement.setString(i + 1, regionId[i]);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            logger.info("Query executed");

            // Process the result set and build the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode resultArray = objectMapper.createArrayNode();
            while (resultSet.next()) {
                // Create a JSON object for each account
                ObjectNode accountObject = objectMapper.createObjectNode();
                accountObject.put("account_id", resultSet.getInt("account_id"));
                accountObject.put("account_name", resultSet.getString("account_name"));

                // Add the JSON object to the result array
                resultArray.add(accountObject);
            }

            // Convert the resultArray to a JSON string
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultArray);
        } catch (SQLException | JsonProcessingException e) {
            logger.error("Exception: " + e.getMessage(), e);
            return "Error occurred: " + e.getMessage();
        } finally {
            // Close connection in a finally block
            closeConnection(con);
        }
    }



    // Implement other methods similarly

    // Remember to close connection when done
    public void closeConnection(Connection con) {
        try {
            if (con != null && !con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


