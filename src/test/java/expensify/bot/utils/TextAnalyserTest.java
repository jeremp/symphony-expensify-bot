package expensify.bot.utils;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class TextAnalyserTest {

  private TextAnalyser textAnalyser = new TextAnalyser();

  @Test
  public void isAuthAction() {
    Assert.assertTrue(textAnalyser.isAuthAction("auth"));
    Assert.assertTrue(textAnalyser.isAuthAction("auth   "));
    Assert.assertTrue(textAnalyser.isAuthAction("auth hello"));
    Assert.assertTrue(textAnalyser.isAuthAction("auth hello!"));
    Assert.assertFalse(textAnalyser.isAuthAction(null));
    Assert.assertFalse(textAnalyser.isAuthAction("auths hello!"));
    Assert.assertFalse(textAnalyser.isAuthAction("expense 31.5EUR The Juice Box"));
  }

  @Test
  public void isExpenseAction() {
    Assert.assertTrue(textAnalyser.isExpenseAction("expense 31.5EUR The Juice Box"));
    Assert.assertFalse(textAnalyser.isExpenseAction("auth"));
    Assert.assertFalse(textAnalyser.isExpenseAction("auth   "));
    Assert.assertFalse(textAnalyser.isExpenseAction("auth hello"));
    Assert.assertFalse(textAnalyser.isExpenseAction("auth hello!"));
  }

  @Test
  public void extractAuthTokens() {
    String message = " auth TOKEN_A TOKEN_B";
    List<String> result = textAnalyser.extractAuthTokens(message);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("TOKEN_A", result.get(0));
    Assert.assertEquals("TOKEN_B", result.get(1));
  }


  @Test
  public void extractAmount() {
    String text = "  Hello world 565 qsd5454  Hey go 58$ qslk 58, $ lkl 52. $ kqlksd 15,36 $ kq,skd 45$ jkdsfj and 25€ or 25,14 €";
    List<BigDecimal> amounts = textAnalyser.extractAmount(text);
    Assert.assertEquals(7, amounts.size());
  }

  @Test
  public void extractMerchant() {
    String monday = "expense Las Vegas pizza 13 €";
    String mondayMerchant = textAnalyser.extractMerchant(monday);
    Assert.assertEquals("Las Vegas pizza", mondayMerchant);

    String tuesday = "expense 12,20€ my French Burger";
    String tuesdayMerchant = textAnalyser.extractMerchant(tuesday);
    Assert.assertEquals("my French Burger", tuesdayMerchant);

    String wednesday = "expense ramen ta faim  12.80 €";
    String wednesdayMerchant = textAnalyser.extractMerchant(wednesday);
    Assert.assertEquals("ramen ta faim", wednesdayMerchant);

    String thursday = "expense 08€ TheJuiceBox";
    String thursdayMerchant = textAnalyser.extractMerchant(thursday);
    Assert.assertEquals("TheJuiceBox", thursdayMerchant);

    String friday = "expense 06.04 € Les.farcis.de.sophie";
    String fridayMerchant = textAnalyser.extractMerchant(friday);
    Assert.assertEquals("Les.farcis.de.sophie", fridayMerchant);
  }
}