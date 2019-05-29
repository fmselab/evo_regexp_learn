package evolutionary_repair.fitnesseval;

import dk.brics.automaton.MeasuredRunner;
import dk.brics.automaton.RegExp;
import regex.oracle.CachedOracle;

/**
 * regular expression with fitness in which the fitness counts the length of the
 * accepted chars
 * 
 * @author garganti
 *
 */
public class RegexWFitnessSmooth extends RegexWFitness {

	public RegexWFitnessSmooth(RegExp m, CachedOracle o) {
		super(m, o);
	}

	@Override
	protected void updateFitness() {
		// consider all the string that are wrongly rejected but they should be
		// accepted. StringStatus.WRO_REJ
		// the shorter the worse
		double wrongnessWROREJ = 0;
		for (String wr : strings.get(StringStatus.WRO_REJ)) {
			MeasuredRunner.RunResult res = MeasuredRunner.run(mAut, wr);
			assert !res.isAccept();
			double l = wr.length();
			double nC = res.getnChar();
			assert nC <= l;
			// example
			// nC = 5, length = 10 -> 1 - 0.8 * (5/10) -> 0.6
			// nC = 0, length = 10 -> 1 - 0.8 * (0/10) -> 1
			// nC = 10, length = 10 -> 1 - 0.8 * (10/10) -> 0.2
			wrongnessWROREJ += 1 - 0.8 * (nC / l);
		}
		int correct = strings.get(StringStatus.CORR_ACC).size() + strings.get(StringStatus.CORR_REJ).size();
		double wrong = wrongnessWROREJ + strings.get(StringStatus.WRO_ACC).size();
		fitness = correct / (wrong + correct);
	}

}
