package evolutionary_repair.fitnesseval;

import java.util.HashSet;
import java.util.Set;

import regex.oracle.CachedOracle;

public abstract class RegExpEvaluatorFactory {
	protected boolean monitoring;

	public abstract RegExpEvaluator buildEvaluator(CachedOracle oracle, StringGenerationTerminationCond term);

	/** return true if there is already a string that can distinguish r and r1 */
	protected boolean canBeDistinguished(RegexWFitness r1, RegexWFitness r2) {
		assert r1 != r2;
		// for each pair of regular expressions in the population there must exist
		// a distinguishing string between the two
		// intersection: accepted by r2 and rejected by r1
		Set<String> accC = new HashSet<String>(r2.get(RegexWFitness.StringStatus.ACCEPTED));
		// delete all those rejected
		accC.retainAll(r1.get(RegexWFitness.StringStatus.REJECTED));
		// there is already a distinguishing string
		if (accC.size() > 0) {
			return true;
		}
		// rejected by r2 and accepted by r1
		Set<String> rejC = new HashSet<String>(r2.get(RegexWFitness.StringStatus.REJECTED));
		// delete all those rejected
		rejC.retainAll(r1.get(RegexWFitness.StringStatus.ACCEPTED));
		if (rejC.size() > 0) {
			return true;
		}
		return false;
	}
}
