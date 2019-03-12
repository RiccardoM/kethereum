package org.kethereum.erc55

import kotlinx.io.core.toByteArray
import org.kethereum.functions.isValid
import org.kethereum.keccakshortcut.keccak
import org.kethereum.model.Address
import org.kethereum.model.extensions.toNoPrefixHexString

/*
ERC-55 Checksum as in https://github.com/ethereum/EIPs/blob/master/EIPS/eip-55.md
 */

fun Address.withERC55Checksum() = cleanHex.toLowerCase().toByteArray().keccak().toNoPrefixHexString().let { hexHash ->
    Address(cleanHex.mapIndexed { index, hexChar ->
        when {
            hexChar in '0'..'9' -> hexChar
            hexHash[index] in '0'..'7' -> hexChar.toLowerCase()
            else -> hexChar.toUpperCase()
        }
    }.joinToString(""))
}

private fun Address.hasValidERC55ChecksumAssumingValidAddress() = withERC55Checksum().hex == hex

fun Address.hasValidERC55Checksum() = isValid() && hasValidERC55ChecksumAssumingValidAddress()
fun Address.hasValidERC55ChecksumOrNoChecksum() = isValid() &&
        (hasValidERC55ChecksumAssumingValidAddress() ||
                cleanHex.toLowerCase() == cleanHex ||
                cleanHex.toUpperCase() == cleanHex)
