package uk.davidwei.perfmock.syntax;

import org.hamcrest.Matcher;

public interface ArgumentConstraintPhrases {
    <T> T with(Matcher<T> matcher);
}
