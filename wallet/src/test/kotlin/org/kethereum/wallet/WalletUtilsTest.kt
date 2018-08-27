package org.kethereum.wallet

import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.kethereum.crypto.ECKeyPair
import org.kethereum.extensions.hexToBigInteger
import org.kethereum.wallet.data.KEY_PAIR
import org.kethereum.wallet.data.PASSWORD
import org.kethereum.wallet.model.UnsupportedWalletVersionException
import java.io.File
import java.nio.file.Files
import kotlin.test.assertFailsWith

private fun loadFile(name: String) = File(WalletUtilsTest::class.java.getResource("/keyfiles/$name").file)

class WalletUtilsTest {

    private val tempDir by lazy { Files.createTempDirectory(WalletUtilsTest::class.java.simpleName + "-testkeys").toFile() }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testGenerateFullWalletFile() {
        val tested = KEY_PAIR.generateWalletFile(PASSWORD, tempDir, STANDARD_SCRYPT_CONFIG)

        assertThat(tested.file.loadKeysFromWalletFile(PASSWORD)).isEqualTo(KEY_PAIR)

        assertThat(tested.wallet.decrypt(PASSWORD)).isEqualTo(KEY_PAIR)
    }

    @Test
    fun testGenerateLightWalletFile() {

        val tested = KEY_PAIR.generateWalletFile(PASSWORD, tempDir, LIGHT_SCRYPT_CONFIG)

        assertThat(tested.file.loadKeysFromWalletFile(PASSWORD)).isEqualTo(KEY_PAIR)

        assertThat(tested.wallet.decrypt(PASSWORD)).isEqualTo(KEY_PAIR)
    }


    @Test
    fun testLoadCredentialsFromFile() {
        val file = loadFile("UTC--2016-11-03T05-55-06.340672473Z--ef678007d18427e6022059dbc264f27507cd1ffc")
        val keyPair = file.loadKeysFromWalletFile(PASSWORD)

        assertThat(keyPair).isEqualTo(KEY_PAIR)
    }

    @Test
    fun testLoadCredentialsMyEtherWallet() {
        val file = loadFile("UTC--2016-11-03T07-47-45.988Z--4f9c1a1efaa7d81ba1cabf07f2c3a5ac5cf4f818")
        val keyPair = file.loadKeysFromWalletFile(PASSWORD)

        assertThat(keyPair).isEqualTo(ECKeyPair.create("6ca4203d715e693279d6cd9742ad2fb7a3f6f4abe27a64da92e0a70ae5d859c9".hexToBigInteger()))
    }

    @Test
    fun testLoadV4Wallet() {
        val file = loadFile("UTC--2016-11-03T07-47-45.988Z--V4")
        val keyPair = file.loadKeysFromWalletFile(PASSWORD)

        assertThat(keyPair).isEqualTo(ECKeyPair.create("6ca4203d715e693279d6cd9742ad2fb7a3f6f4abe27a64da92e0a70ae5d859c9".hexToBigInteger()))
    }


    @Test
    fun testLoadFailsForV1Wallet() {
        val file = loadFile("UTC--2016-11-03T07-47-42.988Z--V1")
        assertFailsWith<UnsupportedWalletVersionException> {
            file.loadKeysFromWalletFile(PASSWORD)
        }
    }

    @Test
    fun testLoadFailsForV2Wallet() {
        val file = loadFile("UTC--2016-11-03T07-47-42.988Z--V2")
        assertFailsWith<UnsupportedWalletVersionException> {
            file.loadKeysFromWalletFile(PASSWORD)
        }
    }

    @Test
    fun testLoadFailsForV5Wallet() {
        val file = loadFile("UTC--2016-11-03T07-47-45.988Z--V5")
        assertFailsWith<UnsupportedWalletVersionException> {
            file.loadKeysFromWalletFile(PASSWORD)
        }

    }
}