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
  <version>3.6.1</version>
  <type>pom</type>
</dependency>
```

Gradle:
```
compile 'com.xendit:xendit-android:3.6.1'
```

Ivy:
```
<dependency org='com.xendit' name='xendit-android' rev='3.6.1'>
  <artifact name='xendit-android' ext='pom' ></artifact>
</dependency>
```

For more information, visit https://bintray.com/xendit/android/xendit-sdk-android

Note: For version 3.0.0 and above, you will need to include cardinal commerce repository credentials to download libraries required for EMV 3DS. Please configure the crendentials below to download the cardinal commerce SDK.

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
    implementation 'org.jfrog.cardinalcommerce.gradle:cardinalmobilesdk:2.2.4-1'
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
```
Card card = new Card("4000000000001091", "12", "2017", "123");

xendit.createSingleUseToken(card, 75000, true, "user-id", new TokenCallback() {
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
`createSingleUseToken` accept 5 parameters: `Card` object, amount, optional `shouldAuthenticate` (boolean), optional `onBehalfOf` (string), and a `TokenCallback`. `shouldAuthenticate` will be set to true and `onBehalfOf` will be set to empty if you omit these parameters.

### Creating a multiple-use token
```
Card card = new Card("4000000000001091", "12", "2017", "123");

xendit.createMultipleUseToken(card, "user-id", new TokenCallback() {
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
`createMultipleUseToken` accept 3 parameters: `Card` object, optional `onBehalfOf` (string), and a `TokenCallback`. `onBehalfOf` will be set to empty if you omit this parameter.

### Creating a 3DS authentication
```
String tokenId = "sample-token-id";
int amount = 50000;

xendit.createAuthentication(tokenId, amount, "user-id", new AuthenticationCallback() {
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

`createAuthentication` accept 4 parameters: tokenId, amount, optional `onBehalfOf` (string), and an `AuthenticationCallback`. `onBehalfOf` will be set to empty if you omit it, but is required when you passed it during `createSingleUseToken` or `createMultipleUseToken`.

## Creating a charge
When you're ready to charge a card, use the private key on your backend to call the charge endpoint. See our API reference at https://xendit.github.io/apireference/#create-charge


## Compability with ProGuard

You will need to add the following to your proguard rules file (`proguard-rules.pro`). Else, proguard might affect deserialization of the authentication response body.
```
# xendit
-keep public class com.xendit.** { public *;}
-keep class com.xendit.Models.** { *; }
-keepattributes *Annotation*
-keepattributes LocalVariableTable,LocalVariableTypeTable
```
