package evolutionary_repair.fitnesseval;

public enum GENERATION_POLICY {
	ALL,//a string for each couple of mutants
	ALL_SORT_FITNESS, ONE, SOME, FITNESS_POPULATION,
	RND_POP,//computed using the population size
	RND_POP_MON,//computed using the population size and with monitoring
	RND_FIXED,//a fixed number of strings TODO
	RND_PER_RGX,//same strings generated with DS policy TODO
	OLD_APPROACH_ICTSS;
}