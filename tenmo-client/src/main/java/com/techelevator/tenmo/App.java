package com.techelevator.tenmo;

import java.math.BigDecimal;

import com.fasterxml.jackson.databind.deser.std.NumberDeserializers.BigDecimalDeserializer;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transaction;
import com.techelevator.tenmo.model.TransferType;
import com.techelevator.tenmo.model.TrasnferStatus;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.ConsoleService;
import com.techelevator.tenmo.services.TransactionService;

public class App {

    private static final String API_BASE_URL = "http://localhost:8080/";

    private final String USER_MENU_HEADER = String.format("%-10s\t%-20s\n", "USER_ID", "USER_NAME" );
    private final String TRANSFER_HISTORY_HEADER = String.format("%-10s\t%-15s\t%-15s\t%-5s\n", "Transfers_ID", "From", "To", "Amount" );
    private final String TRANSFER_DETAILS = String.format("%-40s\n", "Transfer Details:"); 


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
		// Thomas : 
        TransactionService transactionService = new TransactionService(currentUser); 
        System.out.println(currentUser.getUser().getUsername()+" available balance is: " + transactionService.viewCurrentBalance());
		
	}

	private void viewTransferHistory() {
		// Tony:
        TransactionService transactionService = new TransactionService(currentUser); 
        Transaction[] transHistory = transactionService.getTransferHistory();
        if (transHistory!= null) {
            System.out.println(printHeading(TRANSFER_HISTORY_HEADER));
            for (Transaction transaction : transHistory){
                System.out.print(transaction);
            }
            //-----------------This needs to be work on!-------------------------------// 
            int trans_id = consoleService.promptForInt("Please choose a transfer_id to see details or hit ENTER to exit form Transaction history menu: ");            
            for (Transaction transaction : transHistory){
                if (trans_id == transaction.getTransfer_id()){
                System.out.println(printHeading(TRANSFER_DETAILS));
                System.out.println(transaction.showDetails());
                }
            }
        }
        else {
            System.out.println("No available history on user account.");
        }
	}

	private void viewPendingRequests() {
    //agarkov: 
        TransactionService transactionService = new TransactionService(currentUser); 
        Transaction[] pendingTransactions = transactionService.getPendingRequests();

        if (pendingTransactions != null) {
            System.out.println(printHeading(TRANSFER_HISTORY_HEADER));
            for (Transaction transaction : pendingTransactions){
                System.out.print(transaction);
            }            
            //-----------------This needs to be work on!-------------------------------// 
            int pending_trans_id = consoleService.promptForInt("Please enter transfer ID to approve/reject (0 to cancel): ");            
            for (Transaction transaction : pendingTransactions){
                if (transaction.getTransfer_id() == pending_trans_id){
                    int choice = -1; 
                    consoleService.printApprovalMenu();
                    choice = consoleService.promptForInt("Please choose an option: ");
                    if (choice == 1) {
                        transaction.setTransfer_status_id(TrasnferStatus.APPROVED());
                        transactionService.approveTransaction(transaction);
                    }
                    if (choice == 2) {
                        transaction.setTransfer_status_id(TrasnferStatus.REJECTED());
                        transactionService.approveTransaction(transaction);
                    }
                    if (choice == 0) {
                        break;
                    }
                }
            }
        }

	}

	private void sendBucks() {
		// TODO Auto-generated method stub
        TransactionService transactionService = new TransactionService(currentUser); 
        User[] userList = transactionService.getAvailableUsers();
        BigDecimal userBalance = transactionService.viewCurrentBalance();

        StringBuffer prompt = new StringBuffer();
        prompt.append(printHeading(USER_MENU_HEADER)); 

        for (User user : userList) {
            if (!currentUser.getUser().equals(user)) {
                prompt.append(String.format("%-10s\t%-20s\n", user.getId(), user.getUsername()));
            }
        }

        boolean userSelection = false;
        int current_user_id = currentUser.getUser().getId();
        int beneficiar_user_id = -1;
        
        while (userSelection != true) {
            beneficiar_user_id = consoleService.promptForUser(prompt.toString());
            for (User user : userList) {
                if (user.getId() == beneficiar_user_id && beneficiar_user_id != current_user_id) {    
                    userSelection = true;
                    BigDecimal transferAmount = consoleService.promptForBigDecimal("Please enter amount you want to transfer from your account to " + user.getUsername() + " : ");
                    if (transferAmount.compareTo(userBalance) <= 0){
                        Transaction transaction = new Transaction(); 
                        transaction.setAccount_from(transactionService.getAccountId(current_user_id));
                        transaction.setAccount_to(transactionService.getAccountId(beneficiar_user_id));
                        transaction.setAmount(transferAmount);
                        transaction.setTransfer_type_id(TransferType.SEND());
                        transaction.setTransfer_status_id(TrasnferStatus.APPROVED());
                        transactionService.makeTransfer(transaction);
                    }
                    }
            }
        }    
    }

	private void requestBucks() {
		// TODO Auto-generated method stub
        TransactionService transactionService = new TransactionService(currentUser); 
        User[] userList = transactionService.getAvailableUsers();
        StringBuffer prompt = new StringBuffer();

        prompt.append(printHeading(USER_MENU_HEADER)); 
        for (User user : userList) {
            if (!currentUser.getUser().equals(user)) {
                prompt.append(String.format("%-10s\t%-20s\n", user.getId(), user.getUsername()));
            }
        }

        boolean userSelection = false;
        int requestor_user_id = currentUser.getUser().getId();
        int sender_user_id = -1;
        while (userSelection != true) {
            sender_user_id = consoleService.promptForUser(prompt.toString());
            for (User user : userList) {
                if (user.getId() == sender_user_id && sender_user_id != requestor_user_id) {    
                    userSelection = true;
                    BigDecimal transferAmount = consoleService.promptForBigDecimal("Enter ID of user you are requesting from " + user.getUsername() + " (0 to cancel) : ");
                    if (transferAmount.compareTo(BigDecimal.ZERO) == 1){
                        Transaction transaction = new Transaction(); 
                        transaction.setAccount_from(transactionService.getAccountId(requestor_user_id));
                        transaction.setAccount_to(transactionService.getAccountId(sender_user_id));
                        transaction.setAmount(transferAmount);
                        transaction.setTransfer_type_id(TransferType.REQUEST());
                        transaction.setTransfer_status_id(TrasnferStatus.PENDING());
                        transactionService.makeTransfer(transaction);
                    }
                    }
            }
        }    


    }

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
