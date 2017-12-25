package org.jmock.agent;

import java.lang.instrument.UnmodifiableClassException;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ThreadClassAdapter extends ClassVisitor implements Opcodes {

  ThreadClassAdapter(ClassVisitor cv) {
    super(ASM5, cv);
  }

  @Override
  public MethodVisitor visitMethod(
      int access,
      String name,
      String desc,
      String signature,
      String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (name.equals("<init>")) {
      return new MethodVisitor(ASM5, mv) {
        @Override
        public void visitInsn(int opcode) {
          if ((opcode == ARETURN) ||
              (opcode == IRETURN) ||
              (opcode == LRETURN) ||
              (opcode == FRETURN) ||
              (opcode == DRETURN)) {
            throw new RuntimeException(new UnmodifiableClassException(
                "Constructors are supposed to return void"));
          }
          if (opcode == RETURN) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/jmock/agent/PerfMockInstrumenter",
                "doPreCallback",
                "(Ljava/lang/Thread;)V",
                false);
          }
          super.visitInsn(opcode);
        }
      };
    } else if (name.equals("run")) {
      return new MethodVisitor(ASM5, mv) {
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc,
            boolean itf) {
          super.visitMethodInsn(opcode, owner, name, desc, itf);
          if (name.equals("run")) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(
                INVOKESTATIC,
                "org/jmock/agent/PerfMockInstrumenter",
                "doPostCallback",
                "(Ljava/lang/Thread;)V",
                false);
          }
        }
      };
    }
    return mv;
  }
}