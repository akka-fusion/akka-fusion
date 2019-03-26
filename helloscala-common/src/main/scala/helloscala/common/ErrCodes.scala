package helloscala.common

object ErrCodes {
  val ERROR: Int       = -1
  val SUCCESS          = 0
  val OK               = 200
  val CREATE           = 201
  val ACCEPTED         = 202
  val BAD_REQUEST      = 400
  val UNAUTHORIZED     = 401
  val NO_CONTENT       = 402
  val FORBIDDEN        = 403
  val NOT_FOUND        = 404
  val NOT_FOUND_CONFIG = 404001
  val CONFLICT         = 409
  val INTERNAL_ERROR   = 500
  val NOT_IMPLEMENTED  = 501
}
