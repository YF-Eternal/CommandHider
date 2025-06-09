// org.commandhider.tabcomplete.TabCompleterHandler.java
package org.commandhider.tabcomplete;

import com.comphenix.protocol.ProtocolManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.commandhider.CommandHider;

import java.util.List;

public interface PacketListenerHandler {
    void registerPacketListener();
}
