package evolutionary_repair.fitnesseval;

/** when the generation of strings must be terminate */
public abstract class StringGenerationTerminationCond {

	/** start the generation */
	public abstract void start();

	/** return true if the generation must be stopped */
	public abstract boolean timeout();
}
