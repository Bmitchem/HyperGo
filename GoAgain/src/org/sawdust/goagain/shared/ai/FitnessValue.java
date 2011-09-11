package org.sawdust.goagain.shared.ai;

public class FitnessValue {
  public static final FitnessValue MIN_VALUE = new FitnessValue(-1000000, 0);
  public static final FitnessValue MAX_VALUE = new FitnessValue(1000000, 0);
  public final double fitness;
  public final double uncertianty;
  
  public FitnessValue(double fitness, double uncertianty) {
    super();
    this.fitness = fitness;
    this.uncertianty = uncertianty;
  }
  
  public static FitnessValue avg(FitnessValue[] fitnessValues) {
    double fitness = 0;
    double uncertianty = 0;
    for(FitnessValue v : fitnessValues)
    {
      fitness += v.fitness;
      uncertianty += v.fitness / v.uncertianty;
    }
    fitness /= fitnessValues.length;
    return new FitnessValue(fitness, fitness / uncertianty);
  }
}
