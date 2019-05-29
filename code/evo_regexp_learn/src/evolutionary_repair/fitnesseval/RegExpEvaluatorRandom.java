package evolutionary_repair.fitnesseval;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import regex.oracle.CachedOracle;

/**
 * Generates random strings
 * 
 */
public class RegExpEvaluatorRandom extends RegExpEvaluatorFactory {
	public static RegExpEvaluatorFactory rndNoMonitoring = new RegExpEvaluatorRandom(false);
	public static RegExpEvaluatorFactory rndMonitoring = new RegExpEvaluatorRandom(true);
	public static long numOfStrings = 10000;
	public static GENERATION_POLICY genPolicy = GENERATION_POLICY.RND_POP;

	private RegExpEvaluatorRandom(boolean monitoring) {
	}

	@Override
	public RegExpEvaluator buildEvaluator(CachedOracle oracle, StringGenerationTerminationCond term) {
		return new RegExpEvaluatorR(oracle, term);
	}

	private static Random rnd;

	public class RegExpEvaluatorR extends RegExpEvaluator {
		public final Logger logger = Logger.getLogger(RegExpEvaluatorR.class.getName());

		RegExpEvaluatorR(CachedOracle o, StringGenerationTerminationCond term) {
			super(o, term);
			rnd = new Random();
		}

		@Override
		public void generateNewStrings(List<? extends RegexWFitness> population) {
			int populationSize = population.size();

			switch (genPolicy) {
			case RND_POP:
			case RND_POP_MON:
				numOfStrings = (populationSize * (populationSize - 1)) / 2;
				break;
			default:
				throw new Error(genPolicy + " not implemented!");
			}

			cond.start();
			// oldCode();
			outerLoop: for (int i = 0; i < population.size() - 1; i++) {
				for (int j = i + 1; j < population.size(); j++) {
					if (cond.timeout()) {
						logger.log(Level.INFO, "timeout in string generation");
						break;
					}
					if (monitoring && canBeDistinguished(population.get(i), population.get(j))) {
						continue;
					}

					int length = rnd.nextInt(100);
					StringBuilder sb = new StringBuilder(length);
					for (int k = 0; k < length; k++) {
						sb.append((char) (rnd.nextInt(94) + 33));
					}
					String s = sb.toString();
					boolean accepted = oracle.accept(s);
					logger.log(Level.INFO, "generated string: " + s);
					logger.log(Level.INFO, "Start updating the population with the new string");
					for (RegexWFitness p : population) {
						p.addString(s, accepted);
						/*
						 * if(cond.timeout()) { logger.log(Level.INFO, "timeout in fitness update");
						 * break loopStrings; }
						 */
					}
					logger.log(Level.INFO, "End of population update");
				}
			}
		}

		private void oldCode(List<? extends RegexWFitness> population) {
			loopStrings: for (long i = 0; i < numOfStrings; i++) {
				if (cond.timeout()) {
					logger.log(Level.INFO, "timeout in string generation");
					break;
				}
				int length = rnd.nextInt(100);
				StringBuilder sb = new StringBuilder(length);
				for (int j = 0; j < length; j++) {
					sb.append((char) (rnd.nextInt(94) + 33));
				}
				String s = sb.toString();
				boolean accepted = oracle.accept(s);
				logger.log(Level.INFO, "generated string: " + s);
				logger.log(Level.INFO, "Start updating the population with the new string");
				for (RegexWFitness p : population) {
					p.addString(s, accepted);
					/*
					 * if(cond.timeout()) { logger.log(Level.INFO, "timeout in fitness update");
					 * break loopStrings; }
					 */
				}
				logger.log(Level.INFO, "End of population update");
			}
		}

		@Override
		public String getStringGenName() {
			return "RND";
		}
	}
}
