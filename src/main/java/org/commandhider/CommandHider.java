package org.commandhider;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.commandhider.tabcomplete.PacketListenerHandler;

import java.util.ArrayList;
import java.util.List;

public class CommandHider extends JavaPlugin implements Listener {
    // 插件实例
    private static CommandHider instance;
    // 存储被隐藏的命令
    public List<String> hiddenCommands;
    // 存储需要显示的命令别名
    public List<String> showCommands;
    // 是否隐藏所有命令
    public boolean all;
    // ProtocolManager 实例，用于注册和管理协议包监听器
    public ProtocolManager manager;
    // TabCompleterHandler 实例，用于获取命令补全的候选列表
    private PacketListenerHandler packetListenerHandler;

    public CommandHider() {
        instance = this;
    }
    public static CommandHider getInstance(){
        return instance;
    }

    @Override
    public void onEnable() {
        // 加载配置数据
        loadConfigData();
        // 设置插件实例
        instance = this;
        // 动态注册适配版本的补全命令api
        registerPacketListenerHandler();
        // 注册协议包监听器
        registerPacketListener();
        // 注册事件监听器
        registerEvents(this);
        // 发送加载成功的信息到控制台
        getLogger().info(ChatColor.AQUA + "CommandHider 已加载!");
        getLogger().info("作者: YF_Eternal");
    }

    @Override
    public void onDisable() {
        // 发送卸载成功的信息到控制台
        getLogger().info(ChatColor.AQUA + "CommandHider 已卸载!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 处理命令hider的reload命令
        if (command.getName().equalsIgnoreCase("commandhider") && args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("commandhider.reload")) {
            // 重新加载配置数据
            reloadConfigData();
            // 发送重新加载配置文件成功的消息给发送者
            sender.sendMessage(ChatColor.GREEN + "CommandHider 配置文件已重新加载.");
            return true;
        }
        return false;
    }

    // 加载配置数据
    private void loadConfigData() {
        FileConfiguration config = getConfig();
        // 从配置文件加载被隐藏的命令列表和需要显示的命令别名列表
        hiddenCommands = new ArrayList<>(config.getStringList("hiddencommands"));
        showCommands = new ArrayList<>(config.getStringList("showcommands"));
        // 检查是否隐藏所有命令
        all = !hiddenCommands.isEmpty() && hiddenCommands.get(0).equalsIgnoreCase("all");
        // 保存默认配置文件
        saveDefaultConfig();
    }

    // 重新加载配置数据
    private void reloadConfigData() {
        // 重新加载配置文件
        reloadConfig();
        // 加载配置数据
        loadConfigData();
        instance = this;
        getLogger().info(showCommands.toString());
    }

    // 根据核心版本动态注册 PacketListenerHandler 实例
    private void registerPacketListenerHandler() {
        // 获取当前服务器的版本信息
        String version = getServer().getClass().getPackage().getName().split("\\.")[3];
        // 输出服务器版本到日志
        getLogger().info("Minecraft 版本: " + version);
        // 定义包路径变量，根据不同版本选择不同的 PacketListenerHandler 实现
        String path = "";
        // 检查版本并确定相应的 PacketListenerHandler 类路径
        if(version.startsWith("v1_8") || version.startsWith("v1_9") || version.startsWith("v1_10") || version.startsWith("v1_11") || version.startsWith("v1_12")){
            // 对应 v1_8 到 v1_12 的版本使用该类实现
            path = "org.commandhider.tabcomplete.v1_12.PacketListenerHandlerImpl";
        }
        else if(version.startsWith("v1_13") || version.startsWith("v1_14") || version.startsWith("v1_15") || version.startsWith("v1_16") || version.startsWith("v1_17") || version.startsWith("v1_18") || version.startsWith("v1_19") || version.startsWith("v1_20") || version.startsWith("v1_21") ){
            // 对应 v1_13 到 v1_21 的版本使用该类实现
            path = "org.commandhider.tabcomplete.v1_13.PacketListenerHandlerImpl";
        }
        else if(version.startsWith("v1_19") || version.startsWith("v1_20") || version.startsWith("v1_21") ){
            // 针对 v1_19 到 v1_21 使用该类
            path = "org.commandhider.tabcomplete.v1_19.PacketListenerHandlerImpl";
        }
        try {
            // 使用反射加载对应版本的 PacketListenerHandler 类
            Class<?> clazz = Class.forName(path);
            // 创建该类的实例并赋值给 packetListenerHandler
            packetListenerHandler = (PacketListenerHandler) clazz.getDeclaredConstructor().newInstance();
            // 输出日志，显示使用的 TabCompleterHandler 类
            getLogger().info("使用 TabCompleterHandler: " + version);
        } catch (Exception e) {
            // 如果发生异常，表示该版本不支持，输出错误日志并禁用插件
            getLogger().severe("不支持的 Minecraft 版本: " + version);
            getServer().getPluginManager().disablePlugin(this);
        }
        // 获取 ProtocolManager 实例
        manager = ProtocolLibrary.getProtocolManager();
        // 调用 PacketListenerHandler 的注册方法
        packetListenerHandler.registerPacketListener();
    }


    // 注册协议包监听器
    private void registerPacketListener() {
        manager = ProtocolLibrary.getProtocolManager();

    }

    // 注册事件监听器
    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
