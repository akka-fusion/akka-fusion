package com.helloscala.akka.security.oauth.server.authentication.client

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 17:18:13
 */
case class RegisteredClient(
    id: String,
    clientId: String,
    clientSecret: String,
    redirectUris: Set[String],
    scopes: Set[String],
    /**
     * Crypto key, see [[com.helloscala.akka.security.oauth.server.crypto.keys.ManagedKey.id]]
     */
    keyId: String)
