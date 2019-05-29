package evolutionary_repair;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.GenerationalEvolutionEngine;
import org.uncommons.watchmaker.framework.PopulationData;
import org.uncommons.watchmaker.framework.SelectionStrategy;
import org.uncommons.watchmaker.framework.TerminationCondition;
import org.uncommons.watchmaker.framework.selection.TruncationSelection;
import org.uncommons.watchmaker.framework.termination.ElapsedTime;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RegExpCounter;
import evolutionary_repair.fitnesseval.RegExpEvaluator;
import evolutionary_repair.fitnesseval.RegExpEvaluatorFactory;
import evolutionary_repair.fitnesseval.RegexWFitness;
import evolutionary_repair.fitnesseval.StringGenerationTerminationCond;
import evolutionary_repair.fitnesseval.RegexWFitness.StringStatus;
import regex.oracle.CachedOracle;
import regex.oracle.RegexOracle;

public class EvoRegexpRepairProcess {
	private static Logger logger = Logger.getLogger(EvoRegexpRepairProcess.class.getName());
	private int elite;
	private CachedOracle oracle;
	private int genNumber = 1;
	private GenerationalEvolutionEngine<RegexWFitness> engine;
	private Random rnd = new Random();
	private RegExpEvaluator fitnessEvaluator;
	private int populationSize;
	private ElapsedTime timeTermCond;// timeout

	/**
	 * Instantiates a new regexp repair process.
	 *
	 * @param candidate            the candidate
	 * @param ro                   the ro
	 * @param populationSizeFactor TODO
	 * @param fitEvalClass         the fit eval class
	 * @param stop the stop
	 * @param timeoutInMillis the timeout in millis
	 */
	public EvoRegexpRepairProcess(RegExp candidate, RegexOracle ro, int populationSizeFactor, RegExpEvaluatorFactory fitEvalClass,
			StringGenerationTerminationCond stop, int timeoutInMillis) {
		timeTermCond = new ElapsedTime(timeoutInMillis);
		oracle = new CachedOracle(ro);
		// TODO calcolare la size della popolazione in base a qualche parametro
		// populationSize = populationSizeFactor * (candidate.countOperators() +
		// candidate.getOriginalRgxAsStr().length());
		populationSize = populationSizeFactor * RegExpCounter.countOperators(candidate);
		logger.log(Level.INFO, "populationSize " + populationSize);
		/*
		 * if (populationSizeFactor < 200) { populationSizeFactor = 400; }
		 */
		elite = 3;
		//
		RegexWFitness candidatewa = RegexWFitness.createRegexWFitness(candidate, oracle, false);
		CandidateFactoryRegExp candidateFactory = new CandidateFactoryRegExp(candidatewa, oracle);
		//
		EvolutionaryByMutationDeepRandom evolutionaryOperator = new EvolutionaryByMutationDeepRandom(oracle, timeoutInMillis);
		//
		fitnessEvaluator = fitEvalClass.buildEvaluator(oracle, stop);
		// SelectionStrategy<Object> selectionStrategy = new RouletteWheelSelection();
		SelectionStrategy<Object> selectionStrategy = new TruncationSelection(.2d);
		engine = new GenerationalEvolutionEngine<RegexWFitness>(candidateFactory, evolutionaryOperator, fitnessEvaluator,
				selectionStrategy, rnd);
		engine.setSingleThreaded(true);
		// add the string generator
		engine.addEvolutionObserver(new EvolutionObserver<RegexWFitness>() {
			@Override
			public void populationUpdate(PopulationData<? extends RegexWFitness> data) {
				logger.log(Level.INFO,
						"Generation " + data.getGenerationNumber() + ": best " + data.getBestCandidate()
								+ " n.evaluated " + oracle.getNumEvaluatedStrings() + " getPopulationSize "
								+ data.getPopulationSize());
				logger.log(Level.INFO, "fitness " + data.getBestCandidateFitness());
				RegexWFitness cand = data.getBestCandidate();
				for (StringStatus kind : StringStatus.values()) {
					logger.log(Level.INFO, kind + " => " + cand.get(kind).size());
				}
				genNumber = data.getGenerationNumber();
				fitnessEvaluator.computeNewStrings = true;
			}
		});
	}

	/**
	 * Run.
	 * @return the regex plus
	 */
	public RegexWFitness run() {
		return run(new TerminationCondition[] { timeTermCond });
	}

	private RegexWFitness run(TerminationCondition[] terminationConditions) {
		return engine.evolve(populationSize, elite, terminationConditions);
	}

	
	public List<TerminationCondition> getSatisfiedTerminationConditions() {
		return engine.getSatisfiedTerminationConditions();
	}

	/** number of generation steps */
	public int getGenNumber() {
		return genNumber;
	}

	public int getNumEvaluatedStrings() {
		return oracle.getNumEvaluatedStrings();
	}

	public String getFitEvalName() {
		return fitnessEvaluator.getStringGenName();
	}

	public String getEvaluatedStrings() {
		return oracle.getAcceptedByOracle().toString() + oracle.getRejectedByOracle();
	}
}
