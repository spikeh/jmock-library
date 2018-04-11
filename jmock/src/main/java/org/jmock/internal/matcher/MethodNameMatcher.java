/**
 * 
 */
package org.jmock.internal.matcher;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class MethodNameMatcher extends TypeSafeMatcher<Method> {
    Pattern namePattern;
    
    public MethodNameMatcher(String nameRegex) {
        namePattern = Pattern.compile(nameRegex);
    }
    
    @Override
    public boolean matchesSafely(Method method) {
        return namePattern.matcher(method.getName()).matches();
    }
    
    @Override
    protected void describeMismatchSafely(Method item, Description mismatchDescription) {
        mismatchDescription.appendText("was method ").appendText(item.getName());
    }
    
    public void describeTo(Description description) {
        description.appendText(namePattern.toString());
    }

}