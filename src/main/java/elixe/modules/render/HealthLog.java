package elixe.modules.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

import elixe.Elixe;
import elixe.events.OnPacketReceiveEvent;
import elixe.events.OnRender2DEvent;
import elixe.modules.Module;
import elixe.modules.ModuleCategory;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import net.minecraft.entity.DataWatcher.WatchableObject;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;

public class HealthLog extends Module {

	public HealthLog() {
		super("HealthLog", ModuleCategory.RENDER);
	}


	public void onDisable() {
		super.onDisable();
		healthLogs.clear();
		ySpacing = 10f;
	}
	
	List<LogEntry> healthLogs = new CopyOnWriteArrayList<>();
	private float ySpacing;
	@EventHandler
	private Listener<OnPacketReceiveEvent> onPacketReceiveEvent = new Listener<>(e -> {
		if (e.getPacket() instanceof S1CPacketEntityMetadata) {
			S1CPacketEntityMetadata meta = (S1CPacketEntityMetadata) e.getPacket();
			if (meta.getEntityId() == mc.thePlayer.getEntityId()) {
				for (WatchableObject data : meta.func_149376_c()) {
					if (data.getDataValueId() == 6) {
						healthLogs.add(new LogEntry(100f, ySpacing, System.currentTimeMillis(), 2000, "cu -> " + data.getObject()));
						ySpacing += 10f;
					}
				}
			}
		}
	});
	
	@EventHandler
	private Listener<OnRender2DEvent> onRender2DEvent = new Listener<>(e -> {
		updateEntries();
		if (!this.mc.gameSettings.showDebugInfo) {
			for (LogEntry log : healthLogs) {
				elixe.utils.misc.FontUtil.drawStringWithShadow(mc.fontRendererObj, log.text, log.x, log.y, 0xFFFFFFFF);
			}
		}
	});
	
	private void updateEntries() {
		long actualTime = System.currentTimeMillis();
		
		// Collect expired entries first to avoid ConcurrentModificationException
		ArrayList<LogEntry> toRemove = new ArrayList<>();
		for (LogEntry log : healthLogs) {
			if (log.timePassed(actualTime)) {
				toRemove.add(log);
			}
		}
		
		if (!toRemove.isEmpty()) {
			healthLogs.removeAll(toRemove);
			// Recalculate ySpacing from scratch
			ySpacing = 10f;
			for (LogEntry log : healthLogs) {
				log.y = ySpacing;
				ySpacing += 10f;
			}
		}
	}

	public class LogEntry {
		public float x, y;

		public long startTime;
		public int logTime;
		public String text;

		public LogEntry(float x, float y, long startTime, int logTime, String text) {
			super();
			this.x = x;
			this.y = y;
			this.startTime = startTime;
			this.logTime = logTime;
			this.text = text;
		}

		public boolean timePassed(long actualTime) {
			return actualTime - startTime > logTime;
		}
	}
}
