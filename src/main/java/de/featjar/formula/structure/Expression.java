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
package de.featjar.formula.structure;

import de.featjar.base.tree.structure.Traversable;
import de.featjar.formula.structure.assignment.Assignment;
import de.featjar.formula.visitor.ValueVisitor;

import java.util.List;
import java.util.function.Predicate;

/**
 * An expression in propositional or first-order logic.
 * Implemented recursively as a tree of expressions.
 * An expression is either a {@link de.featjar.formula.structure.formula.Formula}
 * or a {@link de.featjar.formula.structure.term.Term}.
 *
 * @author Sebastian Krieter
 * @author Elias Kuiter
 */
public interface Expression extends Traversable<Expression> {
    /**
     * {@return the name of this expression's operator}
     */
    String getName();

    /**
     * {@return the type this expression evaluates to}
     */
    Class<?> getType();

    /**
     * {@return the evaluation of this expression on a given list of values}
     *
     * @param values the values
     */
    Object evaluate(List<?> values);

    /**
     * {@return the evaluation of this formula on a given assignment}
     *
     * @param assignment the assignment
     */
    default Object evaluate(Assignment assignment) {
        return traverse(new ValueVisitor(assignment)).orElse(null);
    }

    /**
     * {@return the type this expression's children must evaluate to, if any}
     */
    default Class<?> getChildrenType() {
        return null;
    }

    @Override
    default Predicate<Expression> getChildrenValidator() {
        return expression -> getChildrenType() == null || getChildrenType().isAssignableFrom(expression.getType());
    }
}
