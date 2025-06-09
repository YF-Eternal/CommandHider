// v1.13.X-v1.18X实现
package org.commandhider.tabcomplete.v1_13;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
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
        CommandHider thisPlugin=CommandHider.getInstance();
        thisPlugin.manager.addPacketListener(new PacketAdapter(thisPlugin, PacketType.Play.Server.COMMANDS) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player player = event.getPlayer();

                if (!player.hasPermission("commandhider.commands")) {
                    PacketContainer packet = event.getPacket();
                    Object handle = packet.getHandle();

                    try {
                        RootCommandNode<?> originalRoot = null;

                        // 使用反射查找 RootCommandNode 类型的字段
                        // 遍历所有字段，尝试找出 RootCommandNode 类型的值
                        for (Field field : handle.getClass().getDeclaredFields()) {
                            field.setAccessible(true);
                            Object value = field.get(handle);
//                            thisPlugin.getLogger().info("字段名: " + field.getName() + ", 类型: " + field.getType().getName() +
//                                    ", 实际值类型: " + (value != null ? value.getClass().getName() : "null"));
                            if (value instanceof RootCommandNode) {
                                originalRoot = (RootCommandNode<?>) value;
                                break;
                            }
                        }

                        if (originalRoot == null) {
                            thisPlugin.getLogger().warning("未能在 Packet 中找到命令树字段！");
                            return;
                        }

                        // 克隆一个新的根节点
                        @SuppressWarnings("unchecked")
                        RootCommandNode<Object> newRoot = (RootCommandNode<Object>) new RootCommandNode<>();

                        for (CommandNode<?> child : originalRoot.getChildren()) {
                            String name = child.getName();
                            // 如果配置文件中设置为忽略全部
                            if(thisPlugin.all){
                                // 如果遍历到的子节点名不是白名单命令，则不添加入新命令树
                                if(!thisPlugin.showCommands.contains(name)){
                                    continue;
                                }
                                // 添加保留命令
                                newRoot.addChild((CommandNode<Object>) child);
                            }
                            else {
                                if (thisPlugin.hiddenCommands.contains(name) && !thisPlugin.showCommands.contains(name)) {
                                    continue; // 忽略隐藏命令
                                }
                                // 添加保留命令
                                newRoot.addChild((CommandNode<Object>) child);
                            }
                        }

                        // 替换命令树字段
                        for (Field field : handle.getClass().getDeclaredFields()) {
                            if (RootCommandNode.class.isAssignableFrom(field.getType())) {
                                field.setAccessible(true);
                                field.set(handle, newRoot);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        thisPlugin.getLogger().warning("拦截命令树失败: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}