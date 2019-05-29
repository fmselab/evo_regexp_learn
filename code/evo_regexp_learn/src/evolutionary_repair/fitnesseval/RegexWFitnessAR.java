package evolutionary_repair.fitnesseval;

import dk.brics.automaton.RegExp;
import regex.oracle.CachedOracle;

/**
 * regular expression +: - positive and negative automata - strings that are
 * correctly accepted or rejected by r - strings that are wrongly accepted or
 * rejected by r
 * 
 * - in this way, I can know quickly the regex fitness
 * 
 * @author garganti
 *
 */
public class RegexWFitnessAR extends RegexWFitness {

	public RegexWFitnessAR(RegExp m, CachedOracle o) {
		super(m, o);
	}

	@Override
	protected void updateFitness() {
		int correct = strings.get(StringStatus.CORR_ACC).size() + strings.get(StringStatus.CORR_REJ).size();
		int wrong = strings.get(StringStatus.WRO_REJ).size() + strings.get(StringStatus.WRO_ACC).size();
		fitness = correct / ((double) wrong + correct);
	}

}
