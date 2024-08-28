package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transaction {
    
    private int transfer_id;
    private int transfer_type_id; 
    private int transfer_status_id;
    private int account_from; 
    private int account_to; 
    private BigDecimal amount;

    //adding new properties to represent account's holder
    private int user_id_from;
    private int user_id_to; 
    private String username_from;
    private String username_to; 
        
    public int getUser_id_from() {
        return user_id_from;
    }

    public void setUser_id_from(int user_id_from) {
        this.user_id_from = user_id_from;
    }

    public int getUser_id_to() {
        return user_id_to;
    }

    public void setUser_id_to(int user_id_to) {
        this.user_id_to = user_id_to;
    }

    public String getUsername_from() {
        return username_from;
    }

    public void setUsername_from(String username_from) {
        this.username_from = username_from;
    }

    public String getUsername_to() {
        return username_to;
    }

    public void setUsername_to(String username_to) {
        this.username_to = username_to;
    }

    public Transaction(int transfer_id, int transfer_type_id, int transfer_status_id, int account_from, int account_to,
            BigDecimal amount) {
        this.transfer_id = transfer_id;
        this.transfer_type_id = transfer_type_id;
        this.transfer_status_id = transfer_status_id;
        this.account_from = account_from;
        this.account_to = account_to;
        this.amount = amount;
    }

    public Transaction(){
        
    }

    public int getTransfer_id() {
        return transfer_id;
    }
    public void setTransfer_id(int transfer_id) {
        this.transfer_id = transfer_id;
    }
    public int getTransfer_type_id() {
        return transfer_type_id;
    }
    public void setTransfer_type_id(int transfer_type_id) {
        this.transfer_type_id = transfer_type_id;
    }
    public int getTransfer_status_id() {
        return transfer_status_id;
    }
    public void setTransfer_status_id(int transfer_status_id) {
        this.transfer_status_id = transfer_status_id;
    }
    public int getAccount_from() {
        return account_from;
    }
    public void setAccount_from(int account_from) {
        this.account_from = account_from;
    }
    public int getAccount_to() {
        return account_to;
    }
    public void setAccount_to(int account_to) {
        this.account_to = account_to;
    }
    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString(){
        String output = String.format("%-10s\t"+"%-15s\t"+"%-15s\t"+"$ " + "%5s\n", transfer_id, username_from, username_to, amount.toString());
        return output;
    }

    public void showDetails(){
        StringBuffer output = new StringBuffer(); 
        output.append(String.format("%-6s\t"+"%-15s\t\n", "Transfer ID:", transfer_id));
        output.append(String.format("%-6s\t"+"%-15s\t\n", "From:", username_from));
        output.append(String.format("%-6s\t"+"%-15s\t\n", "To:", username_to));
        output.append(String.format("%-6s\t"+"%-15s\t\n", "Type:", TransferType.decode(transfer_type_id)));
        output.append(String.format("%-6s\t"+"%-15s\t\n", "Status:", TrasnferStatus.decode(transfer_status_id)));
        output.append(String.format("%-6s\t"+"$"+"%-15s\t\n", "Amount:", amount.toString()));

        System.out.println(output.toString());
    }
}
