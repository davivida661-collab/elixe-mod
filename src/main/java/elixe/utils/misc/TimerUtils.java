package elixe.utils.misc;

public class TimerUtils {
	public class MilisecondTimer {
		private long time, dif = 0;

		public MilisecondTimer() {
			time = System.currentTimeMillis();
		}

		public boolean hasTimePassed(int ms) {

			long trueDelay = ms - dif;
			long timeDif = System.currentTimeMillis() - time;

			if (timeDif >= trueDelay) {
				if (ms > timeDif - trueDelay) {
					dif = timeDif - trueDelay;
				} else {
					dif = 0;
				}

				return true;
			} else {
				return false;
			}
		}

		public void reset() {
			time = System.currentTimeMillis();
		}
	}

	public class TickTimer {
		private int ticks = 0;
		
		public void update() {
			ticks++;
		}
		
		public boolean hasTimePassed(int passedTicks) {
			return ticks >= passedTicks;
		}
		
		public void reset() {
			ticks = 0;
		}
	}
}
