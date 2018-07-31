/*  Copyright (c) 2000-2004 jMock.org
 */
package uk.davidwei.perfmock.internal;

import org.hamcrest.Description;
import uk.davidwei.perfmock.api.Action;
import uk.davidwei.perfmock.api.Imposteriser;
import uk.davidwei.perfmock.api.Invocation;
import uk.davidwei.perfmock.lib.JavaReflectionImposteriser;

import java.lang.reflect.Array;
import java.util.*;

import static java.lang.reflect.Modifier.isAbstract;


/**
 * Returns a default value for the invoked method's result type.
 * <ul>
 * <li>Returns nothing from void methods.</li>
 * <li>Returns zero or false results for primitive types.</li>
 * <li>Returns zero length instances for arrays and strings.</li>
 * <li>Returns empty instances for collections and maps types in {@link java.util}.</li>
 * <li>Returns imposterised <a
 * href="http://www.c2.com/cgi/wiki?NullObject">Null Objects</a> for
 * types that can be imposterised by the action's {@link Imposteriser}.</li>
 * <li>Otherwise returns <code>null</code>.</li>
 * </ul>
 * The default value can be overridden for specific types.
 * 
 * @author Nat Pryce
 * @author Steve Freeman 2013
 */
public class ReturnDefaultValueAction implements Action {
    private final static Class<?>[] CONCRETE_COLLECTION_TYPES = {
      LinkedList.class,
      TreeSet.class,
      TreeMap.class
    };
    private final Map<Class<?>, Object> resultValuesByType;
    private Imposteriser imposteriser;

    public ReturnDefaultValueAction(Imposteriser imposteriser, Map<Class<?>, Object> typeToResultValue) {
        this.imposteriser = imposteriser;
        this.resultValuesByType = typeToResultValue;
    }

    public ReturnDefaultValueAction(Imposteriser imposteriser) {
      this(imposteriser, createDefaultResults());
    }

    public ReturnDefaultValueAction() {
        this(new JavaReflectionImposteriser());
    }
    
    public void setImposteriser(Imposteriser newImposteriser) {
        this.imposteriser = newImposteriser;
    }
    
    public void describeTo(Description description) {
        description.appendText("returns a default value");
    }

    public void addResult(Class<?> resultType, Object resultValue) {
        resultValuesByType.put(resultType, resultValue);
    }

    public Object invoke(Invocation invocation) throws Throwable {
      final Class<?> returnType = invocation.getInvokedMethod().getReturnType();

      if (resultValuesByType.containsKey(returnType)) {
          return resultValuesByType.get(returnType);
      }
      if (returnType.isArray()) {
          return Array.newInstance(returnType.getComponentType(), 0);
      }
      if (isCollectionOrMap(returnType)) {
        final Object instance = collectionOrMapInstanceFor(returnType);
        if (instance != null) return instance;
      }
      if (imposteriser.canImposterise(returnType)) {
          return imposteriser.imposterise(this, returnType);
      }
      return null;
    }

    private static Object collectionOrMapInstanceFor(Class<?> returnType) throws Throwable {
      return cannotCreateNewInstance(returnType) ? instanceForCollectionType(returnType) : returnType.newInstance();
    }

    private static Object instanceForCollectionType(Class<?> type) throws Throwable {
      for (Class<?> collectionType : CONCRETE_COLLECTION_TYPES) {
        if (type.isAssignableFrom(collectionType)) {
          return collectionType.newInstance();
        }
      }
      return null;
    }

    private static boolean isCollectionOrMap(Class<?> type) {
      return Collection.class.isAssignableFrom(type)
          || Map.class.isAssignableFrom(type);
    }

    private static boolean cannotCreateNewInstance(Class<?> returnType) {
        return returnType.isInterface() || isAbstract(returnType.getModifiers());
    }

    protected static Map<Class<?>, Object> createDefaultResults() {
      final HashMap<Class<?>, Object> result = new HashMap<Class<?>, Object>();
      result.put(boolean.class, Boolean.FALSE);
      result.put(void.class, null);
      result.put(byte.class, (byte) 0);
      result.put(short.class, (short) 0);
      result.put(int.class, 0);
      result.put(long.class, 0L);
      result.put(char.class, '\0');
      result.put(float.class, 0.0F);
      result.put(double.class, 0.0);
      result.put(Boolean.class, Boolean.FALSE);
      result.put(Byte.class, (byte) 0);
      result.put(Short.class, (short) 0);
      result.put(Integer.class, 0);
      result.put(Long.class, 0L);
      result.put(Character.class, '\0');
      result.put(Float.class, 0.0F);
      result.put(Double.class, 0.0);
      result.put(String.class, "");
      result.put(Object.class, new Object());
      return result;
    }
}
