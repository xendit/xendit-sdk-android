# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/Dimon_GDA/Prog/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# xendit
-keepclassmembers,allowoptimization public class com.xendit.Models.* {
    public <methods>;
    public <fields>;
}
-keepattributes *Annotation*
-keepattributes LocalVariableTable,LocalVariableTypeTable

#volley
-keep class com.android.volley.** { *; }
-keep interface com.android.volley.** { *; }
-keep class org.apache.commons.logging.**

-keepattributes EnclosingMethod

# Gson
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Cardinal commerce
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.asn1.pkcs.PrivateKeyInfo
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.asn1.x509.SubjectPublicKeyInfo
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.asn1.ASN1ObjectIdentifier

-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.asymmetric.**

-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.openssl.PEMParser
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.symmetric
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.asymmetric
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.digest
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.keystore
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.drbg
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.symmetric.util.ClassUtil
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.util.AlgorithmProvider
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.jcajce.provider.util.AsymmetricKeyInfoConverter

-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.asn1.PQCObjectIdentifiers
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.mceliece.McElieceCCA2KeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.mceliece.McElieceKeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.newhope.NHKeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.qtesla.QTESLAKeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.rainbow.RainbowKeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.sphincs.Sphincs256KeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.xmss.XMSSKeyFactorySpi
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.pqc.jcajce.provider.xmss.XMSSMTKeyFactorySpi


-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.util.io.pem.PemObject
-keep class com.cardinalcommerce.dependencies.internal.bouncycastle.util.io.pem.PemReader

-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JOSEException
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.EncryptionMethod
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWEAlgorithm
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWECryptoParts
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWEHeader
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWSAlgorithm
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWSObject
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWSVerifier
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.KeyLengthException
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.Payload
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.JWEObject
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.RSAEncrypter

-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.ECDSAVerifier
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.RSASSAVerifier
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.ConcatKDF
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.ECDH
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.AAD
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.AESCBC
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.AESGCM
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.AlgorithmSupportMessage
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.AuthenticatedCipherText
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.impl.DeflateHelper
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.DirectEncrypter
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.crypto.DirectDecrypter

-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.util.Base64URL
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.util.Base64
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.util.X509CertUtils
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.util.ByteUtils
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.util.Container

-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.jwk.Curve
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.jwk.ECKey
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.jwk.JWK
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jose.jwk.PEMEncodedKeyParser

-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jwt.EncryptedJWT
-keep class com.cardinalcommerce.dependencies.internal.nimbusds.jwt.JWTClaimsSet
-keep class com.cardinalcommerce.dependencies.internal.minidev.asm.FieldFilter

-ignorewarnings
-keep class com.cardinalcommerce.dependencies.internal.minidev.json.* {
    public private *;
}
