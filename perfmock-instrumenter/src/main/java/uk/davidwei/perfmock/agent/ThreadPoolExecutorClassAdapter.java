package uk.davidwei.perfmock.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ThreadPoolExecutorClassAdapter extends ClassVisitor implements Opcodes {

  ThreadPoolExecutorClassAdapter(ClassVisitor cv) {
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
    if (name.equals("beforeExecute")) {
      return new MethodVisitor(ASM5, mv) {
        @Override
        public void visitInsn(int opcode) {
          if (opcode == RETURN) {
            mv.visitMethodInsn(
                INVOKESTATIC,
                "uk/davidwei/perfmock/agent/PerfMockInstrumenter",
                "doBeforeExecuteCallback",
                "()V",
                false);
          }
          super.visitInsn(opcode);
        }
      };
    } else if (name.equals("afterExecute")) {
      return new MethodVisitor(ASM5, mv) {
        @Override
        public void visitInsn(int opcode) {
          if (opcode == RETURN) {
            mv.visitMethodInsn(
                INVOKESTATIC,
                "uk/davidwei/perfmock/agent/PerfMockInstrumenter",
                "doAfterExecuteCallback",
                "()V",
                false);
          }
          super.visitInsn(opcode);
        }
      };
    }
    return mv;
  }
}