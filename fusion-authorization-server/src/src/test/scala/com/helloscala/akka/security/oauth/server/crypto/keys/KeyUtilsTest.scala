package com.helloscala.akka.security.oauth.server.crypto.keys

import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.Security
import java.security.interfaces.ECKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.io.pem.PemReader
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-09-20 18:29:31
 */
class KeyUtilsTest extends AnyWordSpec with Matchers with BeforeAndAfterAll {

  "KeyUtilsTest" should {
    "RSA key pair" in {
      val kp = KeyUtils.generateRsaKey()
      val jwk = new RSAKey.Builder(kp.getPublic.asInstanceOf[RSAPublicKey])
        .privateKey(kp.getPrivate)
        .keyUse(KeyUse.SIGNATURE)
        .algorithm(JWSAlgorithm.RS256)
        .build();
      val text = jwk.toString
      println(text)

      val rsaKey = RSAKey.parse(text)
      println(kp.getPrivate.equals(rsaKey.toPrivateKey))
      println(kp.getPublic.equals(rsaKey.toPublicKey))

      KeyUtils.writePem(Paths.get("/tmp/pem.pub"), "PUBLIC KEY", kp.getPublic.getEncoded)
      KeyUtils.writePem(Paths.get("/tmp/pem.key"), "PRIVATE KEY", kp.getPrivate.getEncoded)
      val rsaFactory = KeyFactory.getInstance("RSA")
      val pemPub = rsaFactory.generatePublic(new X509EncodedKeySpec(KeyUtils.readPemEncoded(Paths.get("/tmp/pem.pub"))))
      val pemKey =
        rsaFactory.generatePrivate(new PKCS8EncodedKeySpec(KeyUtils.readPemEncoded(Paths.get("/tmp/pem.key"))))
      println(kp.getPrivate.equals(pemKey))
      println(kp.getPublic.equals(pemPub))
    }

    "Pem reader" in {
      val path = Paths.get("/tmp/pem.pub")
      val reader = new PemReader(Files.newBufferedReader(path))
      val pem = reader.readPemObject()

    }

    "generateRsaKey" in {}

    "generateSecretKey" in {}

    "convert" in {}

    "generateKeys" in {}

    "generateEcKey" in {
      val kp = KeyUtils.generateEcKey()
      val ecPublicKey = kp.getPublic.asInstanceOf[ECPublicKey]
      val jwk = new com.nimbusds.jose.jwk.ECKey.Builder(Curve.forECParameterSpec(ecPublicKey.getParams), ecPublicKey)
        .privateKey(kp.getPrivate)
        .keyUse(KeyUse.SIGNATURE)
        .algorithm(JWSAlgorithm.ES256)
        .build();
      val text = jwk.toString
      println(text)

      val ecKey = com.nimbusds.jose.jwk.ECKey.parse(text)
      println(kp.getPrivate.equals(ecKey.toPrivateKey))
      println(kp.getPublic.equals(ecKey.toPublicKey))

      KeyUtils.writePem(Paths.get("/tmp/ec.pub"), "EC PUBLIC KEY", kp.getPublic.getEncoded)
      KeyUtils.writePem(Paths.get("/tmp/ec.key"), "EC PRIVATE KEY", kp.getPrivate.getEncoded)

      val ecFactory = KeyFactory.getInstance("EC")

      val pemPub = ecFactory.generatePublic(new X509EncodedKeySpec(KeyUtils.readPemEncoded(Paths.get("/tmp/ec.pub"))))
      val pemKey =
        ecFactory.generatePrivate(new PKCS8EncodedKeySpec(KeyUtils.readPemEncoded(Paths.get("/tmp/ec.key"))))
      println(kp.getPrivate.equals(pemKey))
      println(kp.getPublic.equals(pemPub))
    }
  }

  override protected def beforeAll(): Unit = Security.addProvider(new BouncyCastleProvider)
}
