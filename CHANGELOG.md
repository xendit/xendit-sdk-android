# CHANGELOG

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