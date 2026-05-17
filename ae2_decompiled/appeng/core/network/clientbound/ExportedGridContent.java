/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientPacketListener
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.RegistryFriendlyByteBuf
 *  net.minecraft.network.chat.ClickEvent
 *  net.minecraft.network.chat.ClickEvent$Action
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.codec.ByteBufCodecs
 *  net.minecraft.network.codec.StreamCodec
 *  net.minecraft.network.protocol.common.custom.CustomPacketPayload$Type
 *  net.minecraft.world.entity.player.Player
 *  net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package appeng.core.network.clientbound;

import appeng.core.network.ClientboundPacket;
import appeng.core.network.CustomAppEngPayload;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ExportedGridContent(int serialNumber, ContentType contentType, byte[] compressedData) implements ClientboundPacket
{
    public static final CustomPacketPayload.Type<ExportedGridContent> TYPE = CustomAppEngPayload.createType("exported_grid_content");
    public static final StreamCodec<RegistryFriendlyByteBuf, ExportedGridContent> STREAM_CODEC = StreamCodec.composite((StreamCodec)ByteBufCodecs.INT, ExportedGridContent::serialNumber, ContentType.STREAM_CODEC, ExportedGridContent::contentType, (StreamCodec)ByteBufCodecs.BYTE_ARRAY, ExportedGridContent::compressedData, ExportedGridContent::new);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final Logger LOG = LoggerFactory.getLogger(ExportedGridContent.class);

    public CustomPacketPayload.Type<ExportedGridContent> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        Object filename;
        Path saveDir = Minecraft.getInstance().gameDirectory.toPath();
        IntegratedServer spServer = Minecraft.getInstance().getSingleplayerServer();
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (spServer != null) {
            saveDir = spServer.getServerDirectory();
            filename = "ae2_grid_";
        } else if (connection != null) {
            filename = "ae2_grid_from_server_";
        } else {
            LOG.error("Ignoring grid export without a connection to a server.");
            return;
        }
        saveDir = saveDir.toAbsolutePath().normalize();
        filename = (String)filename + this.serialNumber + "_" + TIMESTAMP_FORMATTER.format(LocalDateTime.now()) + ".zip";
        OpenOption[] openOptions = new OpenOption[]{};
        if (this.contentType != ContentType.FIRST_CHUNK) {
            openOptions = new OpenOption[]{StandardOpenOption.APPEND};
        }
        Path tempPath = saveDir.resolve((String)filename + ".tmp");
        Path finalPath = saveDir.resolve((String)filename);
        try (OutputStream out = Files.newOutputStream(tempPath, openOptions);){
            out.write(this.compressedData);
        }
        catch (IOException e) {
            player.sendSystemMessage((Component)Component.literal((String)("Failed to write exported grid data to " + String.valueOf(tempPath))).withStyle(ChatFormatting.RED));
            LOG.error("Failed to write exported grid data to {}", (Object)tempPath, (Object)e);
            return;
        }
        if (this.contentType == ContentType.LAST_CHUNK) {
            try {
                Files.move(tempPath, finalPath, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                LOG.error("Failed to move grid export {} into place", (Object)finalPath, (Object)e);
            }
            player.sendSystemMessage((Component)Component.literal((String)("Saved grid data for grid #" + this.serialNumber + " from server to ")).append((Component)Component.literal((String)finalPath.toString()).withStyle(style -> style.withUnderlined(Boolean.valueOf(true)).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, finalPath.getParent().toString())))));
        }
    }

    public static enum ContentType {
        FIRST_CHUNK,
        CHUNK,
        LAST_CHUNK;

        public static final StreamCodec<FriendlyByteBuf, ContentType> STREAM_CODEC;

        static {
            STREAM_CODEC = NeoForgeStreamCodecs.enumCodec(ContentType.class);
        }
    }
}

