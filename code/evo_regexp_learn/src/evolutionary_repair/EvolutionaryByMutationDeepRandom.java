package evolutionary_repair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import evolutionary_repair.fitnesseval.RegexWFitness;
import regex.operators.AllMutators;
import regex.operators.RegexMutator.MutatedRegExp;
import regex.oracle.CachedOracle;

/**
 * evolution by mutations take each element, build all the mutants and take one
 * randomly
 */
public class EvolutionaryByMutationDeepRandom implements EvolutionaryOperator<RegexWFitness> {
	public static Logger logger = Logger.getLogger(EvolutionaryByMutationDeepRandom.class.getName());

	private final CachedOracle o;
	private long timeoutInMillis = Long.MAX_VALUE;
	private long startTime;

	EvolutionaryByMutationDeepRandom(CachedOracle o) {
		this.o = o;
		startTime = System.currentTimeMillis();
	}

	public EvolutionaryByMutationDeepRandom(CachedOracle oracle, long timeoutInMillis) {
		this(oracle);
		this.timeoutInMillis = timeoutInMillis;
	}

	/**
	 * @param rr  The individuals to evolve.
	 * @param rng A source of randomness for stochastic operators (most operators
	 *            will be stochastic).
	 * @return The evolved individuals.
	 */
	@Override
	public List<RegexWFitness> apply(List<RegexWFitness> rr, Random arg1) {
		List<RegexWFitness> results = new ArrayList<RegexWFitness>();
		logger.info("start building mutants for " + rr.size() + " regexes");
		for (RegexWFitness r : rr) {
			logger.fine("building mutants for " + r);
			Iterator<MutatedRegExp> mutants = AllMutators.mutator.mutateRandom(r.getMutant());
			if (mutants.hasNext()) {
				RegexWFitness regexPlus = RegexWFitness.createRegexWFitness(mutants.next().mutatedRexExp, o, false);
				logger.fine("adding " + regexPlus);
				results.add(regexPlus);
			}
			if ((System.currentTimeMillis() - startTime) > timeoutInMillis) {
				logger.info("timeout building mutants");
				break;
			}
		}
		logger.info("end building mutants");
		return results;
	}
}
