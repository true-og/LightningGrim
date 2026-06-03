package ac.grim.grimac.manager.init.load;

import ac.grim.grimac.utils.anticheat.LogUtil;
import ac.grim.grimac.utils.common.PropertiesUtil;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.chat.ChatTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.enchantment.type.EnchantmentTypes;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.PEVersion;

import java.util.Properties;
import java.util.concurrent.Executors;

public class PacketEventsInit implements LoadableInitable {

    private final PacketEventsAPI<?> packetEventsAPI;
    private static final PEVersion MINIMUM_REQUIRED_PE_VERSION = new PEVersion(2, 11, 2, false);

    public PacketEventsInit(PacketEventsAPI<?> packetEventsAPI) {
        this.packetEventsAPI = packetEventsAPI;
    }

    @Override
    public void load() {
        LogUtil.info("Loading PacketEvents...");

        if (isShadePE()) {
            // Shaded build owns the lifecycle.
            PacketEvents.setAPI(packetEventsAPI);

            if (!checkPacketEventsVersion()) {
                LogUtil.error("\n" +
                        "******************************************************\n" +
                        "LightningGrim requires PacketEvents >= " + MINIMUM_REQUIRED_PE_VERSION +
                        (MINIMUM_REQUIRED_PE_VERSION.snapshot() ? "-SNAPSHOT" : "") + "\n" +
                        "Current version: " + PacketEvents.getAPI().getVersion() + "\n" +
                        "Please update PacketEvents to a compatible version.\n" +
                        "*****************************************************");
            }

            PacketEvents.getAPI().getSettings()
                    .fullStackTrace(true)
                    .kickOnPacketException(true)
                    .checkForUpdates(false)
                    .reEncodeByDefault(false)
                    .debug(false);
            PacketEvents.getAPI().load();
        } else {
            // External provider owns settings + load; only verify the running version.
            if (!checkPacketEventsVersion()) {
                LogUtil.error("\n" +
                        "******************************************************\n" +
                        "LightningGrim requires PacketEvents >= " + MINIMUM_REQUIRED_PE_VERSION + "\n" +
                        "Current version: " + PacketEvents.getAPI().getVersion() + "\n" +
                        "Please update PacketEvents to a compatible version.\n" +
                        "*****************************************************");
            }
        }

        // Async warm-up so JIT class-loads PE types before the first packet arrives.
        Executors.defaultThreadFactory().newThread(() -> {
            StateTypes.AIR.getName();
            ItemTypes.AIR.getName();
            EntityTypes.PLAYER.getParent();
            EntityDataTypes.BOOLEAN.getName();
            ChatTypes.CHAT.getName();
            EnchantmentTypes.ALL_DAMAGE_PROTECTION.getName();
            ParticleTypes.DUST.getName();
        }).start();
    }

    private boolean checkPacketEventsVersion() {
        PEVersion current = PacketEvents.getAPI().getVersion();
        PEVersion required = MINIMUM_REQUIRED_PE_VERSION;

        if (current.isNewerThan(required)) {
            return true;
        }

        // Accept any 2.11.2 regardless of snapshot status; the True-OG fork self-reports a snapshot.
        return current.major() == required.major()
                && current.minor() == required.minor()
                && current.patch() == required.patch();
    }

    /** Cached read of {@code build.shade_pe} from {@code grimac.properties}. */
    private static volatile Boolean cachedShadePE;

    public static boolean isShadePE() {
        Boolean cached = cachedShadePE;
        if (cached != null) return cached;
        synchronized (PacketEventsInit.class) {
            if (cachedShadePE != null) return cachedShadePE;
            final Properties properties = PropertiesUtil.readProperties(PacketEventsInit.class, "grimac.properties");
            final String raw = properties.getProperty("build.shade_pe", "false");
            cachedShadePE = Boolean.parseBoolean(raw.trim());
            return cachedShadePE;
        }
    }
}
