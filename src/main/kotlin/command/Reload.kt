package top.e404.viewslimechunk.command

import org.bukkit.command.*
import top.e404.viewslimechunk.PL
import top.e404.viewslimechunk.config.Config
import top.e404.viewslimechunk.config.Lang

object Reload : CommandExecutor, TabCompleter {
    fun register() {
        // 使用 Paper 插件的程序化命令注册
        val command = object : org.bukkit.command.Command("slimereload") {
            init {
                description = "重载插件配置"
                usage = "/slimereload"
                permission = "viewslimechunk.admin"
                aliases = listOf("sreload")
            }
            
            override fun execute(sender: CommandSender, commandLabel: String, args: Array<out String>): Boolean {
                return onCommand(sender, this, commandLabel, args)
            }
            
            override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>): List<String> {
                return onTabComplete(sender, this, alias, args)
            }
        }
        
        PL.server.commandMap.register("viewslimechunk", command)
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String> = emptyList()

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): Boolean {
        if (!PL.hasPerm(sender, "viewslimechunk.admin", true)) return true
        // 使用Paper/Folia兼容的异步调度器
        PL.runAsync(Runnable {
            Lang.load()
            Config.load()
            PL.sendMsgWithPrefix(sender, Lang["command.reload"])
        })
        return true
    }
}