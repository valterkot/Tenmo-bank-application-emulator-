package com.techelevator.tenmo.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;


import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TrasnferStatus;
import com.techelevator.tenmo.model.User;

@Component
public class JdbcTransactionDao implements TransactionDao{

    private final JdbcTemplate jdbcTemplate;

    private final String JOINT_SQL_TRANSACTIONS = "SELECT transfer_id, transfer_status_id, transfer_type_id, account_from, " +
                            "user_from.username as username_from, account_to, user_to.username as username_to, amount " +
                            "FROM transfer JOIN account as acc_from ON account_from = acc_from.account_id " +
                            "JOIN tenmo_user as user_from ON acc_from.user_id = user_from.user_id "+
                            "JOIN account as acc_to ON account_to = acc_to.account_id " +
                            "JOIN tenmo_user as user_to ON acc_to.user_id = user_to.user_id ";

    public JdbcTransactionDao(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

    @Override
    public List<User> getAllUsers(){
        List<User> userList = new ArrayList<>();
        String sql = "SELECT user_id, username FROM tenmo_user;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql); 
            while (results.next()) {
				User userResult = mapRowToUser(results);
				userList.add(userResult);
			}

			return userList;
		}
		catch (CannotGetJdbcConnectionException e) {
			throw new DaoException("Unable to connect to server or database", e);
		}
    }

    @Override
    public BigDecimal viewCurrentBalance(int user_id) {
        // TODO Auto-generated method stub
        BigDecimal currentBalance = null;
        String sql = "SELECT balance FROM account WHERE user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
            while (results.next()) {
                currentBalance = results.getBigDecimal("balance");
                
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return currentBalance;
    }

    @Override
    public int getAccountId (int user_id) {
        int account_id = 0;
        String sql = "SELECT account_id FROM account WHERE user_id = ?;";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, user_id);
            while (results.next()) {
                account_id = results.getInt("account_id");
                
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return account_id;
    }

    @Override
    public List<Transaction> viewTransferHistory(int user_id) {
        // TODO Auto-generated method stub
        List<Transaction> transHistory = new ArrayList<>();
        String jointSql = JOINT_SQL_TRANSACTIONS + 
                            "WHERE acc_to.user_id = ? or acc_from.user_id = ?";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(jointSql, user_id, user_id); 
            while (results.next()) {
				Transaction transResults = mapRowToTransaction(results);
				transHistory.add(transResults);
			}
			return transHistory;
		}
		catch (CannotGetJdbcConnectionException e) {
			throw new DaoException("Unable to connect to server or database", e);
		}
    }

    @Override
    public boolean proceedTransaction (Transaction transaction) {
        BigDecimal amount = transaction.getAmount();
        int account_from = transaction.getAccount_from();
        int account_to = transaction.getAccount_to();
        boolean success = false;

        String decreaseBalanceSql = "UPDATE account SET balance = balance - ? WHERE account_id = ?;";
        String increaseBalanceSql = "UPDATE account SET balance = balance + ? WHERE account_id = ?;";
        try {
            jdbcTemplate.update(decreaseBalanceSql, amount, account_from);
            jdbcTemplate.update(increaseBalanceSql, amount, account_to);
            success = true; 
            }
        catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return success;
    }

    public int createTransaction(Transaction transaction){
        int transfer_type_id = transaction.getTransfer_type_id();
        int transfer_status_id = transaction.getTransfer_status_id();
        BigDecimal amount = transaction.getAmount();
        int account_from = transaction.getAccount_from();
        int account_to = transaction.getAccount_to();

        String sql = "INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?) RETURNING transfer_id;";
        try{
            int transfer_id = jdbcTemplate.queryForObject(sql, int.class, transfer_type_id, transfer_status_id, account_from, account_to, amount);           
            return transfer_id;
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
    } 

    @Override
    public Transaction getTransactionById (int transfer_id) {
        Transaction transaction = new Transaction(); 
        String sql = JOINT_SQL_TRANSACTIONS + " WHERE transfer_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, transfer_id); 
            if (results.next()) {
                transaction	= mapRowToTransaction(results);
            }
		}
		catch (CannotGetJdbcConnectionException e) {
			throw new DaoException("Unable to connect to server or database", e);
		}
        return transaction;
    }

    @Override
    public boolean requestMoney(Transaction transaction) {
        // Tony: 
        createTransaction(transaction);
        return true;
    }

    @Override
    public List<Transaction> viewPendingRequests(int user_id) {
        // TODO Auto-generated method stub
        List<Transaction> pendingRequests = new ArrayList<>();
        String jointSql = JOINT_SQL_TRANSACTIONS + 
                            " WHERE transfer_status_id = 1 AND (acc_to.user_id = ? or acc_from.user_id = ?);";

        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(jointSql, user_id, user_id); 
            while (results.next()) {
				Transaction transResults = mapRowToTransaction(results);
				pendingRequests.add(transResults);
			}
			return pendingRequests;
		}
		catch (CannotGetJdbcConnectionException e) {
			throw new DaoException("Unable to connect to server or database", e);
		}
    }

    @Override
    public boolean approveRequest(Transaction transaction) {
        // TODO Auto-generated method stub
        boolean success = false; 
        int transfer_id = transaction.getTransfer_id(); 
        int updated_status = transaction.getTransfer_status_id();
        String sql = "UPDATE transfer SET transfer_status_id = ? WHERE transfer_id = ?;";
        try {
            jdbcTemplate.update(sql, updated_status, transfer_id);
            success = true;
            }
        catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        }
        return success;
    }

    private Transaction mapRowToTransaction(SqlRowSet rs) {
        Transaction transaction = new Transaction();
        transaction.setTransfer_id(rs.getInt("transfer_id"));
        transaction.setTransfer_type_id(rs.getInt("transfer_type_id"));
        transaction.setTransfer_status_id(rs.getInt("transfer_status_id"));
        transaction.setAccount_from(rs.getInt("account_from"));
        transaction.setAccount_to(rs.getInt("account_to"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setUsername_from(rs.getString("username_from"));
        transaction.setUsername_to(rs.getString("username_to"));
        return transaction;
    }

    private User mapRowToUser(SqlRowSet rs) {
        User user = new User(); 
        user.setId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        return user;
    }
}