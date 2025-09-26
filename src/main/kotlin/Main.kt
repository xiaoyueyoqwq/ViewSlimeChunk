package top.e404.viewslimechunk

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bstats.bukkit.Metrics
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import top.e404.viewslimechunk.command.Reload
import top.e404.viewslimechunk.command.Slime
import top.e404.viewslimechunk.config.Config
import top.e404.viewslimechunk.config.Lang
import top.e404.viewslimechunk.update.Updater

class Main : JavaPlugin() {
    companion object {
        lateinit var instance: Main
    }

    private var metrics: Metrics? = null
    
    val prefix: String
        get() = Lang["prefix"]

    override fun onEnable() {
        instance = this
        
        // 初始化配置和语言文件 (顺序很重要)
        try {
            Config.load()
            logger.info("Config loaded successfully")
        } catch (e: Exception) {
            logger.severe("Failed to load config: ${e.message}")
        }
        
        try {
            Lang.load()
            logger.info("Language file loaded successfully")
        } catch (e: Exception) {
            logger.severe("Failed to load language file: ${e.message}")
        }
        
        // 注册命令
        Slime.register()
        Reload.register()
        
        // 初始化更新检查器
        Updater.init()
        
        // 初始化 bStats
        initBStats()
        
        sendConsoleMessage("&f加载完成, 作者&b404E")
    }

    override fun onDisable() {
        sendConsoleMessage("&f卸载完成, 作者&b404E")
    }
    
    private fun initBStats() {
        try {
            // 创建一个自定义的bStats配置，禁用重定位检查
            val constructor = Metrics::class.java.getDeclaredConstructor(
                org.bukkit.plugin.java.JavaPlugin::class.java, 
                Int::class.java,
                Boolean::class.java
            )
            constructor.isAccessible = true
            metrics = constructor.newInstance(this, 15069, false) as Metrics
            logger.info("bStats initialized successfully")
        } catch (e: NoSuchMethodException) {
            // 回退到标准构造器
            try {
                metrics = Metrics(this, 15069)
                logger.info("bStats initialized with standard constructor")
            } catch (e2: Exception) {
                logger.warning("Failed to initialize bStats: ${e2.message}")
            }
        } catch (e: Exception) {
            logger.warning("Failed to initialize bStats: ${e.message}")
        }
    }
    
    fun sendConsoleMessage(message: String) {
        val component = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
        server.consoleSender.sendMessage(component)
    }
    
    fun sendMsgWithPrefix(sender: CommandSender, message: String) {
        val fullMessage = "$prefix $message"
        val component = LegacyComponentSerializer.legacyAmpersand().deserialize(fullMessage)
        sender.sendMessage(component)
    }
    
    fun isPlayer(sender: CommandSender, sendError: Boolean = false): Boolean {
        if (sender !is Player) {
            if (sendError) {
                sendMsgWithPrefix(sender, Lang["message.non_player"])
            }
            return false
        }
        return true
    }
    
    fun hasPerm(sender: CommandSender, permission: String, sendError: Boolean = false): Boolean {
        if (!sender.hasPermission(permission)) {
            if (sendError) {
                sendMsgWithPrefix(sender, Lang["message.noperm"])
            }
            return false
        }
        return true
    }
    
    /**
     * 兼容Paper和Folia的异步任务执行方法
     */
    fun runAsync(task: Runnable) {
        try {
            // 尝试使用Folia的异步调度器
            server.asyncScheduler.runNow(this) { _ -> task.run() }
        } catch (e: NoSuchMethodError) {
            try {
                // 如果Folia API不可用，使用全局区域调度器  
                server.globalRegionScheduler.run(this) { _ -> task.run() }
            } catch (e2: NoSuchMethodError) {
                // 最后回退到传统调度器（仅Paper）
                server.scheduler.runTaskAsynchronously(this, task)
            }
        } catch (e: Exception) {
            logger.warning("Failed to run async task: ${e.message}")
        }
    }
    
    /**
     * 兼容Paper和Folia的延时任务执行方法
     */
    fun runTaskLater(task: Runnable, delay: Long) {
        try {
            // 尝试使用Folia的延时调度器
            server.asyncScheduler.runDelayed(this, { _ -> task.run() }, delay * 50L, java.util.concurrent.TimeUnit.MILLISECONDS)
        } catch (e: NoSuchMethodError) {
            try {
                // 如果Folia API不可用，使用全局区域调度器
                server.globalRegionScheduler.runDelayed(this, { _ -> task.run() }, delay)
            } catch (e2: NoSuchMethodError) {
                // 最后回退到传统调度器（仅Paper）
                server.scheduler.runTaskLater(this, task, delay)
            }
        } catch (e: Exception) {
            logger.warning("Failed to run delayed task: ${e.message}")
        }
    }
}

val PL: Main
    get() = Main.instance