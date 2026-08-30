package banking.api;

import banking.api.controllers.AccountController;
import banking.api.controllers.LoanController;
import banking.api.controllers.TransferController;
import banking.api.dto.LoginRequest;
import banking.data.DataManager;
import banking.data.DatabaseManager;
import banking.models.Account;
import banking.models.Loan;
import banking.models.User;
import banking.services.BankingService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankingService bankingService;

    @BeforeAll
    static void initDb() {
        DatabaseManager.setDbUrl("jdbc:sqlite::memory:");
        DataManager.resetInstance();
        DataManager.getInstance();
    }

    private String getAuthToken(String username, String password) throws Exception {
        LoginRequest req = new LoginRequest(username, password);
        MvcResult res = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = objectMapper.readTree(res.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    @Test
    public void testLoginSuccess() throws Exception {
        String token = getAuthToken("john", "john123");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest req = new LoginRequest("unknown", "wrong");
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetAccounts() throws Exception {
        String token = getAuthToken("john", "john123");
        DataManager dm = DataManager.getInstance();
        User john = dm.findUserByUsername("john").orElseThrow();

        MvcResult res = mockMvc.perform(get("/accounts/" + john.getId())
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode accounts = objectMapper.readTree(res.getResponse().getContentAsString());
        assertTrue(accounts.isArray());
        assertTrue(accounts.size() >= 1);
    }

    @Test
    public void testDepositAndWithdraw() throws Exception {
        String token = getAuthToken("john", "john123");
        DataManager dm = DataManager.getInstance();
        User john = dm.findUserByUsername("john").orElseThrow();
        Account savings = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();

        // Deposit
        AccountController.DepositRequest depReq = new AccountController.DepositRequest(150.0, "API Deposit");
        mockMvc.perform(post("/accounts/" + savings.getId() + "/deposit")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depReq)))
                .andExpect(status().isOk());

        // Withdraw
        AccountController.WithdrawRequest withReq = new AccountController.WithdrawRequest(50.0, "API Withdraw");
        mockMvc.perform(post("/accounts/" + savings.getId() + "/withdraw")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withReq)))
                .andExpect(status().isOk());
    }

    @Test
    public void testTransferSuccessAndInsufficientFunds() throws Exception {
        String token = getAuthToken("john", "john123");
        DataManager dm = DataManager.getInstance();
        User john = dm.findUserByUsername("john").orElseThrow();
        User jane = dm.findUserByUsername("jane").orElseThrow();

        Account johnSavings = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();
        Account janeSavings = dm.getAccountsByUser(jane.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();

        // Transfer Success
        TransferController.TransferRequest successReq = new TransferController.TransferRequest(
                johnSavings.getId(), janeSavings.getAccountNumber(), 25.0, "Test Transfer API");
        mockMvc.perform(post("/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(successReq)))
                .andExpect(status().isOk());

        // Insufficient Funds Failure
        TransferController.TransferRequest failReq = new TransferController.TransferRequest(
                johnSavings.getId(), janeSavings.getAccountNumber(), 99999999.0, "Too Big");
        mockMvc.perform(post("/transfer")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failReq)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoanApply() throws Exception {
        String token = getAuthToken("john", "john123");
        DataManager dm = DataManager.getInstance();
        User john = dm.findUserByUsername("john").orElseThrow();
        Account johnSavings = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();

        LoanController.LoanApplyRequest loanReq = new LoanController.LoanApplyRequest(
                johnSavings.getId(), Loan.LoanType.PERSONAL, 5000.0, 12, "API Personal Loan");

        mockMvc.perform(post("/loans/apply")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanReq)))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetStatementPdf() throws Exception {
        String token = getAuthToken("john", "john123");
        DataManager dm = DataManager.getInstance();
        User john = dm.findUserByUsername("john").orElseThrow();
        Account johnSavings = dm.getAccountsByUser(john.getId()).stream()
                .filter(a -> a.getType() == Account.AccountType.SAVINGS)
                .findFirst().orElseThrow();

        MvcResult res = mockMvc.perform(get("/accounts/" + johnSavings.getId() + "/statement")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("application/pdf", res.getResponse().getContentType());
        assertTrue(res.getResponse().getContentAsByteArray().length > 0);
    }
}
