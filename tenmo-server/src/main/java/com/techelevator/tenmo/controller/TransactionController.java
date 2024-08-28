package com.techelevator.tenmo.controller;

import javax.validation.Valid;

import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.LoginResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.JdbcTransactionDao;
import com.techelevator.tenmo.dao.TransactionDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.LoginDto;
import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TransferType;
import com.techelevator.tenmo.model.TrasnferStatus;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;


/**
 * Controller to work with transactions.
 */
@RestController
@PreAuthorize(value = "isAuthenticated()")
public class TransactionController {

    private TransactionDao transactionDao;

    public TransactionController(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
    }

    // VIEW CURRENT BALANCE... 
    @RequestMapping(path = "/{user_id}/balance", method = RequestMethod.GET)
    public BigDecimal viewCurrentBalance (@PathVariable int user_id, Principal principal){
        int authUser = transactionDao.getUserIdByName(principal.getName());
        return transactionDao.viewCurrentBalance(authUser);
    } 

    // GET LIST WITH ALL REGISTERED USERS
    @RequestMapping(path = "/allUsers", method = RequestMethod.GET)
    public List<User> getAllUsers(){
        return transactionDao.getAllUsers();
    }

    // GET ACCOUNT_ID BY USER_ID
    @RequestMapping(path =  "/{user_id}/accounts", method = RequestMethod.GET)
    public int getAccountId(@PathVariable int user_id){
        return transactionDao.getAccountId(user_id);
    }

    // PROCEED NEW TRANSACTION  
    @RequestMapping(path =  "/transactions", method = RequestMethod.POST)
    public Transaction newTransaction(@RequestBody Transaction transaction){
        if (transaction.getTransfer_type_id() == TransferType.SEND()){
            if (transactionDao.proceedTransaction(transaction)){
                int new_trans_id = transactionDao.createTransaction(transaction);
                return transactionDao.getTransactionById(new_trans_id);
            }
        }
        if (transaction.getTransfer_type_id() == TransferType.REQUEST()){
            int new_trans_id = transactionDao.createTransaction(transaction);
            return transactionDao.getTransactionById(new_trans_id);
        }
        else {
            return null;
        }
    }

    // RETURN TRANSACTIONS BY USER_ID
    @RequestMapping(path = "/{user_id}/transactions", method = RequestMethod.GET)
    public List<Transaction> viewTransactions(@PathVariable int user_id){
        return transactionDao.viewTransferHistory(user_id);
    }

    @RequestMapping(path = "/{user_id}/pending", method = RequestMethod.GET)
    public List<Transaction> viewPendingRequests(@PathVariable int user_id){
        return transactionDao.viewPendingRequests(user_id);
    }
    
    @RequestMapping(path =  "/transactions/{transfer_id}", method = RequestMethod.PUT)
    public Transaction changeStatus(@PathVariable int transfer_id, @RequestBody Transaction transaction){
        if (transaction.getTransfer_status_id() != TrasnferStatus.PENDING()){
            if (transactionDao.approveRequest(transaction)){
                transactionDao.proceedTransaction(transaction);
            }
            return transactionDao.getTransactionById(transfer_id);
        }
        else {
            return null;
        }
    }


}
