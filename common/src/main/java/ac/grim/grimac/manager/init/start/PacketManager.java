package ac.grim.grimac.manager.init.start;

import ac.grim.grimac.events.packets.*;
import ac.grim.grimac.events.packets.worldreader.BasePacketWorldReader;
import ac.grim.grimac.events.packets.worldreader.PacketWorldReaderEight;
import ac.grim.grimac.events.packets.worldreader.PacketWorldReaderEighteen;
import ac.grim.grimac.manager.init.load.PacketEventsInit;
import ac.grim.grimac.manager.init.stop.StoppableInitable;
import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

import java.util.ArrayList;
import java.util.List;

public class PacketManager implements StartableInitable, StoppableInitable {

    private final List<PacketListenerCommon> registeredHandles = new ArrayList<>();

    @Override
    public void start() {
        LogUtil.info("Registering packets...");

        registerTracked(new PacketPlayerJoinQuit());
        registerTracked(new PacketPingListener());
        registerTracked(new PacketPlayerWindow());
        registerTracked(new PacketPlayerDigging());
        registerTracked(new PacketPlayerAttack());
        registerTracked(new PacketEntityAction());
        registerTracked(new PacketBlockAction());
        registerTracked(new PacketSelfMetadataListener());
        registerTracked(new PacketServerTeleport());
        registerTracked(new PacketPlayerCooldown());
        registerTracked(new PacketPlayerRespawn());
        registerTracked(new PacketPlayerTick());
        registerTracked(new CheckManagerListener());
        registerTracked(new PacketPlayerSteer());

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_13)) {
            registerTracked(new PacketServerTags());
        }

        if (PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_18)) {
            registerTracked(new PacketWorldReaderEighteen());
        } else if (PacketEvents.getAPI().getServerManager().getVersion().isOlderThanOrEquals(ServerVersion.V_1_8_8)) {
            registerTracked(new PacketWorldReaderEight());
        } else {
            registerTracked(new BasePacketWorldReader());
        }

        registerTracked(new ProxyAlertMessenger());
        registerTracked(new PacketHidePlayerInfo());

        // init() belongs to the lifecycle owner; the external provider has already called it.
        if (PacketEventsInit.isShadePE()) {
            PacketEvents.getAPI().init();
        }
    }

    private void registerTracked(PacketListenerCommon listener) {
        PacketEvents.getAPI().getEventManager().registerListener(listener);
        registeredHandles.add(listener);
    }

    @Override
    public void stop() {
        for (PacketListenerCommon handle : registeredHandles) {
            try {
                PacketEvents.getAPI().getEventManager().unregisterListener(handle);
            } catch (Throwable t) {
                LogUtil.warn("Failed to unregister PE listener: " + t.getMessage());
            }
        }
        registeredHandles.clear();
    }
}
