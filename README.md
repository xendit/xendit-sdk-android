# xendit-sdk-android
The Xendit Android SDK makes it easy to collect sensitive credit card information without that data ever having to touch your server.

## Ownership

Team: [Credit Cards Team](https://www.draw.io/?state=%7B%22ids%22:%5B%221Vk1zqYgX2YqjJYieQ6qDPh0PhB2yAd0j%22%5D,%22action%22:%22open%22,%22userId%22:%22104938211257040552218%22%7D)

Slack Channel: [#cards-dev](https://xendit.slack.com/messages/cards-dev)

Slack Mentions: `@troops-cards`

## Requirements
The Xendit SDK is compatible with Android 5.0 and above.

NOTE: For version 3.0.0 onwards, only Android 5.0 (SDK 21) and above is supported.

## Example
Visit and try the `app` module to see an example of how the SDK works.

1. [Install android studio.](https://developer.android.com/studio/install)
2. [Clone repository.](https://help.github.com/en/github/creating-cloning-and-archiving-repositories/cloning-a-repository)
3. [Get your Public Key.](https://dashboard.xendit.co/settings/developers#api-keys)
4. Search for PUBLISHABLE_KEY and replace the content with your Public Key from Step 3.
```
PUBLISHABLE_KEY="xnd_public_development_XXX"
```

## Installation
Maven:
```
<dependency>
  <groupId>com.xendit</groupId>
  <artifactId>xendit-android</artifactId>
  <version>4.2.1</version>
  <type>pom</type>
</dependency>
```

Gradle:
```
compile 'com.xendit:xendit-android:4.2.1'
```

Ivy:
```
<dependency org='com.xendit' name='xendit-android' rev='4.2.1'>
  <artifact name='xendit-android' ext='pom' ></artifact>
</dependency>
```

For more information, visit https://central.sonatype.com/artifact/com.xendit/xendit-android/4.2.1/versions

**Note**:

Starting from version `3.7.0`, setting up cardinal commerce below is no longer needed.

For version `3.0.0` up to version `3.6.4`, you will need to include cardinal commerce repository credentials to download libraries required for EMV 3DS. Please configure the crendentials below to download the cardinal commerce SDK.

```
repositories {
    maven {
        url "https://cardinalcommerceprod.jfrog.io/artifactory/android"
        credentials {
            username 'cybersource_xendit_payfac_cards'
            password 'AKCp8k7kDxqgxY63sahozmx9h2wLR8QikWwSPnQyZZxy63juWsQJ8PevpWY6s3eREW4rrzTyj'
        }
    }
}
dependencies {
    ...
    implementation 'org.jfrog.cardinalcommerce.gradle:cardinalmobilesdk:2.2.7-2'
}
```

### Add XenditActivity in your AndroidManifest
```
<activity android:name="com.xendit.XenditActivity"/>
```


### Initializing Xendit
```
Xendit xendit = new Xendit(getApplicationContext(), "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==");

// If using EMV 3DS (3DS 2.0), please send the activity in the constructor,
Xendit xendit = new Xendit(getApplicationContext(), "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==", thisActivity);
```

### Creating a single-use token

This function accepts parameters below:

|Parameter|Type|Description|
|---------|----|-----------|
|Card|Card Object|Card data that will be used to create a token|
|amount|String|Amount that will be used to create a token bundled with 3DS authentication|
|shouldAuthenticate|Boolean|A flag indicating if 3DS authentication is required for this token. Will be set to `true` if you omit the parameter|
|onBehalfOf|String| Sub-account business ID for XenPlatform master account who intended to create a token for their sub-account. Will be set to `empty` if you omit the parameter|
|isMultipleUse|Boolean|A flag to identify whether a token will be reusable or just for one-time use. Will be set to `false` if you omit the parameter|
|billingDetails|billingDetails Object|Card holder's billing details|
|customer|customer object|Xendit customer object|
|currency|String|Currency of the transaction that will be submitted for 3DS authentication|
|midLabel|String|*For switcher merchant only* Specific string value which labels any of your Merchant IDs (MID) set up with Xendit. This can be configured in the list of MIDs on your Dashboard settings. (If this is not included in a request, and you have more than 1 MID in your list, the transaction will proceed using your prioritized MID (first MID on your list)).|


```java
// The second (month) parameter needs to be a 2-digit string representing the month (e.g., "01" for January, "12" for December)
CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
Card card = new Card("4000000000001091", "05", "2039", "123", cardHolderData);

xendit.createSingleUseToken(card, 75000, true, "user-id", billingDetails, customer, currency, midLabel, new TokenCallback() {
    @Override
    public void onSuccess(Token token) {
        // Handle successful tokenization
        System.out.println("Token ID: " + token.getId());
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

### Creating a multiple-use token
|Parameter|Type|Description|
|---------|----|-----------|
|Card|Card Object|Card data that will be used to create a token|
|onBehalfOf|String| Sub-account business ID for XenPlatform master account who intended to create a token for their sub-account. Will be set to `empty` if you omit the parameter|
|billingDetails|billingDetails Object|Card holder's billing details|
|customer|customer object|Xendit customer object|
|midLabel|String|*For switcher merchant only* Specific string value which labels any of your Merchant IDs (MID) set up with Xendit. This can be configured in the list of MIDs on your Dashboard settings. (If this is not included in a request, and you have more than 1 MID in your list, the transaction will proceed using your prioritized MID (first MID on your list)).|

```java
CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");
Card card = new Card("4000000000001091", "12", "2039", "123");

xendit.createMultipleUseToken(card, "user-id", billingDetails, customer, midLabel, new TokenCallback() {
    @Override
    public void onSuccess(Token token) {
        // Handle successful tokenization
        System.out.println("Token ID: " + token.getId());
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

### Creating a 3DS authentication
| Parameter      |Type|Description|
|----------------|----|-----------|
| tokenId        |String|a multiple-use token ID that is already created|
| amount         |String|Amount that will be used to create a token bundled with 3DS authentication|
| currency       |String|Currency of the transaction that will be submitted for 3DS authentication|
| cardCvn        |String|Card verification Number collected from card holder|
| cardHolderData |String|Additional information of the card holder data|
| midLabel       |String|*For switcher merchant only* Specific string value which labels any of your Merchant IDs (MID) set up with Xendit. This can be configured in the list of MIDs on your Dashboard settings. (If this is not included in a request, and you have more than 1 MID in your list, the transaction will proceed using your prioritized MID (first MID on your list)).|
| onBehalfOf     |String| Sub-account business ID for XenPlatform master account who intended to create a token for their sub-account. Will be set to `empty` if you omit the parameter|

```java
String tokenId = "sample-token-id";
int amount = 50000;
CardHolderData cardHolderData = new CardHolderData("John", "Doe", "johndoe@example.com", "+628212223242526");

xendit.createAuthentication(tokenId, amount, currency, cardCvn, cardHolderData, "user-id", midLabel, new AuthenticationCallback() {
    @Override
    public void onSuccess(Authentication authentication) {
        // Handle successful authentication
        System.out.println("Authentication ID: " + authentication.getId());
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

## Creating a charge
When you're ready to charge a card, use the private key on your backend to call the charge endpoint. See our API reference at https://xendit.github.io/apireference/#create-charge


## Compatibility with ProGuard

You will need to add the following to your proguard rules file (`proguard-rules.pro`). Else, proguard might affect deserialization of the authentication response body.
```
# xendit
-keep public class com.xendit.** { public *;}
-keep class com.xendit.Models.** { *; }
-keepattributes *Annotation*
-keepattributes LocalVariableTable,LocalVariableTypeTable
```
