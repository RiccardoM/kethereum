package org.kethereum.model

import org.kethereum.model.extensions.hexToBigInteger
import org.kethereum.model.extensions.toBigInteger
import org.kethereum.model.number.BigInteger

inline class PrivateKey(val key: BigInteger) {
    constructor(privateKey: ByteArray) : this(privateKey.toBigInteger())
    constructor(hex: String) : this(hex.hexToBigInteger())
}

inline class PublicKey(val key: BigInteger) {

    constructor(publicKey: ByteArray) : this(publicKey.toBigInteger())
    constructor(publicKey: String) : this(publicKey.hexToBigInteger())

    override fun toString() = key.toString()
}

data class ECKeyPair(val privateKey: PrivateKey, val publicKey: PublicKey)
