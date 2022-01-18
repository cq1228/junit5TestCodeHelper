package com.cq.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cq.common.CodeUtils;
import com.cq.model.ParentTree;
import com.cq.valuegenerator.JsonValueService;
import com.cq.valuegenerator.impl.BooleanGenerator;
import com.cq.valuegenerator.impl.ByteGenerator;
import com.cq.valuegenerator.impl.CharacterGenerator;
import com.cq.valuegenerator.impl.DateGenerator;
import com.cq.valuegenerator.impl.DoubleGenerator;
import com.cq.valuegenerator.impl.FloatGenerator;
import com.cq.valuegenerator.impl.IntegerGenerator;
import com.cq.valuegenerator.impl.LongGenerator;
import com.cq.valuegenerator.impl.StringGenerator;
import com.google.common.collect.Maps;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import org.apache.commons.lang.StringUtils;

import static com.cq.common.CodeUtils.isCollection;

/**
 * @author 有尘
 * @date 2021/9/28
 */
public class JsonFileGenerator {

    private final static Map<String, JsonValueService> normalTypes = new HashMap<>();

    private static ParentTree node;

    static {
        //Fake fakeDecimal = new FakeDecimal();
        //FakeDateTime fakeDateTime = new FakeDateTime();

        normalTypes.put("Boolean", new BooleanGenerator());
        normalTypes.put("Float", new FloatGenerator());
        normalTypes.put("Double", new DoubleGenerator());
        normalTypes.put("Integer", new IntegerGenerator());
        normalTypes.put("Long", new LongGenerator());
        normalTypes.put("Number", new IntegerGenerator());
        normalTypes.put("Character", new CharacterGenerator());
        normalTypes.put("CharSequence", new StringGenerator());
        normalTypes.put("String", new StringGenerator());
        normalTypes.put("Date", new DateGenerator());
        normalTypes.put("Byte", new ByteGenerator());
    }

    public static Object getFields(PsiType type) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (type == null) {
            return map;
        }

        PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);
        for (PsiField field : psiClass.getAllFields()) {
            map.put(fieldResolve(field), typeResolve(field.getType(), 0));
        }

        return map;
    }

    /**
     * 属性值
     *
     * @param type
     * @param level
     * @return
     */
    public static Object typeResolve(PsiType type, int level) {
        if (level == 0) {
            node = new ParentTree(type.getCanonicalText());
        } else {
            if (alreadyExist(node)) {
                return null;
            } else {
                // 转换node
                ParentTree newNode = node.getSonList().computeIfAbsent(type.getCanonicalText(),
                    a -> {
                        ParentTree parentTree1 = new ParentTree(a);
                        parentTree1.setParent(node);
                        return parentTree1;
                    });
                node = newNode;
            }
        }
        level = ++level;
        try {
            /**
             * 原始类型
             */
            if (type instanceof PsiPrimitiveType) {
                return normalTypes.get(getPackageType(type)).defaultValue();
            }

            /**
             * 数组类型
             */
            if (type instanceof PsiArrayType) {
                System.out.println("is Array :" + type);
                List<Object> list = new ArrayList<>();
                PsiType deepType = type.getDeepComponentType();
                list.add(typeResolve(deepType, level));
                return list;
            }

            List<String> fieldTypeNames = new ArrayList<>();

            fieldTypeNames.add(type.getPresentableText());
            // 父类类型
            PsiType[] types = type.getSuperTypes();
            fieldTypeNames.addAll(Arrays.stream(types).map(PsiType::getPresentableText).collect(Collectors.toList()));

            //集合类，或迭代器类
            if (isCollection(type)) {
                List<Object> list = new ArrayList<>();
                PsiType deepType = CodeUtils.getCollectionType(type);
                System.out.println("json deepType:"+deepType.getCanonicalText());
                list.add(typeResolve(deepType, level));
                return list;
            }
            Map<String, Object> map = new LinkedHashMap<>();

            PsiClass psiClass = PsiUtil.resolveClassInClassTypeOnly(type);

            /**
             * 枚举类型
             */
            if (psiClass != null && psiClass.isEnum()) {
                for (PsiField field : psiClass.getFields()) {
                    if (field instanceof PsiEnumConstant) {
                        return field.getName();
                    }
                }
                return "";

            }
            // 其他Object类型

            //todo map类型
            if (fieldTypeNames.stream().anyMatch(s -> s.startsWith("Map"))) {
                HashMap<Object, Object> objectHashMap = Maps.newHashMap();
                PsiType[] parameters = ((PsiClassReferenceType)type).getParameters();
                if (parameters.length >= 2) {
                    objectHashMap.put(typeResolve(parameters[0], level).toString(), typeResolve(parameters[1], level));
                }
                return objectHashMap;
            }

            // Class类型
            if (fieldTypeNames.stream().anyMatch(s -> s.startsWith("Class"))) {
                return null;
            }

            List<String> retain = new ArrayList<>(fieldTypeNames);
            retain.retainAll(normalTypes.keySet());
            if (!retain.isEmpty()) {
                return normalTypes.get(retain.get(0)).defaultValue();
            } else {

                if (level > 500) {
                    throw new RuntimeException(
                        "This class reference level exceeds maximum limit or has nested references!");
                }
                if(psiClass==null){
                    return map;
                }
                for (PsiField field : psiClass.getAllFields()) {
                    // 静态变量无需配置
                    if (field.hasModifierProperty("static")) {
                        continue;
                    }
                    // 针对泛型
                    if (type instanceof PsiClassReferenceType) {
                        // 泛型参数类型
                        PsiType[] parameters = ((PsiClassReferenceType)type).getParameters();
                        // 泛型表示，比如T,R
                        PsiTypeParameter[] typeParameters = psiClass.getTypeParameters();
                        for (int i = 0; i < parameters.length; i++) {
                            // 得到里面的类型
                            String genericName = typeParameters[i].getText();
                            // 得到T
                            String presentableText = field.getType().getPresentableText();
                            if (presentableText.equals(genericName)) {
                                map.put(fieldResolve(field), typeResolve(parameters[i], level));
                            }
                        }
                    }
                    if (!map.containsKey(fieldResolve(field))) {
                        map.put(fieldResolve(field), typeResolve(field.getType(), level));
                    }

                }
                return map;
            }
        } catch (Exception e) {
            return null;
        } finally {
            node = node.getParent();
        }

    }

    public static String getPackageType(PsiType type) {
        switch (type.getCanonicalText()) {
            case "boolean":
                return "Boolean";
            case "byte":
                return "Byte";
            case "short":
            case "int":
                return "Integer";
            case "long":
                return "Long";
            case "float":
                return "Float";
            case "double":
                return "Double";
            case "char":
                return "Character";
            default:
                return null;
        }
    }

    /**
     * 属性名
     *
     * @param field
     * @return
     */
    private static String fieldResolve(PsiField field) {

        PsiAnnotation annotation = field.getAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class.getName());
        if (annotation != null) {
            String fieldName = annotation.findAttributeValue("value").getText()
                .replace("\"", "");
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }

        annotation = field.getAnnotation("com.alibaba.fastjson.annotation.JSONField");
        if (annotation != null) {
            String fieldName = annotation.findAttributeValue("name").getText()
                .replace("\"", "");
            if (StringUtils.isNotBlank(fieldName)) {
                return fieldName;
            }
        }
        return field.getName();
    }

    public static Object tryGetPrimitiveType(PsiType type) {
        Object res;
        if (normalTypes.containsKey(type.getPresentableText())) {
            return normalTypes.get(type.getPresentableText()).defaultValue();
        } else {
            String packageType = getPackageType(type);

            res = packageType == null ? null : normalTypes.get(packageType).defaultValue();
        }
        return res;
    }

    public static String getPrimitiveTypeStr(PsiType type) {
        if (normalTypes.containsKey(type.getPresentableText())) {
            return type.getPresentableText();
        } else {
            return getPackageType(type);
        }
    }

    /**
     * 获取json对象
     *
     * @param type
     * @return
     */
    public static Object getJsonObject(PsiType type) {
        Object randomValue = tryGetPrimitiveType(type);
        if (randomValue == null) {
            Object fieldMap = typeResolve(type, 0);
            return fieldMap;
            // 获取json数据
        } else {
            return randomValue;
        }
    }


    public static boolean isGeneric(String presentableText) {
        return presentableText.contains("<");
    }

    public static boolean alreadyExist(ParentTree node) {
        if (node == null) {
            return false;
        }
        String name = node.getValue();
        while (node.getParent() != null) {
            node = node.getParent();
            if (name.equals(node.getValue())) {
                return true;
            }
        }
        return false;
    }
}
