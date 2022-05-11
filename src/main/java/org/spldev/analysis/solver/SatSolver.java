/* -----------------------------------------------------------------------------
 * Formula Lib - Library to represent and edit propositional formulas.
 * Copyright (C) 2021-2022  Sebastian Krieter
 * 
 * This file is part of Formula Lib.
 * 
 * Formula Lib is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * Formula Lib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Formula Lib.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * See <https://github.com/skrieter/formula> for further information.
 * -----------------------------------------------------------------------------
 */
package org.spldev.analysis.solver;

/**
 * Sat solver interface.
 *
 * @author Sebastian Krieter
 */
public interface SatSolver extends Solver {

	/**
	 * Possible outcomes of a satisfiability solver call.<br>
	 * One of {@code TRUE}, {@code FALSE}, or {@code TIMEOUT}.
	 *
	 * @author Sebastian Krieter
	 */
	enum SatResult {
		FALSE, TIMEOUT, TRUE
	}

	/**
	 * Checks whether there is a satisfying solution considering the clauses of the
	 * solver.
	 *
	 * @return A {@link SatResult}.
	 */
	SatResult hasSolution();

}
