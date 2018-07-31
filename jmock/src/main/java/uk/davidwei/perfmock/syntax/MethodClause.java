package uk.davidwei.perfmock.syntax;

import org.hamcrest.Matcher;

import java.lang.reflect.Method;

public interface MethodClause {
    ParametersClause method(Matcher<Method> methodMatcher);
    ParametersClause method(String nameRegex);
}
