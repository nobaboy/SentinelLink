@file:Suppress("unused")

package me.nobaboy.sentinellink

import kotlinx.coroutines.*
import me.nobaboy.sentinellink.Connection.attemptAcknowledge
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds

@Mod(modid = "sentinellink", useMetadata = true, clientSideOnly = true)
class SentinelLink {
    companion object {
        private const val MOD_PREFIX = "§3§lSentinel > §b"
        val MOD_VERSION: String by lazy {
            Loader.instance().activeModContainer().version
        }

        val LOGGER: Logger = LogManager.getLogger(SentinelLink)

        @JvmStatic
        val mc: Minecraft by lazy {
            Minecraft.getMinecraft()
        }

        val modDir by lazy {
            File(mc.mcDataDir, "config").also {
                it.mkdirs()
            }
        }

        private val supervisorJob = SupervisorJob()
        val coroutineScope = CoroutineScope(
            CoroutineName("SentinelLink") + supervisorJob
        )

        fun send(prefix: Boolean, message: String) {
            val usePrefix = if (prefix) MOD_PREFIX else ""
            mc.thePlayer.addChatMessage(ChatComponentText(usePrefix + message))
        }

        fun send(message: String) {
            send(true, message)
        }

        fun saveConfig() {
            try {
                Config.save()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
    }

    private var task: Job? = null

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)
        try {
            Config.load()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Mod.EventHandler
    fun loadComplete(event: FMLLoadCompleteEvent) {
        ClientCommandHandler.instance.registerCommand(SentinelCommand())
    }

    private fun getFirstUsageMessage(): String {
        return """
            #§7§m-----------------§r§7[ §3Sentinel Link §7]§m-----------------
            # §bIt appears this is your first time using the mod.
            # §bTo get an API key, head over to the Discord server
            # §band run §3/newkey §bto receive a new key.
            # §bThen in-game, use §3/sentinel key <key> §band paste the
            # §bkey obtained from Discord, replacing §3<key> §bwith
            # §byour actual key.
            #§7§m-----------------------------------------------
        """.trimMargin("#")
    }

    @SubscribeEvent
    fun onConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        val onHypixel = mc.runCatching {
            !event.isLocal && (thePlayer?.clientBrand?.lowercase()?.contains("hypixel")
                ?: currentServerData?.serverIP?.lowercase()?.contains("hypixel") ?: false)
        }.onFailure { it.printStackTrace() }.getOrDefault(false)
        if (!onHypixel) return

        task = coroutineScope.launch {
            while (mc.thePlayer == null) {
                delay(50.milliseconds)
            }

            if (Config.firstUsage == true) {
                send(false, getFirstUsageMessage())
                Config.firstUsage = false
                saveConfig()
                return@launch
            }

            val uuid = mc.thePlayer.uniqueID.toString()
            val token = Config.tokens[uuid]
            attemptAcknowledge(uuid, token)
        }
    }

    @SubscribeEvent
    fun onDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        task?.cancel()
    }
}
