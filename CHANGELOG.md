# CHANGELOG

## 4.2.3 (2025-01-24)
- Fix: Handle 3DS authentication edge case and version tracking

## 4.2.2 (2024-11-21)
- Fixed app crash related to unregistered AuthenticationBroadcastReceiver by implementing proper lifecycle-aware receiver management

## 4.2.1 (2024-11-20)
- Enhance: Moved finish() call to Handler to ensure proper UI thread execution and prevent memory leaks
- Fix: Potential race conditions when closing activities with pending operations

## 4.2.0 (2024-08-06)
- Feat: Added card holder information in create token and create authentication

## 4.1.2 (2024-05-27)
- Fix JCB card detection and expiry date

## 4.1.1 (20240-01-26)
- Support mid_label in create token and unbundled create authentication

## 4.1.0 (2024-01-25)
- Fix package corruption issues on 4.0.0.


## 4.0.0 (2024-01-22) - Package Corrupted. Use latest version instead
- Update to use Andriod API Version 34 (Android 14). This package is corrupted

## 3.8.5 (2023-07-04)
- Chore: Upgrade sentry-android to 6.24.0

## 3.8.4 (2023-05-03)
- Bug Fix: Fix Wrong Casting Bug

## 3.8.3 (2023-05-03)
- Bug Fix: Redeploy

## [Broken]3.8.2 (2023-05-03)
- Bug Fix: Handle External Event for 3DS

## 3.8.1 (2023-03-17)
- Chore: Upgrade sentry-android to 6.15.0
- Chore: Update sentry dsn

## 3.8.0 (2023-03-13)
- Feat: Added storeCVN method for caching cvn on subsequent multi-use token purpose
- Feat: Added storeCVN test page
- Bug: Bug fix for validation on createAuthentication method
- Chore: Bump up API version from 28 to 30.

## NOTE: Github tag from 3.4.2 - 3.7.0 is being skipped due to inconsistency github release tag version with sdk version. Starting on 3.8.0 github release tag and xendit-android-sdk package version are in sync.

## 3.4.2 (2021-09-15)
- Updated Cardinal Commerce Library credentials


## 3.3.0 (2021-06-10)

Features:

- Return `failure_reason` for FAILED 3DS

Fixes:

- Improve 3ds handling

## 3.0.0 (2021-02-17)

Features:

- Support multiple currencies for tokenization and authentication

Fixes:

- Updated snowplow to 1.4.2

## 3.0.0-beta (2020-12-16)

Features:

- Tokenization and authentication returns card details such as issuing bank, card type, card scheme (brand) and card art.
- Support EMV 3DS (3DS 2.0)

## 2.3.0 (2020-03-30)

Features:

- Add `onBehalfOf` (string) enable to create tokenization on master account for sub account

## 2.2.0 (2019-11-19)

Features:

- Add `should_3ds` (boolean) field in multi use or skip authentication tokenization result, accessible using `.getShould_3DS`
