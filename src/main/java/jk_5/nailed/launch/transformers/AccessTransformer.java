package jk_5.nailed.launch.transformers;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.*;

public class AccessTransformer implements IClassTransformer {

    private static final Splitter SEPARATOR = Splitter.on(' ').trimResults();

    private final ImmutableMultimap<String, Modifier> modifiers;

    public AccessTransformer() throws IOException {
        this((String) Launch.blackboard.get("nailed.at"));
    }

    protected AccessTransformer(String file) throws IOException {
        checkNotNull(file, "file");

        ImmutableMultimap.Builder<String, Modifier> builder = ImmutableListMultimap.builder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = substringBefore(line, '#').trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> parts = SEPARATOR.splitToList(line);
                checkArgument(parts.size() <= 3, "Invalid access transformer config line: " + line);

                String name = null;
                String desc = null;

                boolean isClass = parts.size() == 2;
                if (!isClass) {
                    name = parts.get(2);
                    int pos = name.indexOf('(');
                    if (pos >= 0) {
                        desc = name.substring(pos);
                        name = name.substring(0, pos);
                    }
                }

                String s = parts.get(0);
                int access = 0;
                if (s.startsWith("public")) {
                    access = ACC_PUBLIC;
                } else if (s.startsWith("protected")) {
                    access = ACC_PROTECTED;
                } else if (s.startsWith("private")) {
                    access = ACC_PRIVATE;
                }

                Boolean markFinal = null;
                if (s.endsWith("+f")) {
                    markFinal = true;
                } else if (s.endsWith("-f")) {
                    markFinal = false;
                }

                String className = parts.get(1).replace('/', '.');
                builder.put(className, new Modifier(name, desc, isClass, access, markFinal));
            }
        }

        this.modifiers = builder.build();
    }

    private static String substringBefore(String s, char c) {
        int pos = s.indexOf(c);
        return pos >= 0 ? s.substring(0, pos) : s;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null || !this.modifiers.containsKey(transformedName)) {
            return bytes;
        }

        ClassNode classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);

        for (Modifier m : this.modifiers.get(transformedName)) {
            if (m.isClass) { // Class
                classNode.access = m.transform(classNode.access);
            } else if (m.desc == null) { // Field
                for (FieldNode fieldNode : classNode.fields) {
                    if (m.wildcard || fieldNode.name.equals(m.name)) {
                        fieldNode.access = m.transform(fieldNode.access);
                        if (!m.wildcard) {
                            break;
                        }
                    }
                }
            } else {
                List<MethodNode> overridable = null;

                for (MethodNode methodNode : classNode.methods) {
                    if (m.wildcard || (methodNode.name.equals(m.name) && methodNode.desc.equals(m.desc))) {
                        boolean wasPrivate = (methodNode.access & ACC_PRIVATE) != 0;
                        methodNode.access = m.transform(methodNode.access);

                        // Constructors always use INVOKESPECIAL
                        // if we changed from private to something else we need to replace all INVOKESPECIAL calls to this method with INVOKEVIRTUAL
                        // so that overridden methods will be called. Only need to scan this class, because obviously the method was private.
                        if (!methodNode.name.equals("<init>") && wasPrivate && (methodNode.access & ACC_PRIVATE) == 0) {
                            if (overridable == null) {
                                overridable = new ArrayList<>(3);
                            }

                            overridable.add(methodNode);
                        }

                        if (!m.wildcard) {
                            break;
                        }
                    }
                }

                if (overridable != null) {
                    for (MethodNode methodNode : classNode.methods) {
                        for (Iterator<AbstractInsnNode> itr = methodNode.instructions.iterator(); itr.hasNext(); ) {
                            AbstractInsnNode insn = itr.next();
                            if (insn.getOpcode() == INVOKESPECIAL) {
                                MethodInsnNode mInsn = (MethodInsnNode) insn;
                                for (MethodNode replace : overridable) {
                                    if (replace.name.equals(mInsn.name) && replace.desc.equals(mInsn.desc)) {
                                        mInsn.setOpcode(INVOKEVIRTUAL);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();
    }

    private static class Modifier {

        private final String name;
        private final String desc;
        private final boolean wildcard;
        private final boolean isClass;

        private final int targetAccess;
        private final Boolean markFinal;

        private Modifier(String name, String desc, boolean isClass, int targetAccess, Boolean markFinal) {
            boolean wildcard = false;
            if (name != null) {
                checkArgument(!name.isEmpty(), "name cannot be empty");
                wildcard = name.equals("*");
            }
            this.name = name;
            checkArgument(desc == null || !desc.isEmpty(), "desc cannot be empty");
            this.desc = desc;
            this.wildcard = wildcard;
            this.isClass = isClass;
            this.targetAccess = targetAccess;
            this.markFinal = markFinal;
        }

        private int transform(int access) {
            int result = access & ~7;

            switch (access & 4) {
                case ACC_PRIVATE:
                    result |= this.targetAccess;
                    break;
                case 0: // default
                    if (this.targetAccess != ACC_PRIVATE) {
                        result |= this.targetAccess;
                    }
                    break;
                case ACC_PROTECTED:
                    result |= this.targetAccess != 0 && this.targetAccess != ACC_PRIVATE ? this.targetAccess : ACC_PROTECTED;
                    break;
                case ACC_PUBLIC:
                    result |= this.targetAccess != 0 && this.targetAccess != ACC_PRIVATE && this.targetAccess != ACC_PROTECTED ? this.targetAccess
                            : ACC_PUBLIC;
                    break;
                default:
                    throw new AssertionError();
            }

            if (this.markFinal != null) {
                if (this.markFinal) {
                    result |= ACC_FINAL;
                } else {
                    result &= ~ACC_FINAL;
                }
            }

            return result;
        }
    }
}
