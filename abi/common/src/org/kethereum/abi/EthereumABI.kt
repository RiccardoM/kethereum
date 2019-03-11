package org.kethereum.abi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.kethereum.abi.model.EthereumFunction

private fun parse(abi: String, moshi: Moshi) = createAdapter(moshi).fromJson(abi) ?: emptyList()

private fun createAdapter(moshi: Moshi): JsonAdapter<List<EthereumFunction>> {
    val listMyData = Types.newParameterizedType(List::class.java, EthereumFunction::class.java)
    return moshi.adapter(listMyData)
}

class EthereumABI(val methodList: List<EthereumFunction>) {

    constructor(abiString: String, moshi: Moshi) : this(parse(abiString, moshi))

}


fun EthereumABI.toFunctionSignaturesString() = methodList.filter { it.name != null }.joinToString("\n") { function ->
    function.name + "(" + function.inputs?.joinToString(",") { it.type + " " + it.name } + ")"
}