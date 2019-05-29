package evolutionary_repair.fitnesseval;

import java.util.List;
import java.util.logging.Logger;

import org.uncommons.watchmaker.framework.FitnessEvaluator;

import regex.oracle.CachedOracle;

/*
 * In the Watchmaker Framework, a fitness function is written by implementing
 * the FitnessEvaluator interface. The getFitness method of this interface takes
 * a candidate solution and returns its fitness score as a Java double. The
 * method actually takes two arguments, but we can ignore the second for now. *
 */
public abstract class RegExpEvaluator implements FitnessEvaluator<RegexWFitness> {
	public static Logger logger = Logger.getLogger(RegExpEvaluator.class.getName());
	protected CachedOracle oracle;
	public boolean computeNewStrings;
	protected StringGenerationTerminationCond cond;

	RegExpEvaluator(CachedOracle oracle, StringGenerationTerminationCond cond) {
		this.oracle = oracle;
		computeNewStrings = true;
		this.cond = cond;
	}

	// how many strings are correctly evaluated. If necessary generate new strings.
	@Override
	public double getFitness(RegexWFitness candidate, List<? extends RegexWFitness> population) {
		if (computeNewStrings) {
			generateNewStrings(population);
			// in every generation, it updates the population
			// once (the first time getFitness is called)
			computeNewStrings = false;
		}
		// return candidate.getFitness();
		// now the fitness is updated every time
		// a new string is generated
		return candidate.getFitness();
	}

	abstract public void generateNewStrings(List<? extends RegexWFitness> population);

	abstract public String getStringGenName();
	
// TODO from UCDetector: Method "RegExpEvaluator.getPopulationMaxFitness(List<? extends RegexWFitness>)" has 0 references
	public double getPopulationMaxFitness(List<? extends RegexWFitness> population) { // NO_UCD (unused code)
		RegexWFitness candidate = population.get(0);
		for(int i = 1; i < population.size(); i++) {
			RegexWFitness r = population.get(i);
			if(r.getFitness() > candidate.getFitness()) {
				candidate = r;
			}
		}
		//logger.log(Level.INFO, "population fitness " + candidate.getFitnessAsStr());
		return candidate.getFitness();
	}

	@Override
	public boolean isNatural() {
		return true;
	}

}
