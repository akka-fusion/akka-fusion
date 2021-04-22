package com.helloscala.akka.security.oauth.server.crypto.keys

import java.io.StringWriter
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import java.time.Instant
import java.util.UUID

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk._
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemReader
import org.bouncycastle.util.io.pem.PemWriter

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-20 14:06:18
 */
object KeyUtils {
  def writePem(path: Path, typ: String, encoded: Array[Byte]): Unit = {
    val writer = new PemWriter(Files.newBufferedWriter(path))
    writer.writeObject(new PemObject(typ, encoded))
    writer.flush()
    writer.close()
  }

  def readPemEncoded(path: Path): Array[Byte] = {
    val reader = new PemReader(Files.newBufferedReader(path))
    val pem = reader.readPemObject()
    pem.getContent
  }

  def convert(managedKey: ManagedKey): Option[JWK] = {
    managedKey.publicKey.flatMap {
      case publicKey: RSAPublicKey =>
        val rsaKey = new RSAKey.Builder(publicKey)
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.RS256)
          .keyID(managedKey.id)
          .build
        Some(rsaKey)
      case publicKey: ECPublicKey =>
        val curve = Curve.forECParameterSpec(publicKey.getParams)
        val ecKey = new ECKey.Builder(curve, publicKey)
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.ES256)
          .keyID(managedKey.id)
          .build
        Some(ecKey)
      case _ =>
        None
    }
  }

  def generateSecretKey(): SecretKey = {
    try KeyGenerator.getInstance("HmacSha256").generateKey
    catch {
      case ex: Exception =>
        throw new IllegalStateException(ex)
    }
  }

  def generateRsaKey(): KeyPair = {
    try {
      val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
      keyPairGenerator.initialize(2048)
      keyPairGenerator.generateKeyPair
    } catch {
      case ex: Exception =>
        throw new IllegalStateException(ex)
    }
  }

  def generateEcKey(): KeyPair = {
    val ellipticCurve = new EllipticCurve(
      new ECFieldFp(new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853951")),
      new BigInteger("115792089210356248762697446949407573530086143415290314195533631308867097853948"),
      new BigInteger("41058363725152142129326129780047268409114441015993725554835256314039467401291"))
    val ecPoint = new ECPoint(
      new BigInteger("48439561293906451759052585252797914202762949526041747995844080717082404635286"),
      new BigInteger("36134250956749795798585127919587881956611106672985015071877198253568414405109"))
    val ecParameterSpec = new ECParameterSpec(
      ellipticCurve,
      ecPoint,
      new BigInteger("115792089210356248762697446949407573529996955224135760342422259061068512044369"),
      1)
    try {
      val keyPairGenerator = KeyPairGenerator.getInstance("EC")
      keyPairGenerator.initialize(ecParameterSpec)
      keyPairGenerator.generateKeyPair
    } catch {
      case ex: Exception =>
        throw new IllegalStateException(ex)
    }
  }

  def generateKeys(): Map[String, ManagedKey] = {
    val rsaKeyPair = generateRsaKey()
    val rsaManagedKey =
      ManagedKey("rsa-key", rsaKeyPair.getPrivate, Some(rsaKeyPair.getPublic), Instant.now(), None)
    val hmacKey: SecretKey = generateSecretKey()
    val secretManagedKey: ManagedKey = ManagedKey(UUID.randomUUID().toString, hmacKey, None, Instant.now(), None)
    val ecKeyPair = generateEcKey()
    val ecManagedKey = ManagedKey("ec-key", ecKeyPair.getPrivate, Some(ecKeyPair.getPublic), Instant.now(), None)
    List(rsaManagedKey, secretManagedKey, ecManagedKey).map(mk => mk.id -> mk).toMap
  }
}
