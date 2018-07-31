package uk.davidwei.perfmock.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class PerfMockInstrumenter implements ClassFileTransformer {

  private static volatile boolean canRewriteBootstrap;
  private static ThreadConsumer preCallback = null;
  private static ThreadConsumer postCallback = null;
  private static Runnable beforeExecuteCallback = null;
  private static Runnable afterExecuteCallback = null;

  private static boolean canRewriteClass(String className, ClassLoader loader) {
    // There are two conditions under which we don't rewrite:
    //  1. If className was loaded by the bootstrap class loader and
    //  the agent wasn't (in which case the class being rewritten
    //  won't be able to call agent methods).
    //  2. If it is java.lang.ThreadLocal, which can't be rewritten because the
    //  JVM depends on its structure.
    if (((loader == null) && !canRewriteBootstrap)
        || className.startsWith("java/lang/ThreadLocal")) {
      return false;
    }
    return true;
  }

  private static String validateClass(String className, byte[] classfileBuffer) {
    if (className.equals("java/lang/Thread")
        || className.equals("java/util/concurrent/ThreadPoolExecutor")) {
      return className;
    } else if (!className.startsWith("java")) {
      ClassReader cr = new ClassReader(classfileBuffer);
      String sClassName = cr.getSuperName();
      if (sClassName.equals("java/lang/Thread")
          || sClassName.equals("java/util/concurrent/ThreadPoolExecutor")) {
        return sClassName;
      } else if (sClassName.equals("java/lang/Object")) {
        return null;
      } else {
        try {
          Class<?> sClass = Class.forName(sClassName.replace('/', '.'));
          while (sClass != null) {
            String canonName = sClass.getCanonicalName();
            if (canonName.equals("java.lang.Thread")
                || canonName.equals("java.util.concurrent.ThreadPoolExecutor")) {
              return canonName.replace('.', '/');
            } else if (canonName.equals("java.lang.Object")) {
              return null;
            }
            sClass = sClass.getSuperclass();
          }
        } catch (ClassNotFoundException e) {
          // NOP
        }
      }
    }
    return null;
  }

  public static void doPreCallback(Thread t) {
    if (preCallback != null) {
      preCallback.accept(t);
    }
  }

  public static void doPostCallback(Thread t) {
    if (postCallback != null) {
      postCallback.accept(t);
    }
  }

  public static void doBeforeExecuteCallback() {
    if (beforeExecuteCallback != null) {
      beforeExecuteCallback.run();
    }
  }

  public static void doAfterExecuteCallback() {
    if (afterExecuteCallback != null) {
      afterExecuteCallback.run();
    }
  }

  public static void setPreCallback(ThreadConsumer r) {
    preCallback = r;
  }

  public static void setPostCallback(ThreadConsumer r) {
    postCallback = r;
  }

  public static void setBeforeExecuteCallback(Runnable r) {
    beforeExecuteCallback = r;
  }

  public static void setAfterExecuteCallback(Runnable r) {
    afterExecuteCallback = r;
  }

  PerfMockInstrumenter() {
  }

  public static void premain(String agentArgs, Instrumentation inst) {
    // Force eager class loading here; we need these classes in order to do
    // instrumentation, so if we don't do the eager class loading, we
    // get a ClassCircularityError when trying to load and instrument
    // this class.
    try {
      Class.forName("sun.security.provider.PolicyFile");
      Class.forName("java.util.ResourceBundle");
      Class.forName("java.util.Date");
    } catch (Throwable t) {
      // NOP
    }

    if (!inst.isRetransformClassesSupported()) {
      System.err.println("Some JDK classes are already loaded and will not be instrumented.");
    }

    // Don't try to rewrite classes loaded by the bootstrap class
    // loader if this class wasn't loaded by the bootstrap class
    // loader.
    if (PerfMockInstrumenter.class.getClassLoader() != null) {
      canRewriteBootstrap = false;
      // The loggers aren't installed yet, so we use println.
      System.err.println("Class loading breakage: " +
          "Will not be able to instrument JDK classes");
      return;
    }

    canRewriteBootstrap = true;
    bootstrap(inst);
  }

  private static void bootstrap(Instrumentation inst) {
    inst.addTransformer(new PerfMockInstrumenter(), inst.isRetransformClassesSupported());

    if (!canRewriteBootstrap) {
      return;
    }

    // Get the set of already loaded classes that can be rewritten.
    Class<?>[] classes = inst.getAllLoadedClasses();
    ArrayList<Class<?>> classList = new ArrayList<>();
    for (Class<?> aClass : classes) {
      if (inst.isModifiableClass(aClass)) {
        classList.add(aClass);
      }
    }

    // Reload classes, if possible.
    Class<?>[] workaround = new Class<?>[classList.size()];
    try {
      inst.retransformClasses(classList.toArray(workaround));
    } catch (UnmodifiableClassException e) {
      System.err.println("Could not retransform early loaded classes.");
    }
  }

  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
      ProtectionDomain protectionDomain, byte[] classfileBuffer) {
    if (!canRewriteClass(className, loader)) {
      return null;
    }

    String classTypeName = validateClass(className, classfileBuffer);
    if (classTypeName == null) {
      return null;
    }

    /*
    System.out.println(
        "PerfMockInstrumenter::transform | className = " + className + ", classTypeName = "
            + classTypeName);
    */

    return instrument(classTypeName, classfileBuffer, loader);
  }

  public static byte[] instrument(String className, byte[] classfileBuffer, ClassLoader loader) {
    ClassReader cr = new ClassReader(classfileBuffer);
    ClassWriter cw =
        new StaticClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, loader);
    ClassVisitor cv;

    if (className.equals("java/lang/Thread")) {
      cv = new ThreadClassAdapter(cw);
    } else if (className.equals("java/util/concurrent/ThreadPoolExecutor")) {
      cv = new ThreadPoolExecutorClassAdapter(cw);
    } else {
      return null;
    }

    cr.accept(cv, ClassReader.SKIP_FRAMES);

    return cw.toByteArray();
  }
}