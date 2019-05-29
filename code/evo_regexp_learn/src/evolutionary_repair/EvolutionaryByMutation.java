package evolutionary_repair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.uncommons.watchmaker.framework.EvolutionaryOperator;

import evolutionary_repair.fitnesseval.RegexWFitness;
import regex.operators.AllMutators;
import regex.operators.RegexMutator.MutatedRegExp;
import regex.oracle.CachedOracle;

/**
 * Evolutionary operators are the components that perform the actual evolution
 * of a population. Cross-over is an evolutionary operator, as is mutation.
 * 
 * In this case: use mutation
 */

/**
 * evolution by mutations: take each element and mutate random (actually with a
 * random operator)
 */
class EvolutionaryByMutation implements EvolutionaryOperator<RegexWFitness> {

	private final CachedOracle o;

	EvolutionaryByMutation(CachedOracle o) {
		this.o = o;
	}

	/**
	 * @param rr  The individuals to evolve.
	 * @param rng A source of randomness for stochastic operators (most operators
	 *            will be stochastic).
	 * @return The evolved individuals (one random for each individual).
	 */
	@Override
	public List<RegexWFitness> apply(List<RegexWFitness> rr, Random arg1) {
		List<RegexWFitness> results = new ArrayList<RegexWFitness>();
		// shuffle the regexes
		Collections.shuffle(rr);
		// select some individuals to be mutated
		for (RegexWFitness r : rr) {
			Iterator<MutatedRegExp> mutants = AllMutators.mutator.mutateRandom(r.getMutant());
			// take one mutant for r
			while (mutants.hasNext()) {
				RegexWFitness mutatedRexExp = RegexWFitness.createRegexWFitness(mutants.next().mutatedRexExp, o, false);
				if (!results.contains(mutatedRexExp)) {
					results.add(mutatedRexExp);
					break;
				}
			}
		}
		return results;
	}
}
