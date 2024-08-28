package com.techelevator.dao;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import com.techelevator.tenmo.dao.JdbcTransactionDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TransferType;
import com.techelevator.tenmo.model.TrasnferStatus;
import com.techelevator.tenmo.model.User;

public class JdbcTransactionDaoTests extends BaseDaoTests {



    private final Transaction TRANSACTION_1 = new Transaction(3101, TransferType.SEND(), TrasnferStatus.APPROVED(), 2002, 2003, new BigDecimal ("10.00"));
    // protected static final User USER_2 = new User(1002, "user2", "user2", "USER");
    // private static final User USER_3 = new User(1003, "user3", "user3", "USER");

    private JdbcTransactionDao transTest;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        transTest = new JdbcTransactionDao(jdbcTemplate);
    }

    @Test 
    public void proceedTransaction_successfully_changed_account_balance(){
        System.out.println(TRANSACTION_1);
        transTest.viewCurrentBalance(1001);
        transTest.proceedTransaction(TRANSACTION_1); 
        Assert.assertEquals(new BigDecimal ("990.00"), transTest.viewCurrentBalance(1001));
    }

    @Test
    public void getTransactionById_returns_correct_values(){
        // transTest.proceedTransaction(TRANSACTION_1);
        Transaction requestedTransaction = transTest.getTransactionById(3001);
        Assert.assertEquals(new BigDecimal("300.00"), requestedTransaction.getAmount());
    }

    @Test
    public void viewPendingRequests_returns_correct_number(){
        List<Transaction> pends = transTest.viewPendingRequests(1001);
        Assert.assertEquals(pends.size(), 2);
    }

    @Test
    public void approveRequest_success_change_state(){
        Transaction pendingTransaction = transTest.getTransactionById(3005);
        pendingTransaction.setTransfer_status_id(2);
        boolean success = transTest.approveRequest(pendingTransaction);

        Assert.assertTrue(success);
        Assert.assertEquals(2, transTest.getTransactionById(3005).getTransfer_status_id());

        // Assert.assertEquals(new BigDecimal(1005.00), transTest.viewCurrentBalance(1001));
    }

    @Test (expected = NullPointerException.class)
    public void proceedTransaction_with_null_amount() {
        Transaction incorrectTransaction = TRANSACTION_1; 
        incorrectTransaction.setAmount(null);
        boolean success = transTest.proceedTransaction(incorrectTransaction); 

        Assert.assertFalse(success);
    }

    @Test (expected = DaoException.class)
    public void proceedTransaction_with_negative_amount() {
        Transaction incorrectTransaction = TRANSACTION_1; 
        incorrectTransaction.setAmount(new BigDecimal(-100.00));
        boolean success = transTest.proceedTransaction(incorrectTransaction); 

        Assert.assertFalse(success);
    }

}
