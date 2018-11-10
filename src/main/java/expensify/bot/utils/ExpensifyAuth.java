package expensify.bot.utils;

public class ExpensifyAuth {

  private String partnerUserID;
  private String partnerUserSecret;

  public ExpensifyAuth(String partnerUserID, String partnerUserSecret) {
    this.partnerUserID = partnerUserID;
    this.partnerUserSecret = partnerUserSecret;
  }

  public String getPartnerUserID() {
    return partnerUserID;
  }

  public void setPartnerUserID(String partnerUserID) {
    this.partnerUserID = partnerUserID;
  }

  public String getPartnerUserSecret() {
    return partnerUserSecret;
  }

  public void setPartnerUserSecret(String partnerUserSecret) {
    this.partnerUserSecret = partnerUserSecret;
  }
}
