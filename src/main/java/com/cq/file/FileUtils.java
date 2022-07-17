package com.cq.file;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

/**
 * @author 有尘
 * @date 2021/9/28
 */
public class FileUtils {
    public static String getUnitFilePath(PsiFile psiFile) {
        String classPath = psiFile.getParent().getVirtualFile().getPath();
        return classPath.replace("/src/main/java", "/src/test/java");
    }

    public static String genJavaFileName(PsiClass psiClass) {
        String name = psiClass.getName();
        return name + "Test.java";
    }

    public static String getJsonFilePath(PsiFile psiFile) {

        String classPath = psiFile.getVirtualFile().getPath();
        classPath = classPath.replace("/src/main/java", "/src/test/java");
        classPath = classPath.replace(".java", "");
        return classPath.replace("java", "resources");
    }

    public static String getJsonFileName(String name) {
        return name + ".json";
    }
}
