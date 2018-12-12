package expensify.bot.domain;

/**
 * This data transfer object represen an expense.
 */
public class ExpenseDto {

  private String merchant;
  private Double amount;
  private String currency;
  private String reportNumber;
  private String expenseNumber;
  private String created;

  /**
   * Constructor with all fields: composition.
   */
  public ExpenseDto(String merchant, Double amount, String currency, String reportNumber, String expenseNumber,
      String created) {
    this.merchant = merchant;
    this.amount = amount;
    this.currency = currency;
    this.reportNumber = reportNumber;
    this.expenseNumber = expenseNumber;
    this.created = created;
  }

  public String getMerchant() {
    return merchant;
  }

  public Double getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public String getReportNumber() {
    return reportNumber;
  }

  public String getExpenseNumber() {
    return expenseNumber;
  }

  public String getCreated() {
    return created;
  }
}
