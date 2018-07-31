package uk.davidwei.perfmock.syntax;

import uk.davidwei.perfmock.api.Action;


public interface ActionClause {
    public abstract void will(Action action);
}
