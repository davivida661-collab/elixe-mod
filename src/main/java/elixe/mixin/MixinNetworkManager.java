package elixe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import elixe.Elixe;
import elixe.events.OnPacketReceiveEvent;
import elixe.events.OnPacketSendEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

/**
 * Posta os eventos de pacote (cancelaveis) — base de MUITOS modulos (Velocity, NoFall, Phase,
 * Ninja, MushExploit, Aesthetics, HealthLog, Misplace, Fly, ItemLock, Commands).
 *
 * Fiel ao MCP-919: o vanilla usa o param ORIGINAL apos o post (nao le event.getPacket() de volta);
 * os modulos mutam o proprio objeto Packet (mesma referencia) ou cancelam. So cancelamento aqui.
 */
@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    // OnPacketReceiveEvent — channelRead0(ctx, packet). Cancelar pula o processPacket. MCP-919 L165.
    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onPacketReceive(ChannelHandlerContext ctx, Packet packet, CallbackInfo ci) {
        OnPacketReceiveEvent event = new OnPacketReceiveEvent(packet);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // OnPacketSendEvent — sendPacket(Packet). MCP-919 L195.
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void elixe$onPacketSend(Packet packetIn, CallbackInfo ci) {
        OnPacketSendEvent event = new OnPacketSendEvent(packetIn);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    // OnPacketSendEvent — sendPacket(Packet, listener, listeners...). MCP-919 L223.
    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V",
            at = @At("HEAD"), cancellable = true)
    private void elixe$onPacketSendWithListeners(Packet packetIn, GenericFutureListener<?> listener,
            GenericFutureListener<?>[] listeners, CallbackInfo ci) {
        OnPacketSendEvent event = new OnPacketSendEvent(packetIn);
        Elixe.INSTANCE.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
