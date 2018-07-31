package uk.davidwei.perfmock.auto.internal;

import uk.davidwei.perfmock.Mockery;
import uk.davidwei.perfmock.Sequence;
import uk.davidwei.perfmock.States;
import uk.davidwei.perfmock.auto.Auto;
import uk.davidwei.perfmock.auto.Mock;
import uk.davidwei.perfmock.internal.AllDeclaredFields;

import java.lang.reflect.Field;
import java.util.List;


public class Mockomatic {
    private final Mockery mockery;

    public Mockomatic(Mockery mockery) {
        this.mockery = mockery;
    }

    public void fillIn(Object object) {
        fillIn(object, AllDeclaredFields.in(object.getClass()));
    }

    public void fillIn(Object object, final List<Field> knownFields) {
        for (Field field : knownFields) {
            if (field.isAnnotationPresent(Mock.class)) {
                autoMock(object, field);
            }
            else if (field.isAnnotationPresent(Auto.class)) {
                autoInstantiate(object, field);
            }
        }
    }

    private void autoMock(Object object, Field field) {
        setAutoField(field, object, 
                     mockery.mock(field.getType(), field.getName()),
                     "auto-mock field " + field.getName());
    }

    private void autoInstantiate(Object object, Field field) {
        final Class<?> type = field.getType();
        if (type == States.class) {
            autoInstantiateStates(field, object);
        }
        else if (type == Sequence.class) {
            autoInstantiateSequence(field, object);
        }
        else {
            throw new IllegalStateException("cannot auto-instantiate field of type " + type.getName());
        }
    }

    private void autoInstantiateStates(Field field, Object object) {
        setAutoField(field, object, 
                     mockery.states(field.getName()), 
                     "auto-instantiate States field " + field.getName());
    }
    
    private void autoInstantiateSequence(Field field, Object object) {
        setAutoField(field, object, 
                     mockery.sequence(field.getName()), 
                     "auto-instantiate Sequence field " + field.getName());
    }

    private void setAutoField(Field field, Object object, Object value, String description) {
        try {
            field.setAccessible(true);
            field.set(object, value);
        }
        catch (IllegalAccessException e) {
            throw new IllegalStateException("cannot " + description, e);
        }
    }

}
