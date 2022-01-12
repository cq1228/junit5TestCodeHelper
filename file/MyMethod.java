package com.cq.file;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.alibaba.fastjson.JSONObject;

import com.cq.common.CodeUtils;
import com.cq.common.MutiValuesWithClass;
import com.cq.valuegenerator.ValueContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiArrayType;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import static com.cq.file.JsonFileGenerator.getJsonObject;
import static com.cq.file.JsonFileGenerator.getPrimitiveTypeStr;
import static com.cq.file.JsonFileGenerator.isCollection;
import static com.cq.file.JsonFileGenerator.isGeneric;

/**
 * @author 有尘
 * @date 2021/10/9
 */
@Data
public class MyMethod {
    /**
     * 测试方法
     */
    PsiMethod method;
    /**
     * 需要mock的数据
     */
    Map<PsiType, MutiValuesWithClass> needMockFields;
    /**
     * 类变量中需要mock的方法
     */
    Map<String, PsiMethod> needMockFieldMethod;

    private Set<String> needImports;

    private CodeGenerator codeGenerator;

    private String text;
    Map<String, Integer> nameCount = new HashMap<>();
    ValueContext valueContext = ValueContext.getContext();

    public MyMethod(PsiMethod method, CodeGenerator codeGenerator) {
        this.method = method;
        this.codeGenerator = codeGenerator;
        needMockFieldMethod = new HashMap<>();
        needMockFields = new HashMap<>();
        needImports = new HashSet<>();
    }

    public void build() {
        String methodName = method.getName();
        String methodNameCount = methodName;
        // 同名方法
        Map<String, Integer> methodCount = codeGenerator.getMethodCount();
        if (methodCount.containsKey(methodName)) {
            Integer count = methodCount.get(methodName);
            methodNameCount = methodNameCount + count;
            methodCount.put(methodName, count + 1);
        } else {
            methodCount.put(methodName, 0);
        }

        String methodContent = this.generateMethodContent(method, methodNameCount);

        String filePath = codeGenerator.getPsiFile().getVirtualFile().getPath();
        int index = filePath.indexOf("java");
        filePath = filePath.substring(index + 5).replace(".java", "");
        String fileName = FileUtils.getJsonFileName(methodNameCount);
        this.text = String.format("\t@ParameterizedTest\n"
                + "\t@JsonFileSource(resources = {\"/%s/%s\"})\n"
                + " \tpublic void %sTest(JSONObject arg) {\n%s\t}\n\n", filePath, fileName, methodNameCount,
            methodContent);
    }

    /**
     * 生成方法内容，并且生成需要的测试数据
     * 每一个方法生成一个大的 json对象
     *
     * @param method psimethod
     * @return
     */
    private String generateMethodContent(PsiMethod method, String fileName) {
        StringBuilder code = new StringBuilder(512);
        String param = generateMethodArg(method);
        // 用来生成测试数据--json文件
        Map<PsiType, MutiValuesWithClass> fields = new HashMap<>();

        generateInputData(method, fields);
        String methodStr = generateMockMethod(method, fields);
        String attr = generateVarFromJsonCode(fields);
        // todo
        code.append(attr);
        code.append(methodStr);
        PsiType returnType = method.getReturnType();
        if (returnType.getPresentableText().equals("void")) {
            code.append(
                String.format("\t\t%s.%s(%s);\n",
                    CodeUtils.getCamelCase(codeGenerator.getPsiClass().getName()),
                    method.getName(), param));
        } else {
            getImport(returnType);
            code.append(
                String.format("\t\t%s result = %s.%s(%s);\n", returnType.getPresentableText(),
                    CodeUtils.getCamelCase(codeGenerator.getPsiClass().getName()),
                    method.getName(), param));
        }
        // 保存json数据
        saveSourceData(fileName, fields);
        code.append("\t\t//todo verify the result\n");
        return code.toString();
    }

    /**
     * 生成方法内部的调用参数
     *
     * @param method
     * @return
     */
    private String generateMethodArg(PsiMethod method) {
        PsiParameterList parameters = method.getParameterList();
        StringBuilder param = new StringBuilder();
        IntStream.range(0, parameters.getParametersCount()).forEach((i) -> {
            PsiType type = parameters.getParameter(i).getType();
            getImport(type);
            // 生成参数
            param.append(parameters.getParameter(i).getName() + ",");

        });
        if (parameters.getParametersCount() > 0) {
            param.deleteCharAt(param.length() - 1);
        }
        return param.toString();
    }

    /**
     * 生成测试数据以及获取数据的code
     *
     * @param method
     * @param fields
     * @return
     */
    private void generateInputData(PsiMethod method, Map<PsiType, MutiValuesWithClass> fields) {
        PsiParameterList parameters = method.getParameterList();
        IntStream.range(0, parameters.getParametersCount()).forEach((i) -> {
            PsiType type = parameters.getParameter(i).getType();
            Object jsonObject = getJsonObject(type);
            // todo 入参的名字，注意范型
            MutiValuesWithClass objectListMap = fields.computeIfAbsent(type, a -> new MutiValuesWithClass(jsonObject));
            objectListMap.addNames(parameters.getParameter(i).getName());
        });
    }

    /**
     * 生成mock对象对应的mock方法
     *
     * @param method
     * @param fields
     * @return
     */
    private String generateMockMethod(PsiMethod method, Map<PsiType, MutiValuesWithClass> fields) {
        StringBuilder content = new StringBuilder();
        //modifierList.get
        Map<PsiMethod, Boolean> collect = Arrays.stream(codeGenerator.getPsiClass().getAllMethods()).filter(
            a -> !a.equals(method) && !a.getName().equals("equals")).collect(
            Collectors.toMap(Function.identity(), a -> true));

        String body = CodeUtils.getBody(method, collect);
        for (int i = 0; i < codeGenerator.getNeedMockFields().size(); i++) {
            PsiField field = codeGenerator.getNeedMockFields().get(i);
            Set<String> alreadyMokcMethods = new HashSet<>();
            Pattern pattern = Pattern.compile(field.getName() + ".\\w+\\(.*\\)");
            Matcher matcher = pattern.matcher(body);

            while (matcher.find()) {
                String methodName = matcher.group();
                methodName = CodeUtils.getStringOnlyBlock(methodName, field.getName());
                String methodShortName = methodName.substring(methodName.indexOf('.') + 1, methodName.indexOf('('));

                String methodKey = field.getType().getCanonicalText() + methodShortName + CodeUtils.getCount(methodName,
                    ",");
                System.out.println(methodKey);
                if (alreadyMokcMethods.contains(methodKey)) {
                    continue;
                } else {
                    alreadyMokcMethods.add(methodKey);
                    // 找到方法
                    PsiMethod fieldMethod = valueContext.getMethod(methodKey);
                    if (fieldMethod == null) {
                        throw new RuntimeException(
                            "方法太复杂，无法解析该方法：" + field.getType().getCanonicalText() + "." + methodShortName);
                    }
                    PsiType returnType = fieldMethod.getReturnType();
                    String fieldMethodReturnType = getImport(returnType);
                    if (!StringUtils.equalsIgnoreCase(fieldMethodReturnType, "void")) {
                        Object jsonObject = getJsonObject(returnType);
                        MutiValuesWithClass mutiValuesWithClass = fields.computeIfAbsent(returnType,
                            a -> new MutiValuesWithClass(jsonObject));

                        String attrName = generateArgName(fieldMethodReturnType, returnType);
                        mutiValuesWithClass.addNames(attrName);
                        content.append(String.format("\t\twhen(%s(" + generateFieldMethodArg(fieldMethod) +
                            ")).thenReturn(%s);\n", field.getName() + "." + methodShortName, attrName));
                    }
                }
            }
        }

        return content.toString();
    }

    /**
     * 解决名字重复、名字是基础类型的问题
     *
     * @param fieldMethodReturnType
     * @param returnType
     * @return
     */
    private String generateArgName(String fieldMethodReturnType, PsiType returnType) {
        String attrName = CodeUtils.filterGeneric(CodeUtils.getCamelCase(fieldMethodReturnType));

        if (CodeUtils.isPrimitiveType(returnType)) {
            attrName += "Arg";
        }
        // 防止重复
        String result = attrName;
        if (nameCount.containsKey(attrName)) {
            int count = nameCount.get(attrName);
            System.out.println("map:" + nameCount + "attrname:" + attrName + "count:" + count);
            result = attrName + count;
            nameCount.put(attrName, count + 1);
        } else {
            nameCount.put(attrName, 0);
        }
        return result;
    }

    private String generateVarFromJsonCode(Map<PsiType, MutiValuesWithClass> fields) {
        StringBuilder jsonObjectBuilder = new StringBuilder();
        fields.entrySet().forEach(a -> {
            PsiType type = a.getKey();
            String shortName = type.getPresentableText();
            MutiValuesWithClass value = a.getValue();
            value.getNames().stream().forEach(name -> {
                if (CodeUtils.isPrimitiveType(type)) {
                    jsonObjectBuilder.append(String.format("\t\t%s %s = arg.get%s(\"%s\"); \n",
                        shortName, name, getPrimitiveTypeStr(type),
                        shortName));
                } else if (PsiUtil.resolveClassInClassTypeOnly(type).isEnum()) {
                    jsonObjectBuilder.append(String.format("\t\t%s %s = %s.values()[0]; \n",
                        shortName, name, shortName));
                } else if (isCollection(type)) {
                    PsiType deepComponentType = PsiUtil.extractIterableTypeParameter(type, false);
                    jsonObjectBuilder.append(
                        String.format("\t\t%s %s = JSONObject.parseArray(arg.getString(\"%s\"),%s.class); \n",
                            shortName, name,
                            shortName, deepComponentType.getPresentableText()));
                } else if (isGeneric(shortName)) {
                    jsonObjectBuilder.append(
                        String.format(
                            "\t\t%s %s = JSONObject.parseObject(arg.getString(\"%s\"),new TypeReference<%s>(){}); \n",
                            shortName, name, shortName, shortName));
                } else {
                    //  todo 增加泛型
                    jsonObjectBuilder.append(
                        String.format("\t\t%s %s = JSONObject.parseObject(arg.getString(\"%s\"),%s.class); \n",
                            shortName, name, shortName, CodeUtils.filterGeneric(shortName)));
                }
            });

        });
        return jsonObjectBuilder.toString();
    }

    /**
     * 保存json数据
     *
     * @param methodName
     * @param fields
     */
    private void saveSourceData(String methodName, Map<PsiType, MutiValuesWithClass> fields) {
        String filePath = FileUtils.getJsonFilePath(this.codeGenerator.getPsiFile());
        String fileName = FileUtils.getJsonFileName(methodName);
        Map<String, Object> collect = fields.entrySet().stream().collect(
            Collectors.toMap(a -> a.getKey().getPresentableText(), a -> {
                    if (a.getValue() != null&&a.getValue().getObject()!=null) {
                        return a.getValue().getObject();
                    } else { return ""; }
                }
            ));
        ApplicationManager.getApplication().runWriteAction(
            new FileCreateTask(filePath, fileName, JSONObject.toJSONString(collect, true)));
    }

    /**
     * 生成方法中所用到的变量的mock方法
     *
     * @param method
     * @return
     */
    private String generateFieldMethodArg(PsiMethod method) {
        PsiParameterList parameters = method.getParameterList();
        return IntStream.range(0, parameters.getParametersCount()).mapToObj(
            i -> {
                getImport(parameters.getParameter(i).getType());
                return "any(" + CodeUtils.filterGeneric(parameters.getParameter(i)
                    .getType().getPresentableText()) + ".class)";
            }).collect(
            Collectors.joining(","));
    }

    /**
     * 范型类需要特殊处理
     *
     * @param type
     */
    private void getImportFromGeneric(PsiType type) {
        // 泛型类
        if (type instanceof PsiClassReferenceType) {
            PsiType[] parameters = ((PsiClassReferenceType)type).getParameters();
            if (parameters.length > 0) {
                for (int i = 0; i < parameters.length; i++) {
                    // 得到里面的类型
                    // todo 当有多层的时候
                    getImport(parameters[i]);
                }
            }
        }
    }

    private String getImport(PsiType returnType) {
        String fieldMethodReturnType = returnType.getPresentableText();
        if (isCollection(returnType)) {

            PsiType deepComponentType = PsiUtil.extractIterableTypeParameter(returnType, false);
            fieldMethodReturnType = deepComponentType.getPresentableText() + "List";
            needImports.add(String.format("import %s;\n", deepComponentType.getCanonicalText()));
        } else if ((returnType instanceof PsiArrayType) && !CodeUtils.isPrimitiveType(
            returnType.getDeepComponentType())) {
            needImports.add(
                String.format("import %s;\n", returnType.getDeepComponentType().getCanonicalText()));
        } else if (!CodeUtils.isPrimitiveType(returnType.getDeepComponentType())) {
            needImports.add(
                String.format("import %s;\n", CodeUtils.filterGeneric(returnType.getCanonicalText())));
            // todo 多层
            getImportFromGeneric(returnType);
        }
        return fieldMethodReturnType;
    }
}
