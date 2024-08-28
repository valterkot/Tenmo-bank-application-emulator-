package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;

import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.User;

public interface TransactionDao {

    List<User> getAllUsers();

    BigDecimal viewCurrentBalance(int user_id);

    BigDecimal viewAccountBalance(int account_id);

    int getAccountId (int user_id);

    int createTransaction(Transaction transaction);

    List<Transaction> viewTransferHistory(int user_id);

    List<Transaction> viewPendingRequests(int user_id);

    boolean approveRequest(Transaction transaction);

    boolean proceedTransaction(Transaction transaction);

    Transaction getTransactionById(int transfer_id);

    int getUserIdByName(String name);
}
