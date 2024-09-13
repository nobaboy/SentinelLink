package me.nobaboy.sentinellink

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

class SentinelCommand : CommandBase() {
    private fun getHelpMessage(): String {
        return """
            #§7§m-----------------§r§7[ §9Sentinel Link §7]§m-----------------
            # §3§l◆ §3Mod Version: §b${SentinelLink.MOD_VERSION}
            # 
            # §9§l➜ Info:
            #  §3/sentinel (Alias: /sentinellink)
            # §9§l➜ Commands:
            #  §3/sentinel key <key> §7» §rSet your Sentinel API key.
            #  §3/sentinel unlink §7» §rUnlink your account from Sentinel Link.
            #  §3/sentinel reset §7» §rReset your Sentinel Link.
            #§7§m-----------------------------------------------
        """.trimMargin("#")
    }

    private fun setKey(args: Array<String>) {
        if (args.size != 1) {
            SentinelLink.send("Usage: §3/sentinel key <key>")
            SentinelLink.send("Get an API key with §3/newkey §bin the Discord server!")
            return
        }

        val uuid = SentinelLink.mc.thePlayer.uniqueID.toString()
        Config.tokens[uuid] = args[0]
        SentinelLink.saveConfig()
        SentinelLink.send("Successfully set new API key.")
    }

    private fun unlinkUser() {
        val uuid = SentinelLink.mc.thePlayer.uniqueID.toString()
        Config.tokens.remove(uuid)
        SentinelLink.saveConfig()
        SentinelLink.send("Unlinked ${SentinelLink.mc.thePlayer.name} from Sentinel Link.")
    }

    private fun resetLink() {
        Config.tokens.clear()
        SentinelLink.saveConfig()
        SentinelLink.send("Reset complete.")
    }

    override fun getCommandName(): String {
        return "sentinel"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/sentinel <key|unlink,reset"
    }

    override fun processCommand(sender: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            SentinelLink.send(false, getHelpMessage())
            return
        }

        when (args[0].lowercase()) {
            "key" -> setKey(args.copyOfRange(1, args.size))
            "unlink" -> unlinkUser()
            "reset" -> resetLink()
            else -> SentinelLink.send(false, getHelpMessage())
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender): Boolean = true

    override fun getCommandAliases(): List<String> = listOf("sentinellink")

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos
    ): List<String> {
        return when (args.size) {
            1 -> {
                val commands = mutableListOf<String>("key", "unlink", "reset")
                getListOfStringsMatchingLastWord(args, commands)
            }
            else -> emptyList<String>()
        }
    }
}