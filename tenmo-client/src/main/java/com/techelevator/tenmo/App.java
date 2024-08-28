package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransactionService;

import java.math.BigDecimal;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private static final String USER_LIST_HEADER = String.format("%-10s\t%-15s\t%-15s\t%-15s\n", "User ID", "Username", "Balance", "Account Type");

    private final String TRANSFER_HISTORY_HEADER = String.format("%-10s\t%-15s\t%-15s\t%-5s\n", "Transfers_ID", "From", "To", "Amount" );
    private final String TRANSFER_DETAILS = String.format("%-40s\n", "Transfer Details:"); 
    private final String USER_MENU_HEADER = String.format("%-10s\t%-20s\n", "USER_ID", "USER_NAME" );

    private final ConsoleService consoleService = new ConsoleService();
    private final AuthenticationService authenticationService = new AuthenticationService(API_BASE_URL);

    private AuthenticatedUser currentUser;

    public static void main(String[] args) {
        App app = new App();
        app.run();
    }

    private void run() {
        consoleService.printGreeting();
        loginMenu();
        if (currentUser != null) {
            mainMenu();
        }
    }
    private void loginMenu() {
        int menuSelection = -1;
        while (menuSelection != 0 && currentUser == null) {
            consoleService.printLoginMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                handleRegister();
            } else if (menuSelection == 2) {
                handleLogin();
            } else if (menuSelection != 0) {
                System.out.println("Invalid Selection");
                consoleService.pause();
            }
        }
    }

    private void handleRegister() {
        System.out.println("Please register a new user account");
        UserCredentials credentials = consoleService.promptForCredentials();
        if (authenticationService.register(credentials)) {
            System.out.println("Registration successful. You can now login.");
        } else {
            consoleService.printErrorMessage();
        }
    }

    private void handleLogin() {
        UserCredentials credentials = consoleService.promptForCredentials();
        currentUser = authenticationService.login(credentials);
        if (currentUser == null) {
            consoleService.printErrorMessage();
        }
    }

    private void mainMenu() {
        int menuSelection = -1;
        while (menuSelection != 0) {
            consoleService.printMainMenu();
            menuSelection = consoleService.promptForMenuSelection("Please choose an option: ");
            if (menuSelection == 1) {
                viewCurrentBalance();
            } else if (menuSelection == 2) {
                viewTransferHistory();
            } else if (menuSelection == 3) {
                viewPendingRequests();
            } else if (menuSelection == 4) {
                sendBucks();
            } else if (menuSelection == 5) {
                requestBucks();
            } else if (menuSelection == 0) {
                continue;
            } else {
                System.out.println("Invalid Selection");
            }
            consoleService.pause();
        }
    }

	private void viewCurrentBalance() {
        TransactionService transactionService = new TransactionService(currentUser); 
        System.out.println(currentUser.getUser().getUsername()+"\'s available balance is: $" + transactionService.viewCurrentBalance());

	}

	private void viewTransferHistory() {
		TransactionService transactionService = new TransactionService(currentUser);
        Transaction[] transHistory = transactionService.viewTransferHistory();
        if (transHistory != null) {
            System.out.println(printHeading(TRANSFER_HISTORY_HEADER));
            for (Transaction transaction : transHistory){
                System.out.print(transaction);
            }

            int pending_trans_id = -1; 
            while (pending_trans_id != 0) {
                pending_trans_id = consoleService.promptForInt("\nPlease enter transfer ID to view details (0 to cancel): ");
                for (Transaction transaction : transactionService.viewTransferHistory()){
                    if (pending_trans_id == transaction.getTransfer_id()){
                        pending_trans_id = 0;
                        System.out.println(printHeading(TRANSFER_HISTORY_HEADER));
                        transaction.showDetails();
                    }
                    }
                }
            }
        else {
            System.out.println("No History Available");
        }
		
	}

	private void viewPendingRequests() {
        TransactionService transactionService = new TransactionService(currentUser); 
        Transaction[] pendingTransactions = transactionService.getPendingRequests();
        BigDecimal userBalance = transactionService.viewCurrentBalance();

        if (pendingTransactions.length != 0) {
            System.out.println(printHeading(TRANSFER_HISTORY_HEADER));
            for (Transaction transaction : pendingTransactions){
                System.out.print(transaction);
            }            

            int pending_trans_id = -1; 
            while (pending_trans_id != 0) {
                pending_trans_id = consoleService.promptForInt("Please enter transfer ID to approve/reject it (0 to cancel): ");

                for (Transaction transaction : pendingTransactions){
                    if (transaction.getTransfer_id() == pending_trans_id){
                        pending_trans_id = 0;
                        int choice = -1; 
                        consoleService.printApprovalMenu();
                        while (choice != 0) {
                            choice = consoleService.promptForInt("Please choose an option: ");
                            if (choice == 1) {
                                if (transaction.getAmount().compareTo(userBalance) > 0) {
                                    System.out.println("Your available balance is lower than request you want to approve...\n");
                                }
                                else {
                                transaction.setTransfer_status_id(TrasnferStatus.APPROVED());
                                transactionService.approveTransaction(transaction);
                                break; 
                                }
                            }
                            if (choice == 2) {
                                transaction.setTransfer_status_id(TrasnferStatus.REJECTED());
                                transactionService.approveTransaction(transaction);
                                break; 
                            }
                            if (choice == 0) {
                                continue;
                            }
                        }
                    }
                }
                if (pending_trans_id != 0) {
                System.out.println("Incorrect input! ");
            }
            }
        } else {
            System.out.println("No pending transfers! ");
        }
		
	}

	private void sendBucks() {
        TransactionService transactionService = new TransactionService(currentUser);
        User[] userList = transactionService.getUserList();
        BigDecimal userBalance = transactionService.viewCurrentBalance();
        try {
            StringBuffer prompt = new StringBuffer();
            prompt.append(printHeading(USER_LIST_HEADER));
            for (User user : userList) {
                if (!currentUser.getUser().equals(user)) {
                    prompt.append(String.format("%-10s\t%-20s\n", user.getId(), user.getUsername()));
                }
            }
            prompt.append("Please enter a valid USER_ID from the list of recipients to whom you want to transfer money: ");

            boolean userSelected = false;
            int current_user_id = currentUser.getUser().getId();
            int beneficiary_id = -1;

            while (userSelected != true) {
                beneficiary_id = consoleService.promptForInt(prompt.toString()); 
                for (User user : userList) {
                    if (user.getId() == beneficiary_id && user.getId() != current_user_id) {  
                        userSelected = true;
                        BigDecimal transferAmount = consoleService.promptForBigDecimal("How much would you like to send " + user.getUsername() + "? : ");
                        if (transferAmount.compareTo(BigDecimal.ZERO) > 0 && transferAmount.compareTo(userBalance) <= 0) {
                            Transaction transaction = new Transaction();
                            transaction.setAccount_from(transactionService.getAccountId(current_user_id));
                            transaction.setAccount_to(transactionService.getAccountId(beneficiary_id));
                            transaction.setAmount(transferAmount);
                            transaction.setTransfer_type_id(TransferType.SEND());
                            transaction.setTransfer_status_id(TrasnferStatus.APPROVED());
                            transactionService.makeTransaction(transaction);
                        }
                        else {
                            System.out.println("You don't have enough money for transfer or inputed number is less or equal ZERO!");
                        }
                    }
                    }
                // System.out.println("\n Requested user_id is not found!");
                }
            } catch (NullPointerException e) {
                System.err.println("No available users for money transfer...");
            }
        }

	private void requestBucks() {
        TransactionService transactionService = new TransactionService(currentUser); 
        User[] userList = transactionService.getUserList();
        StringBuffer prompt = new StringBuffer();
        prompt.append(printHeading(USER_MENU_HEADER));
        try {
            for (User user : userList) {
                if (!currentUser.getUser().equals(user)) {
                    prompt.append(String.format("%-10s\t%-20s\n", user.getId(), user.getUsername()));
                }
            }
            prompt.append("Please enter a valid USER_ID from the list of sender you requesting money: ");

            boolean userSelected = false;
            int requestor_user_id = currentUser.getUser().getId();
            int sender_user_id = -1;
            while (userSelected != true) {
                sender_user_id = consoleService.promptForInt(prompt.toString());
                for (User user : userList) {
                    if (user.getId() == sender_user_id && sender_user_id != requestor_user_id) {    
                        userSelected = true;
                        BigDecimal transferAmount = consoleService.promptForBigDecimal("How much would you like to request from " + user.getUsername() + "? (0 to cancel) : ");
                        if (transferAmount.compareTo(BigDecimal.ZERO) == 1){
                            Transaction transaction = new Transaction(); 
                            transaction.setAccount_from(transactionService.getAccountId(requestor_user_id));
                            transaction.setAccount_to(transactionService.getAccountId(sender_user_id));
                            transaction.setAmount(transferAmount);
                            transaction.setTransfer_type_id(TransferType.REQUEST());
                            transaction.setTransfer_status_id(TrasnferStatus.PENDING());
                            transactionService.makeTransaction(transaction);
                        }
                        else {
                            System.out.println("You entered incorrect amount, please try again later.");
                        }
                        }
                }
            }    	
        } catch (NullPointerException e) {
            System.err.println("No available users for money transfer...");
        } 	
	}

//Helper method to create header with underscores:
    private String printHeading(String headingText) {
        StringBuffer header = new StringBuffer();
        for (int i = 0; i < headingText.length(); i++) {
			header.append("=");
		}
        header.append("\n");
        header.append(headingText);
        for (int i = 0; i < headingText.length(); i++) {
			header.append("=");
		}
        header.append("\n");
		return header.toString();
	}

}
