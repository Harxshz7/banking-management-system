package banking.services;

import banking.models.Account;
import banking.models.Transaction;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatementService {
    private final BankingService bankingService;

    public StatementService(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    public byte[] generateStatementPdf(String accountId, String userId, LocalDate from, LocalDate to) {
        Account account = bankingService.getUserAccounts(userId).stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElse(null);

        if (account == null) {
            throw new IllegalArgumentException("Account not found or does not belong to user");
        }

        List<Transaction> transactions = bankingService.getFilteredTransactions(userId, from, to, null).stream()
                .filter(t -> t.getAccountId().equals(accountId))
                .toList();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Account Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            document.add(new Paragraph("Account Holder: " + userId, normalFont));
            document.add(new Paragraph("Account Number: " + account.getAccountNumber(), normalFont));
            document.add(new Paragraph("Period: " + (from != null ? from : "Beginning") + " to " + (to != null ? to : "End"), normalFont));
            document.add(new Paragraph("Generated On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), normalFont));
            document.add(new Paragraph(" ", normalFont));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            float[] columnWidths = {2f, 3f, 2f, 2f, 2f};
            table.setWidths(columnWidths);

            String[] headers = {"Date", "Description", "Type", "Amount", "Balance"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            for (Transaction tx : transactions) {
                table.addCell(new Phrase(tx.getTimestamp().format(dtf), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                table.addCell(new Phrase(tx.getDescription(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                table.addCell(new Phrase(tx.getType().name(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                
                String amountStr = String.format("$%.2f", tx.getAmount());
                PdfPCell amountCell = new PdfPCell(new Phrase(amountStr, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(amountCell);

                String balanceStr = String.format("$%.2f", tx.getBalanceAfter());
                PdfPCell balanceCell = new PdfPCell(new Phrase(balanceStr, FontFactory.getFont(FontFactory.HELVETICA, 10)));
                balanceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(balanceCell);
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF statement", e);
        }
    }
}
