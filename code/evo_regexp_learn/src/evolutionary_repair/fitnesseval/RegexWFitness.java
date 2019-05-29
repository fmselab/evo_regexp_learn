package evolutionary_repair.fitnesseval;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import dk.brics.automaton.RegExp;
import regex.mutrex.ds.RegexWAutomata;
import regex.oracle.CachedOracle;

/**
 * regular expression with fitness   regular expression +: - positive and negative automata - strings that are
 * correctly accepted or rejected by r - strings that are wrongly accepted or
 * rejected by r
 * @author garganti
 *
 */
public abstract class RegexWFitness extends RegexWAutomata {

	public enum StringStatus {
		CORR_ACC, CORR_REJ, WRO_ACC, WRO_REJ, REJECTED, ACCEPTED
	}
	
	/**
	 * Creates the regex W fitness.
	 *
	 * @param m the m
	 * @param o the o
	 * @param considerLength the consider length
	 * @return the regex W fitness
	 */
	public static RegexWFitness createRegexWFitness(RegExp m, CachedOracle o, boolean considerLength) {
		return considerLength ? new RegexWFitnessSmooth(m, o) : new RegexWFitnessAR(m, o);
	}

	private CachedOracle oracle;

	// it is not a partition
	protected Map<StringStatus, Set<String>> strings = new HashMap<>();
	protected double fitness;

	protected RegexWFitness(RegExp m, CachedOracle o) {
		super(m);
		oracle = o;
		this.strings = new HashMap<>();
		Set<String> CORR_ACCSet = Collections.synchronizedSet(new HashSet<>());
		Set<String> CORR_REJSet = Collections.synchronizedSet(new HashSet<>());
		Set<String> WRO_ACCSet = Collections.synchronizedSet(new HashSet<>());
		Set<String> WRO_REJSet = Collections.synchronizedSet(new HashSet<>());
		updateFromOracle(CORR_ACCSet, CORR_REJSet, WRO_ACCSet, WRO_REJSet);
		strings.put(StringStatus.CORR_ACC, CORR_ACCSet);
		strings.put(StringStatus.CORR_REJ, CORR_REJSet);
		strings.put(StringStatus.WRO_ACC, WRO_ACCSet);
		strings.put(StringStatus.WRO_REJ, WRO_REJSet);
		// now the rejected and accepted
		// useful instead of returning every time the union
		CORR_ACCSet.addAll(WRO_ACCSet);
		strings.put(StringStatus.ACCEPTED, CORR_ACCSet);
		CORR_REJSet.addAll(WRO_REJSet);
		strings.put(StringStatus.REJECTED, CORR_REJSet);
		updateFitness();
	}

	private void updateFromOracle(Set<String> CORR_ACCSet, Set<String> CORR_REJSet, Set<String> WRO_ACCSet,
			Set<String> WRO_REJSet) {
		for (String s : oracle.getAcceptedByOracle()) {
			if (getmAut().run(s))
				CORR_ACCSet.add(s);
			else
				WRO_REJSet.add(s);
		}
		for (String s : oracle.getRejectedByOracle()) {
			if (getmAut().run(s))
				WRO_ACCSet.add(s);
			else
				CORR_REJSet.add(s);
		}
	}

	/**
	 * 
	 * @param ds
	 * @param accepted by the oracle
	 */
	void addString(String ds, boolean acceptedOracle) {
		boolean acc = getmAut().run(ds);
		if (acc) {
			strings.get(StringStatus.ACCEPTED).add(ds);
		} else {
			strings.get(StringStatus.REJECTED).add(ds);
		}
		// now also in wrong/correct
		if (acc && acceptedOracle)
			strings.get(StringStatus.CORR_ACC).add(ds);
		else if (!acc && acceptedOracle)
			strings.get(StringStatus.WRO_REJ).add(ds);
		else if (acc && !acceptedOracle)
			strings.get(StringStatus.WRO_ACC).add(ds);
		else if (!acc && !acceptedOracle)
			strings.get(StringStatus.CORR_REJ).add(ds);
		else
			assert false;
		updateFitness();
	}

	abstract protected void updateFitness();

	/** get the strings with a certain status */
	public Collection<? extends String> get(StringStatus words) {
		return strings.get(words);
	}

	public double getFitness() {
		return fitness;
	}
}
