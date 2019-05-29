package evolutionary_repair.fitnesseval;

/** it times out after sec seconds */
public class TimeoutStringGeneration extends StringGenerationTerminationCond {
	private long endTimeMillis;
	private int sec;

	public TimeoutStringGeneration(int sec) {
		this.sec = sec;
	}

	@Override
	public void start() {
		endTimeMillis = System.currentTimeMillis() + (1000 * sec);
	}

	@Override
	public boolean timeout() {
		return System.currentTimeMillis() > endTimeMillis;
	}
}
