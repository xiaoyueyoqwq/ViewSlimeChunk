package top.e404.viewslimechunk.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import top.e404.viewslimechunk.PL
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object Config {
    private lateinit var config: FileConfiguration
    private lateinit var configFile: File
    
    val range: Int
        get() = config.getInt("range", 3)

    val disable: List<String>
        get() = config.getStringList("disable")
        
    fun load() {
        configFile = File(PL.dataFolder, "config.yml")
        
        // Create plugin directory if it doesn't exist
        if (!PL.dataFolder.exists()) {
            PL.dataFolder.mkdirs()
        }
        
        // Create config file if it doesn't exist
        if (!configFile.exists()) {
            try {
                configFile.createNewFile()
                // Copy default config from jar
                PL.getResource("config.yml")?.let { inputStream ->
                    configFile.writeBytes(inputStream.readBytes())
                }
            } catch (e: IOException) {
                PL.logger.severe("Could not create config.yml: ${e.message}")
            }
        }
        
        config = YamlConfiguration.loadConfiguration(configFile)
        
        // Load defaults from jar
        PL.getResource("config.yml")?.let { inputStream ->
            val defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(inputStream))
            config.setDefaults(defaultConfig)
        }
        
        save()
    }
    
    private fun save() {
        try {
            config.save(configFile)
        } catch (e: IOException) {
            PL.logger.severe("Could not save config.yml: ${e.message}")
        }
    }
}