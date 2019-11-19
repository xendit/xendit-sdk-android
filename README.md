# xendit-sdk-android
The Xendit Android SDK makes it easy to collect sensitive credit card information without that data ever having to touch your server.

## Ownership

Team: [TPI](https://www.draw.io/?state=%7B%22ids%22:%5B%221Vk1zqYgX2YqjJYieQ6qDPh0PhB2yAd0j%22%5D,%22action%22:%22open%22,%22userId%22:%22104938211257040552218%22%7D)

Slack Channel: [#integration-product](https://xendit.slack.com/messages/integration-product/)

Slack Mentions: `@troops-tpi`

## Requirements
The Xendit SDK is compatible with Android 2.3.3 and above.

## How to try example
Visit and try the `app` module to see an example of how the SDK works.

1. [Install android studio.](https://developer.android.com/studio/install)
2. [Clone repository.](https://help.github.com/en/github/creating-cloning-and-archiving-repositories/cloning-a-repository)
3. [Get your own API Key.](https://dashboard.xendit.co/settings/security)
4. Change line with your own API Key.
```
PUBLISHABLE_KEY="xnd_public_development_YOURAPIKEY"
```
*Replace YOURAPIKEY with your own API Key.

## Installation
Maven:
```
<dependency>
  <groupId>com.xendit</groupId>
  <artifactId>xendit-android</artifactId>
  <version>1.1.0</version>
  <type>pom</type>
</dependency>
```

Gradle:
```
compile 'com.xendit:xendit-android:1.1.0'
```

Ivy:
```
<dependency org='com.xendit' name='xendit-android' rev='1.1.0'>
  <artifact name='xendit-android' ext='pom' ></artifact>
</dependency>
```

For more information, visit https://bintray.com/xendit/android/xendit-sdk-android

### Initializing Xendit
```
Xendit xendit = new Xendit(getApplicationContext(), "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==");
```

### Creating a single-use token
```
Card card = new Card("4000000000000002", "12", "2017", "123");

xendit.createSingleUseToken(card, 75000, true, new TokenCallback() {
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

`createSingleUseToken` accept 4 parameters: `Card` object, amount, an optional `shouldAuthenticate` boolean, and a `TokenCallback`. `shouldAuthenticate` will be set to true if you don't pass this value.

### Creating a multiple-use token
```
Card card = new Card("4000000000000002", "12", "2017", "123");

xendit.createMultipleUseToken(card, new TokenCallback() {
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

### Creating a 3ds authentication
```
String tokenId = "sample-token-id";
int amount = 50000;

xendit.createAuthentication(tokenId, amount, new AuthenticationCallback() {
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
