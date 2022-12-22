/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.structure.term.function;

import de.featjar.formula.structure.term.ITerm;

import java.util.List;

/**
 * Adds the values of two integer terms.
 *
 * @author Sebastian Krieter
 * @deprecated currently not supported by any meaningful operations
 */
@Deprecated
public class IntegerAdd extends AAdd {

    protected IntegerAdd() {
    }

    public IntegerAdd(ITerm leftTerm, ITerm rightTerm) {
        super(leftTerm, rightTerm);
    }

    public IntegerAdd(List<ITerm> arguments) {
        super(arguments);
    }

    @Override
    public Class<Long> getType() {
        return Long.class; // TODO: unintuitive that IntegerAdd requires Long, not Integer (same for real)
    }

    @Override
    public Class<Long> getChildrenType() {
        return Long.class;
    }

    @Override
    public Long evaluate(List<?> values) {
        return IFunction.reduce(values, Long::sum);
    }

    @Override
    public IntegerAdd cloneNode() {
        return new IntegerAdd();
    }
}
