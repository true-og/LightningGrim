package ac.grim.grimac.manager.init.stop;

import ac.grim.grimac.manager.init.load.PacketEventsInit;
import ac.grim.grimac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.PacketEvents;

public class TerminatePacketEvents implements StoppableInitable {
    @Override
    public void stop() {
        // External-provider mode leaves termination to the installed PE plugin's onDisable.
        if (!PacketEventsInit.isShadePE()) {
            return;
        }
        LogUtil.info("Terminating PacketEvents...");
        PacketEvents.getAPI().terminate();
    }
}
