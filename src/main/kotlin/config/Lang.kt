package top.e404.viewslimechunk.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import top.e404.viewslimechunk.PL
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object Lang {
    private lateinit var config: FileConfiguration
    private lateinit var configFile: File
    
    val char: String
        get() = this["char"]
        
    operator fun get(key: String): String {
        return config.getString(key) ?: "&c找不到语言键: $key"
    }
    
    operator fun get(key: String, vararg replacements: Pair<String, Any>): String {
        var message = get(key)
        replacements.forEach { (placeholder, value) ->
            message = message.replace("{$placeholder}", value.toString())
        }
        return message
    }
    
    fun load() {
        configFile = File(PL.dataFolder, "lang.yml")
        
        // Create plugin directory if it doesn't exist
        if (!PL.dataFolder.exists()) {
            PL.dataFolder.mkdirs()
        }
        
        // Create lang file if it doesn't exist
        if (!configFile.exists()) {
            try {
                configFile.createNewFile()
                // Copy default lang from jar
                PL.getResource("lang.yml")?.let { inputStream ->
                    configFile.writeBytes(inputStream.readBytes())
                }
            } catch (e: IOException) {
                PL.logger.severe("Could not create lang.yml: ${e.message}")
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile)
        
        // Load defaults from jar
        PL.getResource("lang.yml")?.let { inputStream ->
            val defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(inputStream))
            config.setDefaults(defaultConfig)
        }
        
        save()
    }
    
    private fun save() {
        try {
            config.save(configFile)
        } catch (e: IOException) {
            PL.logger.severe("Could not save lang.yml: ${e.message}")
        }
    }
}