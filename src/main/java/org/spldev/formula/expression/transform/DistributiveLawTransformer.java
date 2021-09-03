/* -----------------------------------------------------------------------------
 * Formula Lib - Library to represent and edit propositional formulas.
 * Copyright (C) 2021  Sebastian Krieter
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
package org.spldev.formula.expression.transform;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.spldev.formula.expression.Expression;
import org.spldev.formula.expression.Formula;
import org.spldev.formula.expression.atomic.literal.Literal;
import org.spldev.formula.expression.compound.Compound;

/**
 * Transforms propositional formulas into (clausal) CNF or DNF.
 *
 * @author Sebastian Krieter
 */
public abstract class DistributiveLawTransformer implements Transformer {

	private static class PathElement {
		Expression node;
		List<Expression> newChildren = new ArrayList<>();
		int maxDepth = 0;

		PathElement(Expression node) {
			this.node = node;
		}
	}

	protected static void transfrom(Expression node, Class<? extends Compound> clauseClass,
			Function<Collection<? extends Formula>, Formula> clauseConstructor) {
		if (node != null) {
			final ArrayList<PathElement> path = new ArrayList<>();
			final ArrayDeque<Expression> stack = new ArrayDeque<>();
			stack.addLast(node);
			while (!stack.isEmpty()) {
				final Expression curNode = stack.getLast();
				final boolean firstEncounter = path.isEmpty() || (curNode != path.get(path.size() - 1).node);
				if (firstEncounter) {
					if (curNode instanceof Literal) {
						final PathElement parent = path.get(path.size() - 1);
						parent.newChildren.add(curNode);
						stack.removeLast();
					} else {
						path.add(new PathElement(curNode));
						curNode.getChildren().forEach(stack::addLast);
					}
				} else {
					final PathElement currentElement = path.remove(path.size() - 1);
					curNode.setChildren(currentElement.newChildren);

					if (!path.isEmpty()) {
						final PathElement parentElement = path.get(path.size() - 1);
						parentElement.maxDepth = Math.max(currentElement.maxDepth + 1, parentElement.maxDepth);
					}

					if ((clauseClass == curNode.getClass()) && (currentElement.maxDepth > 0)) {
						final PathElement parentElement = path.get(path.size() - 1);
						parentElement.newChildren.addAll(convert(curNode, clauseConstructor));
						parentElement.maxDepth = 1;
					} else {
						if (!path.isEmpty()) {
							final PathElement parentElement = path.get(path.size() - 1);
							parentElement.newChildren.add(curNode);
						}
					}
					stack.removeLast();
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Formula> convert(Expression child,
			Function<Collection<? extends Formula>, Formula> clauseConstructor) {
		if (child instanceof Literal) {
			return null;
		} else {
			final ArrayList<Set<Literal>> newClauseList = new ArrayList<>();
			final List<Formula> children = new ArrayList<>((List<Formula>) child.getChildren());
			Collections.sort(children, (c1, c2) -> (c1.getChildren().size() - c2.getChildren().size()));
			convertNF(children, newClauseList, new LinkedHashSet<>(children.size() << 1), 0);

			final ArrayList<Formula> filteredClauseList;
			Collections.sort(newClauseList, (c1, c2) -> (c1.size() - c2.size()));
			final ArrayList<Set<Literal>> sets = newClauseList;
			final int lastIndex = sets.size();
			int removeCount = 0;
			for (int i = 0; i < lastIndex; i++) {
				final Set<Literal> set = sets.get(i);
				if (set != null) {
					for (int j = i + 1; j < lastIndex; j++) {
						final Set<Literal> set2 = sets.get(j);
						if (set2 != null) {
							if (set2.containsAll(set)) {
								sets.set(j, null);
								removeCount++;
							}
						}
					}
				}
			}
			filteredClauseList = new ArrayList<>(newClauseList.size() - removeCount);
			for (final Set<Literal> children1 : sets) {
				if (children1 != null) {
					filteredClauseList.add(clauseConstructor.apply(children1));
				}
			}
			return filteredClauseList;
		}
	}

	private static void convertNF(List<Formula> children, List<Set<Literal>> clauses, LinkedHashSet<Literal> literals,
			int index) {
		if (index == children.size()) {
			clauses.add(new HashSet<>(literals));
		} else {
			final Formula child = children.get(index);
			if (child instanceof Literal) {
				final Literal clauseLiteral = (Literal) child;
				if (literals.contains(clauseLiteral)) {
					convertNF(children, clauses, literals, index + 1);
				} else if (!literals.contains(clauseLiteral.flip())) {
					literals.add(clauseLiteral);
					convertNF(children, clauses, literals, index + 1);
					literals.remove(clauseLiteral);
				}
			} else {
				boolean redundant = false;
				for (final Expression grandChild : child.getChildren()) {
					if (grandChild instanceof Literal) {
						if (literals.contains(grandChild)) {
							redundant = true;
							break;
						}
					} else {
						int redundantCount = 0;
						for (final Expression literal : grandChild.getChildren()) {
							if (literals.contains(literal)) {
								redundantCount++;
							}
						}
						if (redundantCount == grandChild.getChildren().size()) {
							redundant = true;
							break;
						}
					}
				}
				if (redundant) {
					convertNF(children, clauses, literals, index + 1);
				} else {
					for (final Expression grandChild : child.getChildren()) {
						if (grandChild instanceof Literal) {
							final Literal clauseLiteral = (Literal) grandChild;
							if (!literals.contains(clauseLiteral.flip())) {
								literals.add(clauseLiteral);
								convertNF(children, clauses, literals, index + 1);
								literals.remove(clauseLiteral);
							}
						} else {
							boolean containsComplement = false;
							for (final Expression literal : grandChild.getChildren()) {
								if (literals.contains(((Literal) literal).flip())) {
									containsComplement = true;
									break;
								}
							}
							if (!containsComplement) {
								final ArrayList<Literal> clauseLiterals = new ArrayList<>(
										grandChild.getChildren().size());
								for (final Expression literal : grandChild.getChildren()) {
									literals.add((Literal) literal);
									clauseLiterals.add((Literal) literal);
								}
								convertNF(children, clauses, literals, index + 1);
								literals.removeAll(clauseLiterals);
							}
						}
					}
				}
			}
		}
	}

}