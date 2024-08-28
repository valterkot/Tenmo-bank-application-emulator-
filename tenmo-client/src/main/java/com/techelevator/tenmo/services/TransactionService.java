package com.techelevator.tenmo.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;
import com.techelevator.util.BasicLogger;

import java.math.BigDecimal;


public class TransactionService {

    private final String API_BASE_URL = "http://localhost:8080/";
    private RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;
    private AuthenticatedUser currentUser = null;

    private final String TRANSFER_DETAILS = String.format("%-40s\n", "Transfer Details:"); 


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
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
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

    public Transaction approveTransaction(Transaction transaction){
        HttpEntity<Transaction> entity = makeTransactionEntity(transaction);
        try {
            restTemplate.put(API_BASE_URL+"transactions/"+transaction.getTransfer_id(), entity);
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return null;
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


    public Transaction[] viewTransferHistory() {
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

    public User[] getUserList() {
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL+"allUsers", HttpMethod.GET, makeAuthEntity(), User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return users;
    }

    public int getAccountId(int user_id) {
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

    public void makeTransaction(Transaction transaction) {
        HttpEntity<Transaction> entity = makeTransactionEntity(transaction);
        Transaction newTransaction = null;
        try {
            newTransaction = restTemplate.postForObject(API_BASE_URL+"transactions", entity, Transaction.class);
            if (newTransaction != null) {
                System.out.println("\n You successfully transfered money! \n");
                System.out.println(TRANSFER_DETAILS);
                newTransaction.showDetails();
            }
        } catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
    }


    //duplicating viewCurrentBalance, we will cut off this 

    /* 
    public BigDecimal getUserBalance() {
        BigDecimal balance = null;
        try {
            // String currentUserId;
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL+ currentUser.getUser().getId() +"/balance", HttpMethod.GET, makeAuthEntity(), BigDecimal.class);
            balance = response.getBody();
        }
        catch (RestClientResponseException | ResourceAccessException e) {
            BasicLogger.log(e.getMessage());
        }
        return balance;
    }
    */
}
