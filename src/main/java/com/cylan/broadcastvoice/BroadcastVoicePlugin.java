package com.cylan.broadcastvoice;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BroadcastVoicePlugin extends JavaPlugin implements VoicechatPlugin {

    // El plugin Voice Chat Broadcast detecta este nombre (sin importar mayusculas)
    private static final String GROUP_NAME = "broadcast";
    // Permiso que exige el plugin Voice Chat Broadcast para retransmitir
    private static final String BROADCAST_PERMISSION = "voicechat_broadcast.broadcast";

    private VoicechatServerApi api;
    private Group broadcastGroup;

    @Override
    public void onEnable() {
        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service == null) {
            getLogger().severe("Simple Voice Chat no esta cargado. Desactivando plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        service.registerPlugin(this);
        getLogger().info("BroadcastVoice activado.");
    }

    // ---------- API de Simple Voice Chat ----------

    @Override
    public String getPluginId() {
        return "broadcastvoice";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            api = event.getVoicechat();
            // Grupo persistente: sigue existiendo aunque quede vacio
            broadcastGroup = api.groupBuilder()
                    .setPersistent(true)
                    .setName(GROUP_NAME)
                    .setType(Group.Type.OPEN)
                    .build();
            getLogger().info("Grupo '" + GROUP_NAME + "' creado.");
        });
    }

    // ---------- Comando /broadcastvoice ----------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        if (api == null || broadcastGroup == null) {
            player.sendMessage("§cEl voicechat aun no esta listo. Intenta de nuevo en unos segundos.");
            return true;
        }

        if (!player.hasPermission(BROADCAST_PERMISSION)) {
            player.sendMessage("§cNo tienes el permiso " + BROADCAST_PERMISSION + ".");
            return true;
        }

        VoicechatConnection connection = api.getConnectionOf(player.getUniqueId());
        if (connection == null) {
            player.sendMessage("§cNo estas conectado al voicechat. Instala el mod y revisa que el microfono este activo.");
            return true;
        }

        Group current = connection.getGroup();
        if (current != null && current.getId().equals(broadcastGroup.getId())) {
            connection.setGroup(null);
            player.sendMessage("§eSaliste del canal broadcast.");
        } else {
            connection.setGroup(broadcastGroup);
            player.sendMessage("§aEstas en el canal broadcast. Todo el server te escucha.");
        }
        return true;
    }
}
