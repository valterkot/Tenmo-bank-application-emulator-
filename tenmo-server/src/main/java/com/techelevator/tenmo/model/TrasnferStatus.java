package com.techelevator.tenmo.model;

public class TrasnferStatus {

    private static final int PENDING = 1; 
    private static final int APPROVED = 2; 
    private static final int REJECTED = 3; 

    
    public static int PENDING(){
        return PENDING;
    }

    public static int APPROVED(){
        return APPROVED; 
    }

    public static int REJECTED(){
        return REJECTED;
    }

}
