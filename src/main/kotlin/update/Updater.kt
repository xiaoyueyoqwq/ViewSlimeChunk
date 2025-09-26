package top.e404.viewslimechunk.update

import com.google.gson.JsonParser
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import top.e404.viewslimechunk.PL
import top.e404.viewslimechunk.config.Config
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

object Updater : Listener {
    private const val API_URL = "https://api.github.com/repos/xiaoyueyoqwq/ViewSlimeChunk/releases/latest"
    private const val GITHUB_URL = "https://github.com/xiaoyueyoqwq/ViewSlimeChunk"
    private var latestVersion: String? = null
    private var hasUpdate = false
    
    fun init() {
        if (!enableUpdate()) return
        
        // 注册事件监听器
        Bukkit.getPluginManager().registerEvents(this, PL)
        
        // 异步检查更新
        PL.runAsync(Runnable {
            checkForUpdates()
        })
    }
    
    private fun enableUpdate(): Boolean {
        return Config.range > 0 // 使用 range > 0 作为更新检查的开关，因为 config 不再更新检查项
    }
    
    private fun checkForUpdates() {
        try {
            val client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()
                
            val request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "ViewSlimeChunk-Plugin")
                .GET()
                .build()
                
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            
            if (response.statusCode() == 200) {
                val json = JsonParser.parseString(response.body()).asJsonObject
                latestVersion = json.get("tag_name").asString.removePrefix("v")
                
                val currentVersion = PL.pluginMeta.version
                hasUpdate = !isVersionEqual(currentVersion, latestVersion!!)
                
                if (hasUpdate) {
                    PL.sendConsoleMessage("&e[更新检查] 发现新版本: $latestVersion (当前: $currentVersion)")
                    PL.sendConsoleMessage("&e[更新检查] 下载地址: $GITHUB_URL/releases")
                }
            }
        } catch (e: Exception) {
            PL.logger.warning("更新检查失败: ${e.message}")
        }
    }
    
    private fun isVersionEqual(current: String, latest: String): Boolean {
        return current == latest
    }
    
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!hasUpdate || !event.player.isOp) return
        
        PL.runTaskLater(Runnable {
            PL.sendMsgWithPrefix(event.player, "&e发现新版本: $latestVersion")
            PL.sendMsgWithPrefix(event.player, "&e下载地址: $GITHUB_URL/releases")
        }, 40L) // 2秒后发送
    }
}