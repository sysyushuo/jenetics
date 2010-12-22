/*
 * Java Genetic Algorithm Library (@!identifier!@).
 * Copyright (c) @!year!@ Franz Wilhelmstötter
 *  
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Author:
 * 	 Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 * 	 
 */
package org.jenetics;

import static java.lang.Double.NaN;
import static java.lang.Double.doubleToLongBits;
import static java.lang.String.format;

import java.util.List;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.jenetics.stat.Variance;
import org.jenetics.util.Accumulators;
import org.jenetics.util.Accumulators.MinMax;
import org.jscience.mathematics.number.Float64;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @version $Id$
 */
public class NumberStatistics<
	G extends Gene<?, G>, 
	R extends Number & Comparable<? super R>
>
	extends Statistics<G, R> 
{
	
	/**
	 * Builder for the NumberStatistics class.
	 * 
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
	 * @version $Id$
	 */
	public static class Builder<
		G extends Gene<?, G>, 
		R extends Number & Comparable<? super R>
	>
		extends Statistics.Builder<G, R>
	{
		protected double _fitnessMean = NaN;
		protected double _fitnessVariance = NaN;
		protected double _standardError = NaN;
		
		/**
		 * Create a new NumberStatistics builder.
		 */
		public Builder() {
		}
		
		@Override
		public Builder<G, R> statistics(final Statistics<G, R> statistics) {
			super.statistics(statistics);
			return this;
		}
		
		/**
		 * Set the values of this builder with the values of the given 
		 * {@code statistics}.
		 * 
		 * @param statistics the statistics values. If the {@code statistics}
		 *        is {@code null} nothing is set.
		 * @return this builder.
		 */
		public Builder<G, R> statistics(final NumberStatistics<G, R> statistics) {
			if (statistics != null) {
				super.statistics(statistics);
				_fitnessMean = statistics._fitnessMean;
				_fitnessVariance = statistics._fitnessVariance;
				_standardError = statistics._standardError;
			}
			return this;
		}
		
		/**
		 * @see NumberStatistics#getFitnessMean()
		 */
		public Builder<G, R> fitnessMean(final double fitnessMean) {
			_fitnessMean = fitnessMean;
			return this;
		}
		
		/**
		 * @see NumberStatistics#getFitnessVariance()
		 */
		public Builder<G, R> fitnessVariance(final double fitnessVariance) {
			_fitnessVariance = fitnessVariance;
			return this;
		}
		
		/**
		 * @see NumberStatistics#getStandardError()
		 */
		public Builder<G, R> standardError(final double standardError) {
			_standardError = standardError;
			return this;
		}
		
		@Override
		public NumberStatistics<G, R> build() {
			return new NumberStatistics<G, R>(
					_generation,
					_best,
					_worst,
					_fitnessMean,
					_fitnessVariance,
					_samples,
					_ageMean,
					_ageVariance,
					_standardError,
					_invalid,
					_killed
				);
		}
	}
	
	private static final long serialVersionUID = 1L;

	protected final double _fitnessMean;
	protected final double _fitnessVariance;
	protected final double _standardError;

	protected NumberStatistics(
		final int generation,
		final Phenotype<G, R> best, 
		final Phenotype<G, R> worst,
		final double fitnessMean, 
		final double fitnessVariance,
		final int samples, 
		final double ageMean, 
		final double ageVariance,
		final double errorOfMean,
		final int invalid,
		final int killed
	) {
		super(
				generation, 
				best, 
				worst, 
				samples, 
				ageMean, 
				ageVariance, 
				invalid, 
				killed
			);

		_fitnessMean = fitnessMean;
		_fitnessVariance = fitnessVariance;
		_standardError = errorOfMean;
	}

	/**
	 * Return the mean of the fitness values.
	 * 
	 * @return the mean of the fitness values.
	 */
	public double getFitnessMean() {
		return _fitnessMean;
	}

	/**
	 * Return the variance of the fitness values.
	 * 
	 * @return the variance of the fitness values.
	 */
	public double getFitnessVariance() {
		return _fitnessVariance;
	}

	/**
	 * Return the <a href="https://secure.wikimedia.org/wikipedia/en/wiki/Standard_error_%28statistics%29">
	 * Standard error
	 * </a> of the calculated fitness mean.
	 * 
	 * @return the standard error of the calculated fitness mean.
	 */
	public double getStandardError() {
		return _standardError;
	}

	@Override
	public int hashCode() {
		int hash = super.hashCode() * 31 + 17;
		hash += (int) doubleToLongBits(_fitnessMean) * 31 + 17;
		hash += (int) doubleToLongBits(_fitnessVariance) * 31 + 17;
		hash += (int) doubleToLongBits(_standardError) * 31 + 17;
		return hash;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof NumberStatistics<?, ?>)) {
			return false;
		}

		final NumberStatistics<?, ?> statistics = (NumberStatistics<?, ?>) obj;

		return doubleToLongBits(statistics._fitnessMean) == doubleToLongBits(_fitnessMean)
				&& doubleToLongBits(statistics._fitnessVariance) == doubleToLongBits(_fitnessVariance)
				&& doubleToLongBits(statistics._standardError) == doubleToLongBits(_standardError);
	}

	@Override
	public String toString() {
		final String fpattern = "| %28s: %-26.11f|\n";

		final StringBuilder out = new StringBuilder();
		out.append(super.toString()).append("\n");
		out.append("+---------------------------------------------------------+\n");
		out.append("|  Fitness Statistics                                     |\n");
		out.append("+---------------------------------------------------------+\n");
		out.append(format(fpattern, "Fitness mean", _fitnessMean));
		out.append(format(fpattern, "Fitness variance", _fitnessVariance));
		out.append(format(fpattern, "Fitness error of mean", _standardError));
		out.append("+---------------------------------------------------------+");

		return out.toString();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static final XMLFormat<NumberStatistics> XML = 
		new XMLFormat<NumberStatistics>(NumberStatistics.class) 
	{
		private static final String FITNESS_MEAN = "fitness-mean";
		private static final String FITNESS_VARIANCE = "fitness-variance";
		private static final String ERROR_OF_MEAN = "error-of-mean";

		@Override
		public NumberStatistics newInstance(
			final Class<NumberStatistics> cls,
			final InputElement xml
		) 
			throws XMLStreamException 
		{
			final Statistics stats = Statistics.XML.newInstance(
					Statistics.class, xml
				);
			final Float64 fitnessMean = xml.get(FITNESS_MEAN);
			final Float64 fitnessVariance = xml.get(FITNESS_VARIANCE);
			final Float64 errorOfMean = xml.get(ERROR_OF_MEAN);

			final Builder builder = new Builder().statistics(stats);
			builder.fitnessMean(fitnessMean.doubleValue());
			builder.fitnessVariance(fitnessVariance.doubleValue());
			builder.standardError(errorOfMean.doubleValue());
			
			return builder.build();
		}

		@Override
		public void write(final NumberStatistics s, final OutputElement xml)
			throws XMLStreamException 
		{
			Statistics.XML.write(s, xml);
			xml.add(Float64.valueOf(s.getFitnessMean()), FITNESS_MEAN);
			xml.add(Float64.valueOf(s.getFitnessVariance()), FITNESS_VARIANCE);
			xml.add(Float64.valueOf(s.getStandardError()), ERROR_OF_MEAN);
		}

		@Override
		public void read(final InputElement xml, final NumberStatistics p)
			throws XMLStreamException 
		{
		}
	};

	/**
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz
	 *         Wilhelmstötter</a>
	 * @version $Id$
	 */
	public static class Calculator<
		G extends Gene<?, G>, 
		R extends Number & Comparable<R>
	>
		extends Statistics.Calculator<G, R> 
	{

		public Calculator() {
		}

		@Override
		public NumberStatistics.Builder<G, R> evaluate(
			final List<? extends Phenotype<G, R>> population,
			final int generation, 
			final Optimize opt
		) {
			final Builder<G, R> builder = new Builder<G, R>();
			builder.generation(generation);

			if (!population.isEmpty()) {
				// The statistics accumulators.
				final MinMax<Phenotype<G, R>> minMax = new MinMax<Phenotype<G, R>>();
				final Variance<Integer> ageVariance = new Variance<Integer>();
				final Variance<R> fitnessVariance = new Variance<R>();

				Accumulators.<Phenotype<G, R>>accumulate(
						population, 
						minMax,
						ageVariance.adapt(Phenotype.<G, R>Age(generation)),
						fitnessVariance.adapt(Phenotype.<G, R>Fitness())
					);

				builder.bestPhenotype(opt.best(minMax.getMax(), minMax.getMin()));
				builder.worstPhenotype(opt.worst(minMax.getMax(), minMax.getMin()));
				builder.fitnessMean(fitnessVariance.getMean());
				builder.fitnessVariance(fitnessVariance.getVariance());
				builder.samples(population.size());
				builder.ageMean(ageVariance.getMean());
				builder.ageVariance(ageVariance.getVariance());
				builder.standardError(fitnessVariance.getStandardError());
			}

			return builder;
		}
	}

}
