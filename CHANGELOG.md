# CHANGELOG

## 3.8.0 (2023-03-13)
- Feat: Added storeCVN method for caching cvn on subsequent multi-use token purpose
- Feat: Added storeCVN test page
- Bug: Bug fix for validation on createAuthentication method

## NOTE: 3.4.2 - 3.7.0 is being skipped due to inconsistency github release tag version with sdk version. Starting on 3.8.0 these 2 version are sync.

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