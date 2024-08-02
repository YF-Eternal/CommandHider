package org.commandhider;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandHider extends JavaPlugin implements Listener {
    // 存储被隐藏的命令
    private List<String> hiddenCommands;
    // 存储需要显示的命令别名
    private List<String> aliases;
    // 是否隐藏所有命令
    private boolean all;
    // ProtocolManager 实例，用于注册和管理协议包监听器
    private ProtocolManager manager;

    @Override
    public void onEnable() {
        // 加载配置数据
        loadConfigData();
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
        aliases = new ArrayList<>(config.getStringList("showcommands"));
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
    }

    // 注册协议包监听器
    private void registerPacketListener() {
        manager = ProtocolLibrary.getProtocolManager();
        // 添加一个监听器，以拦截客户端发来的TAB_COMPLETE包
        manager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.TAB_COMPLETE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE && !event.getPlayer().hasPermission("commandhider.commands")
                        && event.getPacket().getStrings().read(0).startsWith("/") && event.getPacket().getStrings().read(0).split(" ").length == 1) {
                    event.setCancelled(true);
                    String start = event.getPacket().getStrings().read(0);
                    List<String> list = new ArrayList<>();
                    if (!all) {
                        try {
                            // 获取命令补全的候选列表
                            list.addAll(Arrays.asList((String[]) getServer().getClass().getMethod("tabCompleteCommand", Player.class, String.class).invoke(getServer(), event.getPlayer(), start)));
                        } catch (Exception e) {
                            getLogger().warning("在命令补全时发生错误: " + e.getMessage());
                        }
                        // 移除被隐藏的命令
                        for (String tab : hiddenCommands) {
                            list.remove('/' + tab);
                        }
                    }
                    // 添加需要显示的命令别名
                    List<String> extra = new ArrayList<>();
                    for (String alias : aliases) {
                        if (('/' + alias).startsWith(start)) {
                            extra.add(alias);
                        }
                    }
                    // 构造最终的命令补全列表
                    String[] tabList = new String[list.size() + extra.size()];
                    int index = 0;
                    for (String s : list) {
                        tabList[index++] = s;
                    }
                    for (String s : extra) {
                        tabList[index++] = '/' + s;
                    }
                    // 按照字母顺序排序
                    Arrays.sort(tabList, String.CASE_INSENSITIVE_ORDER);
                    // 构造一个TAB_COMPLETE协议包并发送给客户端
                    PacketContainer tabComplete = manager.createPacket(PacketType.Play.Server.TAB_COMPLETE);
                    tabComplete.getStringArrays().write(0, tabList);
                    try {
                        manager.sendServerPacket(event.getPlayer(), tabComplete);
                    } catch (Exception e) {
                        getLogger().warning("发送命令补全数据包时发生错误: " + e.getMessage());
                    }
                }
            }
        });
    }

    // 注册事件监听器
    private void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}
