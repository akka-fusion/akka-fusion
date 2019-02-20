package helloscala.common.util

import java.math.{BigInteger, BigDecimal => JBigDecimal}

object NumberUtils {
  private val LONG_MIN = BigInteger.valueOf(Long.MinValue)

  private val LONG_MAX = BigInteger.valueOf(Long.MaxValue)

  /**
   * Convert the given number into an instance of the given target class.
   *
   * @param number      the number to convert
   * @param targetClass the target class to convert to
   * @return the converted number
   * @throws IllegalArgumentException if the target class is not supported
   *                                  (i.e. not a standard Number subclass as included in the JDK)
   * @see Byte
   * @see Short
   * @see Integer
   * @see Long
   * @see BigInteger
   * @see Float
   * @see Double
   * @see BigDecimal
   */ @SuppressWarnings(Array("unchecked")) @throws[IllegalArgumentException]
  def convertNumberToTargetClass[T <: Number](number: Number, targetClass: Class[T]): T = {
    require(number ne null, "Number must not be null")
    require(targetClass ne null, "Target class must not be null")
    if (targetClass.isInstance(number)) {
      return number.asInstanceOf[T]
    } else {
      if (classOf[Byte] eq targetClass) {
        val value: Long = checkedLongValue(number, targetClass)
        if (value < Byte.MinValue || value > Byte.MaxValue) {
          raiseOverflowException(number, targetClass)
        }
        return java.lang.Byte.valueOf(number.byteValue).asInstanceOf[T]
      } else {
        if (classOf[Short] eq targetClass) {
          val value: Long = checkedLongValue(number, targetClass)
          if (value < Short.MinValue || value > Short.MaxValue) {
            raiseOverflowException(number, targetClass)
          }
          return java.lang.Short.valueOf(number.shortValue).asInstanceOf[T]
        } else {
          if (classOf[Integer] eq targetClass) {
            val value: Long = checkedLongValue(number, targetClass)
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) { raiseOverflowException(number, targetClass) }
            return Integer.valueOf(number.intValue).asInstanceOf[T]
          } else {
            if (classOf[Long] eq targetClass) {
              val value: Long = checkedLongValue(number, targetClass)
              return java.lang.Long.valueOf(value).asInstanceOf[T]
            } else {
              if (classOf[BigInteger] eq targetClass) {
                if (number.isInstanceOf[JBigDecimal]) { // do not lose precision - use BigDecimal's own conversion
                  return (number.asInstanceOf[JBigDecimal]).toBigInteger.asInstanceOf[T]
                } else { // original value is not a Big* number - use standard long conversion
                  return BigInteger.valueOf(number.longValue).asInstanceOf[T]
                }
              } else {
                if (classOf[Float] eq targetClass) { return java.lang.Float.valueOf(number.floatValue).asInstanceOf[T] } else {
                  if (classOf[Double] eq targetClass) {
                    return java.lang.Double.valueOf(number.doubleValue).asInstanceOf[T]
                  } else {
                    if (classOf[JBigDecimal] eq targetClass) { // always use BigDecimal(String) here to avoid unpredictability of BigDecimal(double)
                      // (see BigDecimal javadoc for details)
                      return new JBigDecimal(number.toString).asInstanceOf[T]
                    } else {
                      throw new IllegalArgumentException(
                        "Could not convert number [" + number + "] of type [" + number.getClass.getName + "] to unsupported target class [" + targetClass.getName + "]")
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Check for a {@code BigInteger}/{@code BigDecimal} long overflow
   * before returning the given number as a long value.
   *
   * @param number      the number to convert
   * @param targetClass the target class to convert to
   * @return the long value, if convertible without overflow
   * @throws IllegalArgumentException if there is an overflow
   * @see #raiseOverflowException
   */
  private def checkedLongValue(number: Number, targetClass: Class[_ <: Number]): Long = {
    var bigInt: BigInteger = null
    number match {
      case integer: BigInteger  => bigInt = integer
      case decimal: JBigDecimal => bigInt = decimal.toBigInteger
      case _                    => // do nothing
    }

    // Effectively analogous to JDK 8's BigInteger.longValueExact()
    if (bigInt != null && (bigInt.compareTo(LONG_MIN) < 0 || bigInt.compareTo(LONG_MAX) > 0)) {
      raiseOverflowException(number, targetClass)
    }

    number.longValue
  }

  /**
   * Raise an <em>overflow</em> exception for the given number and target class.
   *
   * @param number      the number we tried to convert
   * @param targetClass the target class we tried to convert to
   * @throws IllegalArgumentException if there is an overflow
   */
  private def raiseOverflowException(number: Number, targetClass: Class[_]): Unit = {
    throw new IllegalArgumentException(
      "Could not convert number [" + number + "] of type [" + number.getClass.getName + "] to target class [" + targetClass.getName + "]: overflow")
  }

}
