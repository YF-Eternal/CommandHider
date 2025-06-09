// v1.8.X-1.12.X 实现
package org.commandhider.tabcomplete.v1_12;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.commandhider.CommandHider;
import org.commandhider.tabcomplete.PacketListenerHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketListenerHandlerImpl implements PacketListenerHandler {
    @Override
    public void registerPacketListener() {
        CommandHider thisPlugin=CommandHider.getInstance();

        // 添加一个监听器，以拦截客户端发来的TAB_COMPLETE包w
        thisPlugin.manager.addPacketListener(new PacketAdapter(thisPlugin, PacketType.Play.Client.TAB_COMPLETE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE && !event.getPlayer().hasPermission("commandhider.commands")
                        && event.getPacket().getStrings().read(0).startsWith("/") && event.getPacket().getStrings().read(0).split(" ").length == 1) {
                    event.setCancelled(true);
                    String start = event.getPacket().getStrings().read(0);
                    List<String> list = new ArrayList<>();
                    if (!thisPlugin.all) {
                        try {
                            // 获取命令补全的候选列表
                            list.addAll(Arrays.asList((String[]) thisPlugin.getServer().getClass().getMethod("tabCompleteCommand", Player.class, String.class).invoke(thisPlugin.getServer(), event.getPlayer(), start)));
                        } catch (Exception e) {
                            plugin.getLogger().warning("在命令补全时发生错误: " + e.getMessage());
                        }
                        // 移除被隐藏的命令
                        for (String tab : thisPlugin.hiddenCommands) {
                            list.remove('/' + tab);
                        }
                    }
                    // 添加需要显示的命令别名
                    List<String> extra = new ArrayList<>();
                    for (String alias : thisPlugin.showCommands) {
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
                    PacketContainer tabComplete = thisPlugin.manager.createPacket(PacketType.Play.Server.TAB_COMPLETE);
                    tabComplete.getStringArrays().write(0, tabList);
                    try {
                        thisPlugin.manager.sendServerPacket(event.getPlayer(), tabComplete);
                    } catch (Exception e) {
                        plugin.getLogger().warning("发送命令补全数据包时发生错误: " + e.getMessage());
                    }
                }
            }
        });
    }
}
