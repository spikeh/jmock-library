package uk.davidwei.perfmock.syntax;

import uk.davidwei.perfmock.internal.State;
import uk.davidwei.perfmock.internal.StatePredicate;


public interface StatesClause {

    State is(String name);

    StatePredicate isNot(String name);

}
