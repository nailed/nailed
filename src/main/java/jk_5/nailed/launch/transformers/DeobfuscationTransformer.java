package jk_5.nailed.launch.transformers;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Maps;
import net.minecraft.launchwrapper.IClassNameTransformer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.RemappingMethodAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class DeobfuscationTransformer extends Remapper implements IClassTransformer, IClassNameTransformer {

    private final ImmutableBiMap<String, String> classes;
    private final ImmutableTable<String, String, String> rawFields;
    private final ImmutableTable<String, String, String> rawMethods;

    private final Map<String, Map<String, String>> fields;
    private final Map<String, Map<String, String>> methods;

    private final Set<String> failedFields = new HashSet<>();
    private final Set<String> failedMethods = new HashSet<>();

    private final Map<String, Map<String, String>> fieldDescriptions = new HashMap<>();

    public DeobfuscationTransformer() throws Exception {
        Path path = (Path) Launch.blackboard.get("nailed.deobf-srg");
        String name = path.getFileName().toString();
        boolean gzip = name.endsWith(".gz");

        ImmutableBiMap.Builder<String, String> classes = ImmutableBiMap.builder();
        ImmutableTable.Builder<String, String, String> fields = ImmutableTable.builder();
        ImmutableTable.Builder<String, String, String> methods = ImmutableTable.builder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(gzip ? new GZIPInputStream(Files.newInputStream(path)) : Files.newInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).isEmpty()) {
                    continue;
                }

                String[] parts = StringUtils.split(line, ' ');
                if (parts.length < 3) {
                    System.out.println("Invalid line: " + line);
                    continue;
                }

                MappingType type = MappingType.of(parts[0]);
                if (type == null) {
                    System.out.println("Invalid mapping: " + line);
                    continue;
                }

                String[] source;
                String[] dest;
                switch (type) {
                    case CLASS:
                        classes.put(parts[1], parts[2]);
                        break;
                    case FIELD:
                        source = getSignature(parts[1]);
                        dest = getSignature(parts[2]);
                        String fieldType = getFieldType(source[0], source[1]);
                        fields.put(source[0], source[1] + ':' + fieldType, dest[1]);
                        if (fieldType != null) {
                            fields.put(source[0], source[1] + ":null", dest[1]);
                        }
                        break;
                    case METHOD:
                        source = getSignature(parts[1]);
                        dest = getSignature(parts[3]);
                        methods.put(source[0], source[1] + parts[2], dest[1]);
                        break;
                    default:
                }
            }
        }

        this.classes = classes.build();
        this.rawFields = fields.build();
        this.rawMethods = methods.build();

        this.fields = Maps.newHashMapWithExpectedSize(this.rawFields.size());
        this.methods = Maps.newHashMapWithExpectedSize(this.rawMethods.size());
    }

    private static String[] getSignature(String in) {
        int pos = in.lastIndexOf('/');
        return new String[]{in.substring(0, pos), in.substring(pos + 1)};
    }

    private static byte[] getBytes(String name) {
        try {
            return Launch.classLoader.getClassBytes(name);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private String getFieldType(String owner, String name) {
        Map<String, String> fieldDescriptions = this.fieldDescriptions.get(owner);
        if (fieldDescriptions != null) {
            return fieldDescriptions.get(name);
        }

        byte[] bytes = getBytes(owner);
        if (bytes == null) {
            return null;
        }

        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

        String result = null;
        fieldDescriptions = Maps.newHashMapWithExpectedSize(classNode.fields.size());
        for (FieldNode fieldNode : classNode.fields) {
            fieldDescriptions.put(fieldNode.name, fieldNode.desc);
            if (fieldNode.name.equals(name)) {
                result = fieldNode.desc;
            }
        }

        this.fieldDescriptions.put(owner, fieldDescriptions);
        return result;
    }

    @Override
    public String map(String typeName) {
        if (this.classes == null) {
            return typeName;
        }
        String name = this.classes.get(typeName);
        return name != null ? name : typeName;
    }

    public String unmap(String typeName) {
        if (this.classes == null) {
            return typeName;
        }
        String name = this.classes.inverse().get(typeName);
        return name != null ? name : typeName;
    }

    @Override
    public String mapFieldName(String owner, String fieldName, String desc) {
        if (this.classes == null) {
            return fieldName;
        }
        Map<String, String> fields = getFieldMap(owner);
        if (fields != null) {
            String name = fields.get(fieldName + ':' + desc);
            if (name != null) {
                return name;
            }
        }

        return fieldName;
    }

    private Map<String, String> getFieldMap(String owner) {
        Map<String, String> result = this.fields.get(owner);
        if (result != null) {
            return result;
        }

        if (!this.failedFields.contains(owner)) {
            loadSuperMaps(owner);
            if (!this.fields.containsKey(owner)) {
                this.failedFields.add(owner);
            }
        }

        return this.fields.get(owner);
    }

    @Override
    public String mapMethodName(String owner, String methodName, String desc) {
        if (this.classes == null) {
            return methodName;
        }
        Map<String, String> methods = getMethodMap(owner);
        if (methods != null) {
            String name = methods.get(methodName + desc);
            if (name != null) {
                return name;
            }
        }

        return methodName;
    }

    private Map<String, String> getMethodMap(String owner) {
        Map<String, String> result = this.methods.get(owner);
        if (result != null) {
            return result;
        }

        if (!this.failedMethods.contains(owner)) {
            loadSuperMaps(owner);
            if (!this.methods.containsKey(owner)) {
                this.failedMethods.add(owner);
            }
        }

        return this.methods.get(owner);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        //System.out.println("\t> Deobfuscating " + name + " -> " + transformedName);
        ClassWriter writer = new ClassWriter(0);
        ClassReader reader = new ClassReader(bytes);
        reader.accept(new RemappingAdapter(writer), ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }


    @Override
    public String remapClassName(String typeName) {
        return map(typeName.replace('.', '/')).replace('/', '.');
    }

    @Override
    public String unmapClassName(String typeName) {
        return unmap(typeName.replace('.', '/')).replace('/', '.');
    }

    private void loadSuperMaps(String name) {
        byte[] bytes = getBytes(name);
        if (bytes != null) {
            ClassReader reader = new ClassReader(bytes);
            createSuperMaps(name, reader.getSuperName(), reader.getInterfaces());
        }
    }

    void createSuperMaps(String name, String superName, String[] interfaces) {
        if (Strings.isNullOrEmpty(superName)) {
            return;
        }

        String[] parents = new String[interfaces.length + 1];
        parents[0] = superName;
        System.arraycopy(interfaces, 0, parents, 1, interfaces.length);

        for (String parent : parents) {
            if (!this.fields.containsKey(parent)) {
                loadSuperMaps(parent);
            }
        }

        Map<String, String> fields = new HashMap<>();
        Map<String, String> methods = new HashMap<>();

        Map<String, String> m;
        for (String parent : parents) {
            m = this.fields.get(parent);
            if (m != null) {
                fields.putAll(m);
            }
            m = this.methods.get(parent);
            if (m != null) {
                methods.putAll(m);
            }
        }

        fields.putAll(this.rawFields.row(name));
        methods.putAll(this.rawMethods.row(name));

        this.fields.put(name, ImmutableMap.copyOf(fields));
        this.methods.put(name, ImmutableMap.copyOf(methods));
    }

    String getStaticFieldType(String oldType, String oldName, String newType, String newName) {
        String type = getFieldType(oldType, oldName);
        if (oldType.equals(newType)) {
            return type;
        }

        Map<String, String> newClassMap = this.fieldDescriptions.get(newType);
        if (newClassMap == null) {
            newClassMap = new HashMap<>();
            this.fieldDescriptions.put(newType, newClassMap);
        }
        newClassMap.put(newName, type);
        return type;
    }


    private enum MappingType {
        PACKAGE("PK"), CLASS("CL"), FIELD("FD"), METHOD("MD");

        private static final ImmutableMap<String, MappingType> LOOKUP;

        static {
            ImmutableMap.Builder<String, MappingType> builder = ImmutableMap.builder();
            for (MappingType type : MappingType.values()) {
                builder.put(type.identifier + ':', type);
            }
            LOOKUP = builder.build();
        }

        private final String identifier;

        private MappingType(String identifier) {
            this.identifier = identifier;
        }

        public static MappingType of(String identifier) {
            return LOOKUP.get(identifier);
        }

    }

    private class RemappingAdapter extends RemappingClassAdapter {

        public RemappingAdapter(ClassVisitor cv) {
            super(cv, DeobfuscationTransformer.this);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            if (interfaces == null) {
                interfaces = ArrayUtils.EMPTY_STRING_ARRAY;
            }

            createSuperMaps(name, superName, interfaces);
            super.visit(version, access, name, signature, superName, interfaces);
        }

        @Override
        protected MethodVisitor createRemappingMethodAdapter(int access, String newDesc, MethodVisitor mv) {
            return new RemappingMethodAdapter(access, newDesc, mv, RemappingAdapter.this.remapper) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                    String type = this.remapper.mapType(owner);
                    String fieldName = this.remapper.mapFieldName(owner, name, desc);
                    String newDesc = this.remapper.mapDesc(desc);
                    if (opcode == Opcodes.GETSTATIC && type.startsWith("net/minecraft/") && newDesc.startsWith("Lnet/minecraft/")) {
                        String replDesc = getStaticFieldType(owner, name, type, fieldName);
                        if (replDesc != null) {
                            newDesc = this.remapper.mapDesc(replDesc);
                        }
                    }

                    if (this.mv != null) {
                        this.mv.visitFieldInsn(opcode, type, fieldName, newDesc);
                    }
                }
            };
        }
    }
}
