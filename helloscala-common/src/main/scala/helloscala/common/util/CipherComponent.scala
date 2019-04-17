package helloscala.common.util

import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.Security
import java.util

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex

class CipherComponent(key: String) {
  private val TRIPLE_DES_TRANSFORMATION = "DESede/ECB/PKCS7Padding"
  private val ALGORITHM                 = "DESede"
  private val BOUNCY_CASTLE_PROVIDER    = "BC"
  private val KEY_DES_LENGTH            = 24

  Security.addProvider(new BouncyCastleProvider())

  def encrypt(plainText: String, key: String): String = {
    val encryptedByte = encode(plainText.getBytes(StandardCharsets.UTF_8), key)
    Hex.toHexString(encryptedByte)
  }

  def encrypt(plainText: String): String = encrypt(plainText, key)

  def decrypt(cipherText: String, key: String): String = {
    val decryptedByte = decode(Hex.decode(cipherText.getBytes(StandardCharsets.UTF_8)), key)
    new String(decryptedByte)
  }

  def decrypt(cipherText: String): String = decrypt(cipherText, key)

  private def encode(input: Array[Byte], key: String): Array[Byte] =
    try {
      val cipher = Cipher.getInstance(TRIPLE_DES_TRANSFORMATION, BOUNCY_CASTLE_PROVIDER)
      cipher.init(Cipher.ENCRYPT_MODE, buildKey(key.toCharArray))
      cipher.doFinal(input)
    } catch {
      case e @ (_: GeneralSecurityException | _: UnsupportedEncodingException) =>
        throw new RuntimeException(e.getLocalizedMessage, e)
    }

  private def decode(input: Array[Byte], key: String): Array[Byte] =
    try {
      val decrypter = Cipher.getInstance(TRIPLE_DES_TRANSFORMATION, BOUNCY_CASTLE_PROVIDER)
      decrypter.init(Cipher.DECRYPT_MODE, buildKey(key.toCharArray))
      decrypter.doFinal(input)
    } catch {
      case e @ (_: GeneralSecurityException | _: UnsupportedEncodingException) =>
        throw new RuntimeException(e.getLocalizedMessage, e)
    }

  private def buildKey(password: Array[Char]): SecretKeySpec = {
    val digest = DigestUtils.digestSha256()
    digest.update(String.valueOf(password).getBytes(StandardCharsets.UTF_8))
    val keys   = digest.digest
    val keyDes = util.Arrays.copyOf(keys, KEY_DES_LENGTH)
    new SecretKeySpec(keyDes, ALGORITHM)
  }

}
