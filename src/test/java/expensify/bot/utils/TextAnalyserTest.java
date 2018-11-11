package expensify.bot.utils;

import org.junit.Assert;
import org.junit.Test;

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
}