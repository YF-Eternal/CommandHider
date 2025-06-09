package org.commandhider.tabcomplete.v1_19;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import org.bukkit.entity.Player;
import org.commandhider.CommandHider;
import org.commandhider.tabcomplete.PacketListenerHandler;

import java.lang.reflect.Field;

public class PacketListenerHandlerImpl implements PacketListenerHandler {
    @Override
    public void registerPacketListener() {
        // 获取 CommandHider 插件实例
        CommandHider thisPlugin = CommandHider.getInstance();

        // 注册 PacketListener，监听 COMMMANDS 数据包
        thisPlugin.manager.addPacketListener(new PacketAdapter(thisPlugin, PacketType.Play.Server.COMMANDS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                // 获取发送数据包的玩家
                Player player = event.getPlayer();

                // 如果玩家没有权限，则取消命令包的发送
                if (!player.hasPermission("commandhider.commands")) {
                    event.setCancelled(true); // 阻止原始命令包发送

                    try {
                        // 输出日志，显示正在获取命令树
                        thisPlugin.getLogger().info("获取命令树...");

                        // 获取 MinecraftServer 实例
                        Object minecraftServer = event.getPacket().getHandle();
                        Class<?> minecraftServerClass = minecraftServer.getClass();

                        // 使用反射获取 CommandDispatcher 对象
                        Field dispatcherField = minecraftServerClass.getDeclaredField("dispatcher");
                        dispatcherField.setAccessible(true);
                        Object dispatcher = dispatcherField.get(minecraftServer);

                        // 通过反射获取 CommandListenerWrapper 类实例
                        Class<?> commandListenerWrapperClass = Class.forName("net.minecraft.commands.CommandListenerWrapper");
                        Object listenerWrapper = commandListenerWrapperClass.getDeclaredConstructor().newInstance();

                        // 获取根命令节点
                        Field rootField = dispatcher.getClass().getDeclaredField("root");
                        rootField.setAccessible(true);
                        RootCommandNode<?> rootNode = (RootCommandNode<?>) rootField.get(dispatcher);

                        // 遍历命令树中的每个子节点
                        for (CommandNode<?> node : rootNode.getChildren()) {
                            String name = node.getName();
                            thisPlugin.getLogger().info("命令节点名: " + name);

                            // 如果命令是白名单命令，则继续
                            if (thisPlugin.showCommands.contains(name)) {
                                continue;
                            }

                            // 如果命令在隐藏列表中，则移除该命令
                            if (thisPlugin.hiddenCommands.contains(name)) {
                                rootNode.getChildren().remove(node); // 从命令树中移除该命令
                                thisPlugin.getLogger().info("隐藏命令: " + name);
                            }
                        }

                        // 重新设置更新后的命令树
                        rootField.set(dispatcher, rootNode);

                        // 输出日志，命令树已更新并成功发送给玩家
                        thisPlugin.getLogger().info("命令树已更新并发送给玩家。");

                    } catch (Exception e) {
                        // 捕获异常并输出日志
                        thisPlugin.getLogger().warning("拦截命令树失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
