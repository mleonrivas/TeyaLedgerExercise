# TeyaLedgerExercise
### Building and Running the Application:
  1. Building the app: `./gradlew clean build`.
  2. Running the app: 
     - **ALTERNATIVE 1:** In the command line from the root directory, run `./gradlew bootRun` (or `gradlew.bat bootRun` on Windows).
     - **ALTERNATIVE 2:** Form the Intellij IDEA IDE, you can directly run the file LedgerApplication.kt after syncing gradle.

### Authentication:
  1. The application differentiates between two roles: `USER` and `ADMIN`.
  2. A Dummy Auth has been implemented to differentiate between different users and admins. Just add the header `X-User-Id` with the UUID of the user, and the header `X-User-Role` with the value `USER` or `ADMIN`. 
  3. Added this dummy method to simulate that there's an API Gateway in front of the service that takes care of authentication for users, and that this service receives the user and role details in the request from the gateway.

### Using the Application:
 The application runs on port 8080 by default, so it can be accessed in http://localhost:8080. To interact with the API:
 1. You can use the included Swagger UI at `http://localhost:8080/swagger-ui/index.html`. Remember to set the authentication headers values.
 2. You can use Postman or any other API client to interact with the API. 
 3. You can use curl from the command line. There's an entire testing script below, based on curl commands. 

### Database:
  The service uses an H2 in-memory database. Everytime that the service is restarted, the database resets.

### Testing Script:
I recommend copying the entire script to a text file, and use any editing tool like sublime to replace the placeholders <ALICE_ID>, <BOB_ID>, <ACCOUNT_ID>, <BOB_ACCOUNT_ID> with the actual values obtained from the responses.
```bash
# 1. Acting as ADMIN, With X-User-Id=admin and X-User-Role=ADMIN:
#   1.1 Create a couple of users: Alice and Bob:
        curl -X POST "http://localhost:8080/users" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: admin" -H "X-User-Role: ADMIN" -d "{ \"name\": \"Alice\", \"role\": \"USER\", \"email\": \"alice@teya.com\"}"
        curl -X POST "http://localhost:8080/users" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: admin" -H "X-User-Role: ADMIN" -d "{ \"name\": \"Bob\", \"role\": \"USER\", \"email\": \"bob@teya.com\"}"
#   1.2 Verify the users were created correctly, and copy their IDs:
        curl -X GET "http://localhost:8080/users" -H "accept: */*" -H "X-User-Id: admin" -H "X-User-Role: ADMIN"
#   1.3 Create the accounts for Alice and Bob, replacing <ALICE_ID> and <BOB_ID> with the IDs from the previous step, annotate the resulting IDs:
        curl -X POST "http://localhost:8080/accounts" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: admin" -H "X-User-Role: ADMIN" -d "{ \"currency\": \"GBP\", \"initialBalance\": \"1000.00\", \"ownerId\": \"<ALICE_ID>\", \"alias\": \"Alice's Account\"}"
        curl -X POST "http://localhost:8080/accounts" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: admin" -H "X-User-Role: ADMIN" -d "{ \"currency\": \"EUR\", \"initialBalance\": \"5000.00\", \"ownerId\": \"<BOB_ID>\", \"alias\": \"Bob's Account\"}"
# 2. Acting as Alice, with X-User-Id=<ALICE_ID> and X-User-Role=USER:
#   2.1 Try creating an account (Should fail with 404 --not found-- as Alice is not admin and can't see that resource):
        curl -X POST "http://localhost:8080/accounts" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"currency\": \"GBP\", \"initialBalance\": \"1000.00\", \"ownerId\": \"<ALICE_ID>\", \"alias\": \"Alice's 2nd Account\"}"
#   2.2 List alice accounts (only alice's account should be visible):
        curl -X GET "http://localhost:8080/accounts" -H "accept: */*" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER"
#   2.3 Do a deposit to Alice's account, replacing <ACCOUNT_ID> with the Alice's accountId from the previous steps:
        curl -X POST "http://localhost:8080/transactions" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"amount\": \"100.00\", \"accountId\": <ACCOUNT_ID>, \"type\": \"DEPOSIT\", \"concept\": \"Additional Deposit\"}"
#   2.4 Do a withdrawal from Alice's account of 2000 GBP, should fail with insufficient balance:
        curl -X POST "http://localhost:8080/transactions" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"amount\": \"2000.00\", \"accountId\": <ACCOUNT_ID>, \"type\": \"WITHDRAWAL\", \"concept\": \"Invalid Withdrawal\"}"   
#   2.5 Try another withdrawal with a valid amount (500 GBP):
        curl -X POST "http://localhost:8080/transactions" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"amount\": \"500.00\", \"accountId\": <ACCOUNT_ID>, \"type\": \"WITHDRAWAL\", \"concept\": \"Valid Withdrawal\"}"
#   2.6 Try widthdrawing money from Bob's account (should fail with a 400):
        curl -X POST "http://localhost:8080/transactions" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"amount\": \"100.00\", \"accountId\": <BOB_ACCOUNT_ID>, \"type\": \"WITHDRAWAL\", \"concept\": \"Robbing Attempt\"}"
#   2.7 Do a proper transfer from alice to bob:
        curl -X POST "http://localhost:8080/transactions" -H "accept: */*" -H "Content-Type: application/json" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" -d "{ \"amount\": \"300.00\", \"accountId\": <ACCOUNT_ID>, \"type\": \"TRANSFER\", \"toAccountId\": <BOB_ACCOUNT_ID>, \"concept\": \"Paying debt\"}"
#   2.8 Check the Alice's account details (balance = 300 GBP):
        curl -X GET "http://localhost:8080/accounts/<ACCOUNT_ID>" -H "accept: */*" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER"
#   2.9 Check Alice's account ledger, should show:
#      - The initial deposit of 1000 GBP (CREDIT)
#      - The deposit of 100 GBP (CREDIT)
#      - The withdrawal of 500 GBP (DEBIT)
#      - The transfer to bob of 300 GBP (DEBIT)
        curl -X GET "http://localhost:8080/accounts/<ACCOUNT_ID>/ledger" -H "accept: */*" -H "X-User-Id: <ALICE_ID>" -H "X-User-Role: USER" 

# 3. Acting as Bob, with X-User-Id=<BOB_ID> and X-User-Role=USER:
#   3.1 Check Bob's account details (balance = 5360.75 EUR --- 300 GBP ~ 360.75 EUR):
        curl -X GET "http://localhost:8080/accounts/<BOB_ACCOUNT_ID>" -H "accept: */*" -H "X-User-Id: <BOB_ID>" -H "X-User-Role: USER"
#   3.2 Check Bob's account ledger, should show:
#      - The initial deposit of 5000 EUR (CREDIT)
#      - The 360.75 EUR (300 GBP) transfer from alice (CREDIT)
        curl -X GET "http://localhost:8080/accounts/<BOB_ACCOUNT_ID>/ledger" -H "accept: */*" -H "X-User-Id: <BOB_ID>" -H "X-User-Role: USER"
   ```
## Documented trade-offs:
1. Money representation: I chose to use BigDecimal over Double and Integer. 
   Double is not ok for financial instruments due to the lack of precision, that can result in calculation errors. 
   The other option is using an Integer representing the amounts at the cent level (e.g. 5.99 -> 599). It'd be more performant
   but it would require to do conversions all over the place. Given that there is no specific performance requirement, I chose 
   the BigDecimal representation for simplicity.

2. In-memory DB vs basic data-structures.
   I used an in-memory DB as it resembles a more realistic scenario. I could have used basic data-structures like 
   LinkedHashMap and Lists to store the data, but I think this approach offers a better idea of how the system would be.

3. Single transactions vs double-ledger-entries
   I implemented a double-ledger-entry system. Although it requires managing more entities, 
   the ledger is always ready to be read and does not have to be computed or transformed.

4. REST+text/JSON vs GRPC+protobuf
   I'd only select GRPC+protobuf if high-performance, minimising latency was a requirement.
   As there was no such requirement, I chose REST+text/JSON for simplicity and readability.

5. Missing methods: I have not implemented all CRUD and PATCH methods over the resources, just focused on the ledger functionality.

6. Authentication: I implemented a dummy authentication method favouring interviewer's testing ease. 
   In a real-world scenario, I'd use a proper authentication method, like an OAuth2 Bearer Token. And likely, 
   there will be an API Gateway that would take care of the Authentication, and this service would receive the user and role details in the request from the gateway.