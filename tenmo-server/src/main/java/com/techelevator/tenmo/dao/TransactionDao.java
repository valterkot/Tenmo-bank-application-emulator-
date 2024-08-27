package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;

public interface TransactionDao {

    BigDecimal viewCurrentBalance(int user_id);

    List<Transaction> viewTransferHistory(int user_id);

    boolean proceedTransaction(Transaction transaction);

    boolean requestMoney(Transaction transaction); 

    List<Transaction> viewPendingRequests(int user_id);

    boolean approveRequest(Transaction transaction);

    List<User> getAllUsers();

    int getAccountId (int user_id);

    Transaction getTransactionById(int transfer_id);

    int createTransaction(Transaction transaction);

}
