package me.nobaboy.sentinellink

import me.nobaboy.sentinellink.SentinelLink.Companion.MOD_VERSION
import me.nobaboy.sentinellink.SentinelLink.Companion.mc
import me.nobaboy.sentinellink.SentinelLink.Companion.saveConfig
import me.nobaboy.sentinellink.SentinelLink.Companion.send
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting

class SentinelCommand : CommandBase() {
    override fun getCommandName(): String {
        return "sentinel"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/sentinel <key|unlink,reset"
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            send(false, getHelpMessage())
            return
        }

        when (args[0].lowercase()) {
            "key" -> setKey(args.copyOfRange(1, args.size))
            "unlink" -> unlinkUser()
            "reset" -> resetLink()
            else -> send(false, getHelpMessage())
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    override fun getCommandAliases(): List<String> = listOf("sentinellink")

    private fun getHelpMessage(): String {
        return """
            #§7§m-----------------§r§7[ §9Sentinel Link §7]§m-----------------
            # §3§l◆ §3Mod Version: §b" + $MOD_VERSION
            # §3/sentinel (Alias: /sentinellink)
            # 
            # §3/sentinel key <key> §7» §rSet your Sentinel API key.
            # §3/sentinel unlink §7» §rUnlink your account from Sentinel Link.
            # §3/sentinel reset §7» §rReset your Sentinel Link.
            #§7§m-----------------------------------------------
        """.trimMargin("#")
    }

    private fun setKey(args: Array<String>) {
        if (args.size != 1) {
            send("Usage: ${EnumChatFormatting.DARK_AQUA}/sentinel key <key>")
            send("Get an API key with /apikey in the Discord server!")
            return
        }

        val uuid = mc.thePlayer.uniqueID.toString()
        Config.tokens[uuid] = args[0]
        saveConfig()
        send("Successfully set new API key.")
    }

    private fun unlinkUser() {
        val uuid = mc.thePlayer.uniqueID.toString()
        Config.tokens.remove(uuid)
        saveConfig()
    }

    private fun resetLink() {
        Config.tokens.clear()
        saveConfig()
    }
}