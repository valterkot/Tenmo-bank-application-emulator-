SELECT * FROM account join tenmo_user USING (user_id);

SELECT account_id FROM account WHERE user_id = 1001;

SELECT transfer_id, transfer_status_id, transfer_type_id, account_from, user_from.username as username_from, account_to, user_to.username as username_to, amount 
	FROM transfer 
	join account as acc_from ON account_from = acc_from.account_id 
	join tenmo_user as user_from ON acc_from.user_id = user_from.user_id
	join account as acc_to ON account_to = acc_to.account_id 
	join tenmo_user as user_to ON acc_to.user_id = user_to.user_id;

SELECT * FROM tenmo_user;

START TRANSACTION; 
UPDATE account SET balance = ? WHERE account_id = ?;
UPDATE account SET balance = ? WHERE accound_id = ?;
INSERT INTO transfer (transfer_type_id, transfer_status_id, account_from, account_to, amount) VALUES (?, ?, ?, ?, ?);
COMMIT;

UPDATE account SET balance = balance + 1000 WHERE account_id = 2001;
