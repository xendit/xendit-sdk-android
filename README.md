# xendit-sdk-android
The Xendit Android SDK makes it easy to collect sensitive credit card information without that data ever having to touch your server.

## Requirements
The Xendit SDK is compatible with Android 2.3.3 and above.

## Installation
Maven:
```
<dependency>
  <groupId>com.xendit</groupId>
  <artifactId>xendit-android</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

Gradle:
```
compile 'com.xendit:xendit-android:1.0.0'
```

Ivy:
```
<dependency org='com.xendit' name='xendit-android' rev='1.0.0'>
  <artifact name='xendit-android' ext='pom' ></artifact>
</dependency>
```

For more information, visit https://bintray.com/xendit/android/xendit-sdk-android

## Examples
Visit and try the `app` module to see an example of how the SDK works. Additionally, we've provided some examples below:

### Initializing Xendit
```
Xendit xendit = new Xendit(getApplicationContext(), "xnd_public_development_O4uGfOR3gbOunJU4frcaHmLCYNLy8oQuknDm+R1r9G3S/b2lBQR+gQ==");
```

### Creating a single use token
```
Card card = new Card("4000000000000002", "12", "2017", "123");

xendit.createToken(card, "75000", new TokenCallback() {
    @Override
    public void onSuccess(Token token) {
        // Handle successful tokenization
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

### Creating a multiple use token
```
Card card = new Card("4000000000000002", "12", "2017", "123");

xendit.createMultipleUseToken(card, new TokenCallback() {
    @Override
    public void onSuccess(Token token) {
        // Handle successful tokenization
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

### Creating a 3ds authentication
```
xendit.createAuthentication("sample-token-id", "123", "75000", new TokenCallback() {
    @Override
    public void onSuccess(Token token) {
        // Handle successful tokenization
    }

    @Override
    public void onError(XenditError xenditError) {
        // Handle error
    }
});
```

## Creating a charge
When you're ready to charge a card, use the private key on your backend to call the charge endpoint. See our API reference at https://xendit.github.io/apireference/#create-charge
