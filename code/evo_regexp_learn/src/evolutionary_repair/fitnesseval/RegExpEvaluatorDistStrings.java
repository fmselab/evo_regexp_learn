package evolutionary_repair.fitnesseval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import regex.distinguishing.DistStringCreator;
import regex.oracle.CachedOracle;

/**
 * evaluates the regular expressions by using distinguishing strings
 */
public class RegExpEvaluatorDistStrings extends RegExpEvaluatorFactory {
	public static RegExpEvaluatorFactory dsWithMonitoring = new RegExpEvaluatorDistStrings(true);
	public static RegExpEvaluatorFactory dsNoMonitoring = new RegExpEvaluatorDistStrings(false);
	public static GENERATION_POLICY GEN_POL = GENERATION_POLICY.ALL;

	private RegExpEvaluatorDistStrings(boolean monitoring) {
		this.monitoring = monitoring;
	}

	@Override
	public RegExpEvaluator buildEvaluator(CachedOracle oracle, StringGenerationTerminationCond term) {
		return new RegExpEvaluatorDistS(oracle, term, monitoring);
	}

	public class RegExpEvaluatorDistS extends RegExpEvaluator {
		private final Random rnd = new Random();
		private final Logger logger = Logger.getLogger(RegExpEvaluatorDistS.class.getName());
		private boolean monitoring;

		/**
		 * Instantiates a new reg exp evaluator dist strings.
		 *
		 * @param o          the oracle
		 * @param monitoring the monitoring (check if an existing string distinguish the
		 *                   mutant, do not generate that
		 * 
		 */
		private RegExpEvaluatorDistS(CachedOracle o, StringGenerationTerminationCond term, boolean monitoring) {
			super(o, term);
			this.monitoring = monitoring;
		}

		@Override
		public void generateNewStrings(List<? extends RegexWFitness> population) {
			Comparator<RegexWFitness> fitnessComparator = new Comparator<RegexWFitness>() {
				@Override
				public int compare(RegexWFitness o1, RegexWFitness o2) {
					return Double.compare(o2.getFitness(), o1.getFitness());
				}
			};
			// make sure a distinguishing string between each pair in the population
			logger.log(Level.INFO, "start generateNewStrings");
			cond.start();
			// we have to try to reduce the maximum fitness in order to avoid
			// premature termination
			if (GEN_POL == GENERATION_POLICY.FITNESS_POPULATION) {
				genPolFitnessPopulation(population, fitnessComparator);
			} else {
				List<? extends RegexWFitness> pop = new ArrayList<>(population);
				if (GEN_POL == GENERATION_POLICY.SOME || GEN_POL == GENERATION_POLICY.ALL_SORT_FITNESS) {
					Collections.sort(pop, fitnessComparator);
				}
				outerLoop2: for (int i = 0; i < pop.size() - 1; i++) {
					RegexWFitness r1 = pop.get(i);
					if (GEN_POL == GENERATION_POLICY.ALL || GEN_POL == GENERATION_POLICY.ALL_SORT_FITNESS) {
						for (int j = i + 1; j < pop.size(); j++) {
							if (cond.timeout()) {
								break outerLoop2;
							}
							generateWithJ(pop, r1, j);
						}
					}
					else if (GEN_POL == GENERATION_POLICY.ONE) {
						if (cond.timeout())
							break;
						generateWithJ(pop, r1, rnd.nextInt(pop.size() - i - 1) + i + 1);
					} else if (GEN_POL == GENERATION_POLICY.SOME) {
						double oldFitness = r1.getFitness();
						for (int j = i + 1; j < pop.size(); j++) {
							if (cond.timeout()) {
								break outerLoop2;
							}
							generateWithJ(pop, r1, j);
							double newFitness = r1.getFitness();
							if (newFitness < oldFitness) {
								break;
							}
						}
					}
				}
			}
			logger.log(Level.INFO, "end generateNewStrings");
		}

		private void genPolFitnessPopulation(List<? extends RegexWFitness> population, Comparator<RegexWFitness> fitnessComparator) {
			// double populationFitness = getPopulationMaxFitness(population);
			List<? extends RegexWFitness> pop = new ArrayList<>(population);
			Collections.sort(pop, fitnessComparator);
			double populationFitness = pop.get(0).getFitness();
			logger.log(Level.INFO, "populationFitness " + populationFitness);
			outerLoop: for (int i = 0; i < pop.size() - 1; i++) {
				RegexWFitness r1 = pop.get(i);
				for (int j = i + 1; j < pop.size(); j++) {
					if (cond.timeout()) {
						break outerLoop;
					}
					String ds = generateWithJ(pop, r1, j);
					if (ds != null) {
						Collections.sort(pop, fitnessComparator);// TODO sbagliato!!!
						// double newPopulationFitness = getPopulationMaxFitness(pop);
						double newPopulationFitness = pop.get(0).getFitness();
						if (newPopulationFitness < populationFitness) {
							break outerLoop;
						}
					}
				}
			}
		}

		private String generateWithJ(List<? extends RegexWFitness> population, RegexWFitness r1, int j) {
			RegexWFitness r = population.get(j);
			if (r == r1) {
				return null;
			}
			return makeSureDSexists(r1, r, population);
		}

		private String makeSureDSexists(RegexWFitness r1, RegexWFitness r, List<? extends RegexWFitness> population) {
			if (!monitoring || !canBeDistinguished(r, r1)) {
				// logger.log(Level.INFO, "building ds");
				String ds = DistStringCreator.getDS(r1.getmAut(), r.getmAut(), Collections.EMPTY_SET);
				// there is no distinguishing string between r and candidate
				if (ds != null) {
					// eval with the oracle (cached)
					boolean accepted = oracle.accept(ds);
					// must be added in any case since it is used to compute fitness
					// r1.addString(ds, accepted);
					// r.addString(ds, accepted);
					for (RegexWFitness m : population) {
						// note that addString now also updates the fitness
						m.addString(ds, accepted);
					}
					return ds;
				}
			}
			return null;
		}

		@Override
		public String getStringGenName() {
			return "DS";
		}
	}
}
