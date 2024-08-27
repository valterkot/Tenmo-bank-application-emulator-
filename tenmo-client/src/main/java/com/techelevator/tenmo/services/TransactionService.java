package com.techelevator.tenmo.services;

import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TransferType;
import com.techelevator.tenmo.model.TrasnferStatus;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;

import java.math.BigDecimal;


public class TransactionService {

    private final String API_BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;
    private AuthenticatedUser currentUser = null;

    public TransactionService(AuthenticatedUser currentUser){
        this.currentUser = currentUser; 
        setAuthToken(currentUser.getToken());
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public BigDecimal viewCurrentBalance(){
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL+currentUser.getUser().getId()+"/balance", HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        }        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;

    }

    public User[] getAvailableUsers(){
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL+"allUsers", HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public Transaction[] getPendingRequests () {
        Transaction[] pendingTransactions = null; 
        try {
            ResponseEntity<Transaction[]> response = restTemplate.exchange(API_BASE_URL+currentUser.getUser().getId()+"/pending", HttpMethod.GET, makeAuthEntity(), Transaction[].class);
            pendingTransactions = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return pendingTransactions;
    } 

     


    public Transaction[] getTransferHistory() {
        Transaction[] transHistory = null; 
        try {
            ResponseEntity<Transaction[]> response = restTemplate.exchange(API_BASE_URL+currentUser.getUser().getId()+"/transactions", HttpMethod.GET, makeAuthEntity(), Transaction[].class);
            transHistory = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transHistory;
    }

    public Transaction getTransactionById(int trans_id){
        Transaction transaction = new Transaction(); 
        try {
            ResponseEntity<Transaction> response = restTemplate.exchange(API_BASE_URL+"transactions/" + trans_id, HttpMethod.GET, makeAuthEntity(), Transaction.class);
            transaction = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return transaction;
    }

    public boolean makeTransfer(Transaction transaction){
        HttpEntity<Transaction> entity = makeTransactionEntity(transaction);
        boolean success = false;
        try {
            restTemplate.put(API_BASE_URL+"transactions", entity);
            success = true;
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return success;
    }

    public Transaction approveTransaction(Transaction transaction){
        HttpEntity<Transaction> entity = makeTransactionEntity(transaction);
        try {
            restTemplate.put(API_BASE_URL+"transactions/"+transaction.getTransfer_id(), entity);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return null;
    }

    public Integer getAccountId(int user_id){
        Integer account_id = null;
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL+user_id+"/accounts", HttpMethod.GET, makeAuthEntity(), Integer.class);
            if (response.getBody() != null) {
                account_id = response.getBody();
            }
        }        
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return account_id;
    }

    private HttpEntity<Transaction> makeTransactionEntity(Transaction transaction){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transaction, headers);
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(headers);
    }


}
