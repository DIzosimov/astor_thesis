package fr.inria.astor.core.solutionsearch.population;

import fr.inria.astor.core.entities.validation.TestCaseVariantValidationResult;
import fr.inria.astor.core.entities.validation.VariantValidationResult;
import fr.inria.astor.core.entities.ProgramVariant;

/**
 * Fitness function based on test suite execution.
 * 
 * @author Matias Martinez
 *
 */
public class TestCaseFitnessFunction implements FitnessFunction {
	private static final double EDIT_PENALTY = 0.01;

	/**
	 * In this case the fitness value is associate to the failures: LESS FITNESS
	 * is better.
	 */
	public double calculateFitnessValue(VariantValidationResult validationResult) {

		if (validationResult == null)
			return this.getWorstMaxFitnessValue();

		TestCaseVariantValidationResult result = (TestCaseVariantValidationResult) validationResult;
		return result.getFailureCount();
	}

	public double calculateFitnessValue(ProgramVariant variant, VariantValidationResult validationResult){
			double fitness = calculateFitnessValue(validationResult);

      int editCount = variant.getOperations().size();  // Assuming this method returns the number of edits
      fitness += EDIT_PENALTY * editCount;

      return fitness;
	}

	public double getWorstMaxFitnessValue() {

		return Double.MAX_VALUE;
	}

}
