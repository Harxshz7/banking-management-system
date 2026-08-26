package banking.api.config;

import banking.services.AuthService;
import banking.services.BankingService;
import banking.services.LoanService;
import banking.services.StatementService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public AuthService authService() {
        return new AuthService();
    }

    @Bean
    public BankingService bankingService() {
        return new BankingService();
    }

    @Bean
    public LoanService loanService() {
        return new LoanService();
    }

    @Bean
    public StatementService statementService(BankingService bankingService) {
        return new StatementService(bankingService);
    }
}
