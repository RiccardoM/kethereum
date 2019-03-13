package org.kethereum.crypto.api.ec

import org.kethereum.model.number.BigInteger

interface CurvePoint {
    val x: BigInteger
    val y: BigInteger

    fun mul(n: BigInteger): CurvePoint
    fun add(p: CurvePoint): CurvePoint
    fun normalize(): CurvePoint
    fun isInfinity(): Boolean
    fun encoded(compressed: Boolean = false): ByteArray
}