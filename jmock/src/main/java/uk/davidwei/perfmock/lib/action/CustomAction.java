/*  Copyright (c) 2000-2004 jMock.org
 */
package uk.davidwei.perfmock.lib.action;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;


/**
 * A partial implementation of the {@link Action} interface that makes it convenient 
 * to implement application-specific actions with inline anonymous classes.
 */
public abstract class CustomAction implements Action {
    private String description;
    
    public CustomAction(String description) {
        this.description = description;
    }
    
    public void describeTo(Description description) {
        description.appendText(this.description);
    }
}
