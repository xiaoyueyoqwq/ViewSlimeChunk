package top.e404.viewslimechunk.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.command.*
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import top.e404.viewslimechunk.PL
import top.e404.viewslimechunk.config.Config
import top.e404.viewslimechunk.config.Lang

object Slime : CommandExecutor, TabCompleter {
    fun register() {
        // 使用 Paper 插件的程序化命令注册
        val command = object : org.bukkit.command.Command("slime") {
            init {
                description = "查看周围史莱姆区块"
                usage = "/slime"
                permission = "viewslimechunk.use"
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
        if (!PL.isPlayer(sender, true)) return true
        sender as Player
        if (!PL.hasPerm(sender, "viewslimechunk.use", true)) return true
        if (sender.world.name in Config.disable) {
            PL.sendMsgWithPrefix(sender, Lang["message.invalid_world"])
            return true
        }
        sendSlimeChunk(sender)
        return true
    }

    private fun sendSlimeChunk(p: Player) {
        val chunk = p.location.chunk
        val chunkX = chunk.x
        val chunkZ = chunk.z
        val components = mutableListOf<Component>()
        
        // 添加调试信息
        PL.logger.info("Generating slime chunk map for player ${p.name} at chunk ($chunkX, $chunkZ)")
        PL.logger.info("Range: ${Config.range}, Char: '${Lang.char}'")
        
        for (z in chunkZ - Config.range..chunkZ + Config.range) {
            for (x in chunkX - Config.range..chunkX + Config.range) {
                val c = p.world.getChunkAt(x, z)
                val slime = c.isSlimeChunk
                val b = Lang[if (slime) "hover.is" else "hover.is_not"]
                val cx = x * 16 + 8
                val cz = z * 16 + 8
                val color: String
                val hover: String
                if (x == chunkX && z == chunkZ) {
                    color = if (slime) "color.center.slime" else "color.center.normal"
                    hover = "hover.center"
                } else {
                    color = if (slime) "color.slime" else "color.normal"
                    hover = "hover.normal"
                }
                
                val coloredText = "${Lang[color]}${Lang.char}"
                val hoverText = Lang[hover, "x" to x, "z" to z, "cx" to cx, "cz" to cz, "b" to b]
                
                // 调试输出
                if (x == chunkX && z == chunkZ) {
                    PL.logger.info("Center chunk: coloredText='$coloredText', slime=$slime")
                }
                
                val component = LegacyComponentSerializer.legacyAmpersand().deserialize(coloredText)
                    .hoverEvent(HoverEvent.showText(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(hoverText)
                    ))
                    
                components.add(component)
            }
            components.add(Component.newline())
        }
        
        // 正确构建最终组件
        val builder = Component.text()
        for (component in components) {
            builder.append(component)
        }
        p.sendMessage(builder.build())
    }
}