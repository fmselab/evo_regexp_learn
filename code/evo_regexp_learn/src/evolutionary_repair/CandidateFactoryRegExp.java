package evolutionary_repair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uncommons.watchmaker.framework.factories.AbstractCandidateFactory;

import evolutionary_repair.fitnesseval.RegexWFitness;
import regex.operators.AllMutators;
import regex.operators.RegexMutator.MutatedRegExp;
import regex.oracle.CachedOracle;

/**
 * Every evolutionary simulation must start with an initial population of
 * candidate solutions and the CandidateFactory interface is the mechanism by
 * which the evolution engine creates this population.
 * 
 * In our case, take a random mutator or duplicate the candidate
 */
public class CandidateFactoryRegExp extends AbstractCandidateFactory<RegexWFitness> {
	private static Logger logger = Logger.getLogger(CandidateFactoryRegExp.class.getName());
	private RegexWFitness initR;
	private Iterator<MutatedRegExp> mutants;
	private CachedOracle oracle;

	CandidateFactoryRegExp(RegexWFitness candidate, CachedOracle oracle) {
		super();
		mutants = AllMutators.mutator.mutateRandom(candidate.getMutant());
		initR = candidate;
		this.oracle = oracle;
	}

	@Override
	public RegexWFitness generateRandomCandidate(Random arg0) {
		throw new RuntimeException("use method to build the list");
	}

	@Override
	public List<RegexWFitness> generateInitialPopulation(int populationSize, Random rng) {
		throw new RuntimeException();
	}

	@Override
	public List<RegexWFitness> generateInitialPopulation(int populationSize, Collection<RegexWFitness> seedCandidates,
			Random rng) {
		List<RegexWFitness> population = new ArrayList<RegexWFitness>(populationSize);
		// assuming that there are no candidates
		assert seedCandidates.size() == 0;
		// add first element (the candidate itself)
		population.add(initR);
		// add its mutations
		while (mutants.hasNext() && population.size() < populationSize) {
			RegexWFitness regexPlus = RegexWFitness.createRegexWFitness(mutants.next().mutatedRexExp, oracle, false);
			logger.log(Level.INFO, "added to initial population " + regexPlus);
			population.add(regexPlus);
		}
		int n1Mutation = population.size();
		// if the population is not big enough
		while (population.size() < populationSize) {
			// add other mutations of
			// take a random element in the population (only first mutations?)
			// avoid the first one (the candidate itself)
			int take = rng.nextInt(n1Mutation - 1);
			RegexWFitness newParent = population.get(take + 1);
			int nmut = 0;
			// take max 10 for each mutant
			Iterator<MutatedRegExp> it = AllMutators.mutator.mutateRandom(newParent.getMutant());
			while (it.hasNext() && population.size() < populationSize && nmut < 10) {
				RegexWFitness regexPlus = RegexWFitness.createRegexWFitness(it.next().mutatedRexExp, oracle, false);
				logger.log(Level.INFO, "added to initial population " + regexPlus);
				population.add(regexPlus);
			}
		}
		logger.log(Level.INFO, "population generated");
		return Collections.unmodifiableList(population);
	}
}
