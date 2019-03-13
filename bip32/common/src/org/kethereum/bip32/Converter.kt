package org.kethereum.bip32

import kotlinx.io.ByteBuffer
import kotlinx.io.ByteOrder
import org.kethereum.bip32.model.*
import org.kethereum.crypto.CURVE
import org.kethereum.crypto.CryptoAPI
import org.kethereum.crypto.decompressKey
import org.kethereum.crypto.toECKeyPair
import org.kethereum.encodings.decodeBase58WithChecksum
import org.kethereum.model.ECKeyPair
import org.kethereum.model.PRIVATE_KEY_SIZE
import org.kethereum.model.PrivateKey
import org.kethereum.model.PublicKey
import org.kethereum.model.exceptions.InvalidKeyException
import org.kethereum.model.exceptions.KeyException
import org.kethereum.model.exceptions.NoSuchAlgorithmException
import org.kethereum.model.exceptions.NoSuchProviderException
import org.kethereum.model.number.BigInteger

fun Seed.toExtendedKey(publicKeyOnly: Boolean = false): ExtendedKey {
    try {
        val lr = CryptoAPI.hmac.init(BITCOIN_SEED).generate(seed)
        val l = lr.copyOfRange(0, PRIVATE_KEY_SIZE)
        val r = lr.copyOfRange(PRIVATE_KEY_SIZE, PRIVATE_KEY_SIZE + CHAINCODE_SIZE)
        val m = BigInteger(1, l)
        if (m >= CURVE.n) {
            throw KeyException("Master key creation resulted in a key with higher modulus. Suggest deriving the next increment.")
        }
        val keyPair = PrivateKey(l).toECKeyPair()
        return if (publicKeyOnly) {
            val pubKeyPair = ECKeyPair(PrivateKey(BigInteger.ZERO), keyPair.publicKey)
            ExtendedKey(pubKeyPair, r, 0, 0, 0)
        } else {
            ExtendedKey(keyPair, r, 0, 0, 0)
        }
    } catch (e: NoSuchAlgorithmException) {
        throw KeyException(e)
    } catch (e: NoSuchProviderException) {
        throw KeyException(e)
    } catch (e: InvalidKeyException) {
        throw KeyException(e)
    }

}


fun XPriv.toExtendedKey(): ExtendedKey {
    val data = xPriv.decodeBase58WithChecksum()
    if (data.size != EXTENDED_KEY_SIZE) {
        throw KeyException("invalid extended key")
    }

    val buff = ByteBuffer.allocate(data.size)
        .put(data)
        .order(ByteOrder.BIG_ENDIAN)
        .flip()

    val type = ByteArray(4)

    buff.get(type, 0, type.size)

    val hasPrivate = when {
        type.contentEquals(xprv) -> true
        type.contentEquals(xpub) -> false
        else -> throw KeyException("invalid magic number for an extended key")
    }

    val depth = buff.get()
    val parent = buff.getInt()
    val sequence = buff.getInt()

    val chainCode = ByteArray(PRIVATE_KEY_SIZE)
    buff.get(chainCode, 0, chainCode.size)

    val keyPair = if (hasPrivate) {
        buff.get() // ignore the leading 0
        val privateBytes = ByteArray(PRIVATE_KEY_SIZE)
        buff.get(privateBytes, 0, privateBytes.size)
        PrivateKey(privateBytes).toECKeyPair()
    } else {
        val compressedPublicBytes = ByteArray(COMPRESSED_PUBLIC_KEY_SIZE)
        buff.get(compressedPublicBytes, 0, compressedPublicBytes.size)
        val uncompressedPublicBytes = decompressKey(compressedPublicBytes)
        ECKeyPair(
            PrivateKey(BigInteger.ZERO),
            PublicKey(BigInteger(1, uncompressedPublicBytes))
        )
    }
    return ExtendedKey(keyPair, chainCode, depth, parent, sequence)
}