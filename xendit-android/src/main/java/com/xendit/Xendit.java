package com.xendit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import com.xendit.Models.BillingDetails;
import com.xendit.Models.Card;
import com.xendit.Models.CardHolderData;
import com.xendit.Models.Customer;
import com.xendit.utils.CardValidator;
import com.xendit.utils.StoreCVNCallback;

public interface Xendit {
  String ACTION_KEY = "ACTION_KEY";

  static Xendit create(final Context context, String publishableKey,
      Activity activity) {
    return new XenditImpl(context, publishableKey, activity);
  }

  static Xendit create(final Context context, String publishableKey) {
    return new XenditImpl(context, publishableKey);
  }

  /**
   * Determines whether the credit card number provided is valid
   *
   * @param creditCardNumber A credit card number
   * @return true if the credit card number is valid, false otherwise
   * @deprecated Use CardValidator.isCardNumberValid
   */
  @Deprecated
  static boolean isCardNumberValid(String creditCardNumber) {
    return CardValidator.isCardNumberValid(creditCardNumber);
  }

  /**
   * Determines whether the card expiration month and year are valid
   *
   * @param cardExpirationMonth The month a card expired represented by digits (e.g. 12)
   * @param cardExpirationYear The year a card expires represented by digits (e.g. 2026)
   * @return true if both the expiration month and year are valid
   * @deprecated Use CardValidator.isExpiryValid
   */
  @Deprecated
  static boolean isExpiryValid(String cardExpirationMonth, String cardExpirationYear) {
    return CardValidator.isExpiryValid(cardExpirationMonth, cardExpirationYear);
  }

  /**
   * Determines whether the card CVN is valid
   *
   * @param creditCardCVN The credit card CVN
   * @return true if the cvn is valid, false otherwise
   * @deprecated Use CardValidator.isCvnValid
   */
  @Deprecated
  static boolean isCvnValid(String creditCardCVN) {
    return CardValidator.isCvnValid(creditCardCVN);
  }

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount you will eventually charge. This value is used to display to the
   * user in the 3DS authentication view.
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(final Card card, final int amount, final TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount you will eventually charge. This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate,
      final TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount you will eventually charge. This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(final Card card, final int amount, final boolean shouldAuthenticate,
      final String onBehalfOf, final TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount you will eventually charge. This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer object making the transaction
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(
      final Card card,
      final int amount,
      final boolean shouldAuthenticate,
      final String onBehalfOf,
      final BillingDetails billingDetails,
      final Customer customer,
      TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount you will eventually charge. This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer object making the transaction
   * @param currency Currency when requesting for 3DS authentication
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(
      final Card card,
      final int amount,
      final boolean shouldAuthenticate,
      final String onBehalfOf,
      final BillingDetails billingDetails,
      final Customer customer,
      final String currency,
      TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount in string you will eventually charge. This value is used to display to
   * the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are required for this token
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer object making the transaction
   * @param currency Currency when requesting for 3DS authentication
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(
      final Card card,
      final String amount,
      final boolean shouldAuthenticate,
      final String onBehalfOf,
      final BillingDetails billingDetails,
      final Customer customer,
      final String currency,
      TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this
   * method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount in string you will eventually charge.
   * This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are
   * required for this token
   * @param currency Currency when requesting for 3DS authentication
   * @param tokenCallback The callback that will be called when the token
   * creation completes or
   * fails
   */
  void createSingleUseToken(
      final Card card,
      final String amount,
      final boolean shouldAuthenticate,
      final String currency,
      TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this method unless you
   * set shouldAuthenticate as false.
   *
   * @param tokenId ID of the token as the identifier
   * @param cardCvn CVV number of the card
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createSingleUseToken(
      final BillingDetails billingDetails,
      final Customer customer,
      final String tokenId,
      final String cardCvn,
      TokenCallback tokenCallback);

  /**
   * Creates a single-use token. 3DS authentication will be bundled into this
   * method unless you
   * set shouldAuthenticate as false.
   *
   * @param card A credit card
   * @param amount The amount in string you will eventually charge.
   * This value is used to display to the
   * user in the 3DS authentication view.
   * @param shouldAuthenticate A flag indicating if 3DS authentication are
   * required for this token
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer object making the transaction
   * @param currency Currency when requesting for 3DS authentication
   * @param midLabel Mid label to perform authentication if tokenization
   * is bundled with authenticaiton. This is only
   * applicable for switcher mid.
   * @param tokenCallback The callback that will be called when the token
   * creation completes or
   * fails
   */
  void createSingleUseToken(
      final Card card,
      final String amount,
      final boolean shouldAuthenticate,
      final String onBehalfOf,
      final BillingDetails billingDetails,
      final Customer customer,
      final String currency,
      final String midLabel,
      TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
   * is true.
   *
   * @param card A credit card
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createMultipleUseToken(final Card card, final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
   * is true.
   *
   * @param card A credit card
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createMultipleUseToken(final Card card, final String onBehalfOf,
      final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
   * is true.
   *
   * @param card A credit card
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createMultipleUseToken(final Card card, final String onBehalfOf,
      BillingDetails billingDetails,
      final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
   * is true.
   *
   * @param card A credit card
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer linked to the payment method
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createMultipleUseToken(final Card card, final String onBehalfOf,
      BillingDetails billingDetails, Customer customer, final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if shouldAuthenticate
   * is true.
   *
   * @param card A credit card
   * @param onBehalfOf The onBehalfOf is sub account business id
   * @param billingDetails Billing details of the card
   * @param customer Customer linked to the payment method
   * @param midLabel Mid label to perform authentication if tokenization is
   * bundled with tokenization.
   * This argument is only applicable for switcher merchant.
   * @param tokenCallback The callback that will be called when the token creation completes or
   * fails
   */
  void createMultipleUseToken(final Card card, final String onBehalfOf,
      BillingDetails billingDetails, Customer customer, final String midLabel,
      final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if
   * shouldAuthenticate
   * is true.
   *
   * @param tokenId ID of the token as the identifier
   * @param cardCvn CVV number of the card
   * @param billingDetails Billing details of the card
   * @param customer Customer linked to the payment method
   * @param tokenCallback The callback that will be called when the token
   * creation completes or
   * fails
   */
  void createMultipleUseToken(
      final BillingDetails billingDetails,
      final Customer customer,
      final String tokenId,
      final String cardCvn,
      final TokenCallback tokenCallback);

  /**
   * Creates a multiple-use token. Authentication must be created separately if
   * shouldAuthenticate
   * is true.
   *
   * @param tokenId ID of the token as the identifier
   * @param cardCvn CVV number of the card
   * @param billingDetails Billing details of the card
   * @param customer Customer linked to the payment method
   * @param midLabel Mid label to perform authentication if tokenization is
   * bundled with tokenization.
   * This argument is only applicable for switcher merchant.
   * @param tokenCallback The callback that will be called when the token
   * creation completes or
   * fails
   */
  void createMultipleUseToken(
      final BillingDetails billingDetails,
      final Customer customer,
      final String tokenId,
      final String cardCvn,
      final String midLabel,
      final TokenCallback tokenCallback);

  /**
   * Store CVN method will perform store cvn using an existing tokenId (retokenization).
   * This method is commonly used for performing re-tokenization on subsequent usage of a multi-use
   * token in the purpose of re-caching cardCVN.
   *
   * @param tokenId is a previously created Xendit multiple-use tokenId. Required field.
   * @param cardCvn is card cvn code linked to the tokenId created. Required field.
   * @param billingDetails Billing details of the card
   * @param customer Customer linked to the payment method
   * @param onBehalfOf The onBehalfOf is sub account business id. This field is used for merchants
   * utilizing xenPlatform feature.
   * @param storeCVNCallback The callback that will be called when the token re-creation completes
   * or
   * fails
   */
  void storeCVN(
      final String tokenId,
      final String cardCvn,
      final BillingDetails billingDetails,
      final Customer customer,
      final String onBehalfOf,
      final StoreCVNCallback storeCVNCallback
  );

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final int amount, final String currency,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardHolderData Additional information of the card holder data
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final int amount, final String currency,
      final CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId The id of a multiple-use token
   * @param amount The amount that will eventually be charged. This number is displayed to the
   * user in the 3DS authentication view
   * @param authenticationCallback The callback that will be called when the authentication
   * creation completes or fails
   */
  void createAuthentication(final String tokenId, final int amount,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId The id of a multiple-use token
   * @param amount The amount that will eventually be charged. This number is displayed to the
   * user in the 3DS authentication view
   * @param cardHolderData Additional information of the card holder
   * @param authenticationCallback The callback that will be called when the
   * authentication creation completes or fails
   */
  void createAuthentication(final String tokenId, final int amount,
      CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param cardHolderData Additional information of the card holder data
   * @param authenticationCallback The callback that will be called when the authentication
   * completes or
   * fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final CardHolderData cardHolderData,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardHolderData Additional information of the card holder data
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final CardHolderData cardHolderData, final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param onBehalfOf Business Id to call the API on behalf of
   * (Applicable to Platform merchants)
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final String onBehalfOf,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param cardHolderData Additional information of the card holder data
   * @param onBehalfOf Business Id to call the API on behalf of
   * (Applicable to Platform merchants)
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final CardHolderData cardHolderData, final String onBehalfOf,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param onBehalfOf Business Id to call the API on behalf of
   * (Applicable to Platform merchants)
   * @param midLabel Mid label to perform authentication if
   * tokenization is bundled with authenticaiton.
   * This is only applicable for switcher mid.
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or
   * fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final String onBehalfOf, final String midLabel,
      final AuthenticationCallback authenticationCallback);

  /**
   * Creates a 3DS authentication for a multiple-use token
   *
   * @param tokenId A multi-use token id
   * @param amount Amount of money to be authenticated
   * @param currency Currency of the amount
   * @param cardCvn CVV/CVN collected from the card holder
   * @param cardHolderData Additional information of the card holder data
   * @param onBehalfOf Business Id to call the API on behalf of
   * (Applicable to Platform merchants)
   * @param midLabel Mid label to perform authentication if
   * tokenization is bundled with authenticaiton.
   * This is only applicable for switcher mid.
   * @param authenticationCallback The callback that will be called when the
   * authentication completes or
   * fails
   */
  void createAuthentication(final String tokenId, final String amount,
      final String currency,
      final String cardCvn, final CardHolderData cardHolderData, final String onBehalfOf,
      final String midLabel,
      final AuthenticationCallback authenticationCallback);

  // createCreditCardToken with 5 arguments
  void createCreditCardToken(
      Card card,
      String amount,
      boolean shouldAuthenticate,
      boolean isMultipleUse,
      final TokenCallback tokenCallback);

  // createCreditCardToken with 6 arguments
  void createCreditCardToken(
      final Card card,
      final String amount,
      boolean shouldAuthenticate,
      final String onBehalfOf,
      boolean isMultipleUse,
      final TokenCallback tokenCallback);

  // createCreditCardToken with 11 arguments
  void createCreditCardToken(
      final Card card,
      final String amount,
      boolean shouldAuthenticate,
      final String onBehalfOf,
      boolean isMultipleUse,
      BillingDetails billingDetails,
      Customer customer,
      final String currency,
      final String tokenId,
      final String cardCvn,
      final TokenCallback tokenCallback);

  // createCreditCardToken with 12 arguments
  void createCreditCardToken(
      final Card card,
      final String amount,
      boolean shouldAuthenticate,
      final String onBehalfOf,
      boolean isMultipleUse,
      BillingDetails billingDetails,
      Customer customer,
      final String currency,
      final String tokenId,
      final String cardCvn,
      final String midLabel,
      final TokenCallback tokenCallback);

  void unregisterXenBroadcastReceiver(BroadcastReceiver receiver);

  void unregisterAllBroadcastReceiver();
}