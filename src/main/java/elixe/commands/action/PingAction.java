package elixe.commands.action;

import elixe.commands.CommandManager;
import elixe.utils.misc.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

public class PingAction implements IAction {
	@Override
	public String[] getPrefixes() {
		return new String[] {"p", "ping"};
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public int necessaryArguments() {
		return 0;
	}

	@Override
	public String getName() {
		return "ping";
	}

	@Override
	public String getDescription() {
		return "your response time to the server";
	}

	@Override
	public void execute(CommandManager commandManager, String[] args) {
		if (mc.thePlayer == null || mc.getNetHandler() == null) {
			ChatUtils.message(mc, "you are not connected to a server.");
			return;
		}
		try {
			ChatUtils.message(mc, "your ping is " + mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() + ".");
		} catch (Exception e) {
			ChatUtils.message(mc, "could not retrieve ping.");
		}
	}
}
