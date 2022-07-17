package com.cq.valuegenerator.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.cq.common.CodeUtils;
import com.cq.file.FileUtils;
import com.github.javafaker.Faker;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiUtil;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT;

/**
 * 系统环境变量
 *
 * @author 有尘
 * @date 2021/9/29
 */
public class ValueContext {
    public static ValueContext INSTANCE = new ValueContext();
    public static boolean isJsonFileSource = false;

    Faker faker = new Faker();
    /**
     * 事件
     */
    public static AnActionEvent event;
    /**
     * 目标java类
     */
    public static PsiClass psiClass;

    /**
     * java文件
     */
    public static PsiFile psiFile;
    /**
     * 测试文件路径
     */
    public static String filePath;
    /**
     * 测试文件名称
     */
    public static String fileName;
    /**
     * 测试文件路径
     */
    public static Path path;

    public static PsiClass getPsiClass() {
        return psiClass;
    }

    public static PsiFile getPsiFile() {
        return psiFile;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static String getFileName() {
        return fileName;
    }

    public static Path getPath() {
        return path;
    }

    public static void setEvent(AnActionEvent e) {
        event = e;
        psiClass = PsiUtil.getTopLevelClass(event.getData(PSI_ELEMENT));
        psiFile = event.getData(CommonDataKeys.PSI_FILE);
        filePath = FileUtils.getUnitFilePath(psiFile);
        fileName = FileUtils.genJavaFileName(psiClass);
        path = Paths.get(filePath, fileName);
    }

    public static AnActionEvent getEvent() {
        return event;
    }

    public static void setIsJsonFileSource(boolean v) {
        isJsonFileSource = v;
    }

    public static boolean isJsonFileSource() {
        return isJsonFileSource;
    }

    public static ValueContext getContext() {
        return INSTANCE;
    }

    private final Map<String, PsiClass> cachedCLass = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, PsiMethod> cachedMethod = Collections.synchronizedMap(new HashMap<>());

    public Faker getFaker() {
        return faker;
    }

    public void loadClass(PsiClass psiClass) {
        Arrays.stream(psiClass.getAllFields())
            .filter(a -> !CodeUtils.isPrimitiveType(a.getType()))
            .forEach(field -> {
                    String fieldTypeName = field.getType().getCanonicalText();
                    PsiClass psiClass1 = PsiUtil.resolveClassInClassTypeOnly(field.getType());
                    if (psiClass1 != null) {
                        cachedCLass.put(fieldTypeName, psiClass1);
                        PsiMethod[] methods = psiClass1.getAllMethods();
                        for (PsiMethod a : methods) {
                            if (!a.getModifierList().hasModifierProperty("private")) {
                                // 参数列表
                                PsiParameterList parameterList = a.getParameterList();
                                String name = fieldTypeName + a.getName() + parameterList.getParametersCount();
                                String shortName = field.getType().getPresentableText() + a.getName() + parameterList
                                    .getParametersCount();
                                System.out.println("cache method: " + name);
                                cachedMethod.put(name, a);
                                cachedMethod.put(shortName, a);
                                for (int i = 0; i < parameterList.getParametersCount(); i++) {
                                    PsiType fieldArgType = parameterList.getParameters()[i].getType();
                                    PsiClass fieldArgClass = PsiUtil.resolveClassInClassTypeOnly(fieldArgType);
                                    cachedCLass.put(fieldArgType.getCanonicalText(), fieldArgClass);
                                }
                            }

                        }
                    }
                }
            );
    }

    public PsiClass getClass(String typeName) {
        return cachedCLass.get(typeName);
    }

    public PsiMethod getMethod(String className, String methodName, int argSize) {
        return getMethod(className + methodName + argSize);
    }

    public PsiMethod getMethod(String methodKey) {
        return cachedMethod.get(methodKey);
    }

    public void clear() {
        cachedCLass.clear();
        cachedMethod.clear();
    }
}
