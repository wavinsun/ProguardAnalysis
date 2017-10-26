package cn.mutils.app.proguard.diff;

import proguard.obfuscate.MappingProcessor;
import proguard.obfuscate.MappingReader;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ProguardUtil {

    private static final String[] CARE_PACKAGES = new String[]{"cn.mutils.app."};
    private static final String[] IGNORE_CLASSES = new String[]{"android."};
    private static final String[] IGNORE_CLASSES_END = new String[]{"Activity", ".R", ".R$anim", ".R$attr", ".R$color", ".R$dimen",
            ".R$drawable", ".R$id", ".R$layout", ".R$string", ".R$style", ".R$styleable", ".R$animator", ".R$array", ".R$bool", ".R$integer",
            ".R$raw", ".R$xml"};
    private static final String[] IGNORE_METHODS = new String[]{"<init>", "<clinit>", "run", "toString"};

    public static Map<String, ProguardClass> pump(File file) {
        final Map<String, ProguardClass> map = new TreeMap<String, ProguardClass>();
        try {
            new MappingReader(file).pump(new MappingProcessor() {
                @Override
                public boolean processClassMapping(String className, String newClassName) {
                    System.out.println("Debug: " + className + " -> " + newClassName);
                    ProguardClass proguardClass = map.get(className);
                    if (proguardClass == null) {
                        proguardClass = new ProguardClass();
                        proguardClass.className = className;
                        proguardClass.simpleClassName = obtainSimpleClassName(className);
                        map.put(proguardClass.className, proguardClass);
                    }
                    proguardClass.newClassName = newClassName;
                    proguardClass.newSimpleClassName = obtainSimpleClassName(newClassName);
                    proguardClass.unchangedClassName = proguardClass.className.equals(proguardClass.newClassName);
                    return true;
                }

                @Override
                public void processFieldMapping(String className, String fieldType, String fieldName, String newFieldName) {
                    ProguardClass proguardClass = map.get(className);
                    if (proguardClass == null) {
                        System.out.println("Warning: " + className + " is not found!");
                        return;
                    }
                    ProguardClass.ProguardField field = new ProguardClass.ProguardField();
                    field.fieldType = fieldType;
                    field.filedName = fieldName;
                    field.newFieldName = newFieldName;
                    proguardClass.fields.add(field);
                }

                @Override
                public void processMethodMapping(String className, int firstLineNumber, int lastLineNumber, String methodReturnType, String methodName, String methodArguments, String newMethodName) {
                    ProguardClass proguardClass = map.get(className);
                    if (proguardClass == null) {
                        System.out.println("Warning: " + className + " is not found!");
                        return;
                    }
                    ProguardClass.ProguardMethod method = new ProguardClass.ProguardMethod();
                    method.fistLineNumber = firstLineNumber;
                    method.lastLineNumber = lastLineNumber;
                    method.methodArguments = methodArguments;
                    method.methodReturnType = methodReturnType;
                    method.methodName = methodName;
                    method.newMethodName = newMethodName;
                    proguardClass.methods.add(method);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static List<ProguardClass> transformUnchangedInfo(Map<String, ProguardClass> map) {
        List<ProguardClass> unchanged = new LinkedList<ProguardClass>();
        for (ProguardClass proguardClass : map.values()) {
            if (!proguardClass.unchangedClassName) {
                continue;
            }
            if (isInnerClass(proguardClass.simpleClassName) || isCustomInterface(proguardClass.simpleClassName) ||
                    ignoreClass(proguardClass.className)) {
                continue;
            }
            for (ProguardClass.ProguardField field : proguardClass.fields) {
                if (field.filedName.equals(field.newFieldName)) {
                    proguardClass.unchangedFields.add(field);
                }
            }
            for (ProguardClass.ProguardMethod method : proguardClass.methods) {
                if (ignoreMethod(method.methodName)) {
                    continue;
                }
                if (method.methodName.equals(method.newMethodName)) {
                    proguardClass.unchangedMethods.add(method);
                }
            }
            if (proguardClass.unchangedClassName || proguardClass.unchangedFields.size() != 0 ||
                    proguardClass.unchangedMethods.size() != 0) {
                unchanged.add(proguardClass);
            }
        }
        return unchanged;
    }

    private static boolean ignoreClass(String className) {
        if (className == null) {
            return false;
        }
        if (!className.contains(".")) {
            return true;
        }
        if (className.contains(".inter.")) {
            return true;
        }
        for (String str : IGNORE_CLASSES_END) {
            if (str.isEmpty()) {
                continue;
            }
            if (className.endsWith(str)) {
                return true;
            }
        }
        for (String str : IGNORE_CLASSES) {
            if (str.isEmpty()) {
                continue;
            }
            if (className.startsWith(str)) {
                return true;
            }
        }
        boolean found = false;
        for (String str : CARE_PACKAGES) {
            if (className.startsWith(str)) {
                return false;
            }
        }
        return true;
    }

    private static boolean ignoreMethod(String methodName) {
        if (methodName == null) {
            return false;
        }
        for (String str : IGNORE_METHODS) {
            if (str.isEmpty()) {
                continue;
            }
            if (methodName.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private static String obtainSimpleClassName(String className) {
        int indexOf$ = className.lastIndexOf('$');
        if (indexOf$ != -1) {
            return className.substring(indexOf$ + 1);
        }
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static boolean isInnerClass(String simpleClassName) {
        if (simpleClassName == null) {
            return false;
        }
        try {
            Integer.parseInt(simpleClassName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isCustomInterface(String simpleClassName) {
        if (simpleClassName == null || simpleClassName.isEmpty()) {
            return false;
        }
        if (simpleClassName.length() < 2) {
            return false;
        }
        if (simpleClassName.endsWith("Impl")) {
            return true;
        }
        boolean startWithI = simpleClassName.charAt(0) == 'I';
        if (!startWithI) {
            return false;
        }
        char second = simpleClassName.charAt(1);
        if (second >= 'A' && second <= 'Z') {
            return true;
        }
        return false;
    }

}
