package trifectumsoftware.lookatit.patcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public final class AnnotationPatcher {
    public static void main(String[] args) throws Exception {
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        byte[] bytes = Files.readAllBytes(input);

        ClassReader reader = new ClassReader(bytes);
        ClassWriter writer = new ClassWriter(0);
        final boolean[] hasValue = {false};
        final boolean[] patched = {false};

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM9, writer) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationVisitor delegate = super.visitAnnotation(descriptor, visible);
                if (!"Lnet/minecraftforge/fml/common/Mod;".equals(descriptor)) {
                    return delegate;
                }
                return new AnnotationVisitor(Opcodes.ASM9, delegate) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name)) hasValue[0] = true;
                        super.visit(name, value);
                    }

                    @Override
                    public void visitEnd() {
                        if (!hasValue[0]) {
                            super.visit("value", "lookatit_universal");
                            patched[0] = true;
                        }
                        super.visitEnd();
                    }
                };
            }
        };

        reader.accept(visitor, 0);
        Files.createDirectories(output.getParent());
        Files.write(output, writer.toByteArray());
        System.out.println("[lookatit patcher] " + (patched[0] ? "patched" : "unchanged") + " " + output);
    }
}
