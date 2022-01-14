package com.cq.file;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.cq.common.CodeUtils;
import com.cq.common.MockitoConstants;
import com.cq.valuegenerator.ValueContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiUtil;
import lombok.Data;

import static com.cq.common.MockitoConstants.BEFORE_SETUP;
import static com.cq.common.MockitoConstants.COMMON_ANNOTATION;
import static com.cq.common.MockitoConstants.COMMON_IMPORT;

/**
 * @author 有尘
 * @date 2021/9/28
 */
@Data
public class CodeGenerator {
    ValueContext valueContext = ValueContext.getContext();

    /**
     * 当前类
     */
    private PsiClass psiClass;
    /**
     * 需要mock的类变量
     */
    private List<PsiField> needMockFields;
    /**
     * 需要输出测试的方法
     */
    private List<MyMethod> needMockMethods;
    /**
     * 当前文件，主要为了获取路径
     */
    private PsiFile psiFile;
    private Set<String> needImports = new HashSet<>();
    private Map<String, Integer> methodCount = new HashMap<>();
    JsonFileGenerator jsonFileGenerator = new JsonFileGenerator();

    public CodeGenerator(List<PsiField> fields, List<PsiMethod> needMockMethods) {
        this.psiClass = ValueContext.getPsiClass();
        this.needMockFields = fields;
        this.needMockMethods = needMockMethods.stream().map(a -> new MyMethod(a, this)).collect(Collectors.toList());
        this.psiFile = ValueContext.getPsiFile();
        if (!ValueContext.isJsonFileSource()) {
            needImports.add("import com.util.TestUtils;\n");
            needImports.add("import org.junit.jupiter.params.provider.ValueSource;\n");
            saveTestUtils();
        }
    }

    public CodeGenerator(PsiClass psiClass, List<PsiMethod> needMockMethods, PsiFile psiFile) {
        this.psiClass = psiClass;
        this.needMockMethods = needMockMethods.stream().map(a -> new MyMethod(a, this)).collect(Collectors.toList());
        // todo 有一些小问题，比如不需要mock的fields
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(ValueContext.getPath().toFile());
        PsiJavaFile file = (PsiJavaFile)PsiManager.getInstance(ValueContext.getEvent().getProject()).findFile(virtualFile);
        PsiClass aClass = file.getClasses()[0];
        this.needMockFields = Arrays.asList(aClass.getAllFields());
        this.psiFile = psiFile;
    }

    public String genContent() {

        generateImports();
        for (MyMethod method : needMockMethods) {
            method.build();
            needImports.addAll(method.getNeedImports());
        }
        StringBuilder code = new StringBuilder();
        code.append(generatePackageInfo());
        code.append("\n");
        //todo 放到后面 code.append(generateImports());
        needImports.stream().filter(a -> a.contains(".")).forEach(a -> code.append(a));
        code.append(generateCommonUnitImport());
        code.append(generateClassDeclaration());
        code.append(generateTestObject());
        code.append(generateMockObjects());
        code.append(generateSetUpMethod());

        for (MyMethod method : needMockMethods) {
            code.append(method.getText());
        }
        code.append("}");
        return code.toString();

    }

    public String genMethodBody() {
        StringBuilder code = new StringBuilder();
        for (MyMethod method : needMockMethods) {
            method.build();
            code.append(method.getText());
        }
        return code.toString();
    }

    /**
     * 打包信息，package xxx.xxx
     *
     * @return
     */
    private String generatePackageInfo() {
        return String.format("package %s;\n", PsiUtil.getPackageName(psiClass));
    }

    /**
     * 生成所需要的对象依赖
     *
     * @return
     */
    private void generateImports() {
        needMockFields.stream().map(a -> {
            String canonicalText = a.getType().getCanonicalText();
            return CodeUtils.filterGeneric(canonicalText);
        }).distinct().forEach(t -> needImports.add(String.format("import %s;\n", t)));
    }

    /**
     * 通用依赖，包括junit、util、Mokito
     *
     * @return
     */
    private String generateCommonUnitImport() {
        return COMMON_IMPORT;
    }

    /**
     * 生成类描述
     *
     * @return
     */
    private String generateClassDeclaration() {
        return COMMON_ANNOTATION + String.format("public class %sTest  {\n", psiClass.getName());
    }

    /**
     * 生成测试对象
     *
     * @return
     */
    private String generateTestObject() {
        String name = psiClass.getName();
        return "\t@InjectMocks\n" + String.format("\tprivate %s %s=new %s(); \n", name, CodeUtils.getCamelCase(name),
            name);
    }

    /**
     * 生成需要mock的成员
     *
     * @return
     */
    private String generateMockObjects() {
        String collect = needMockFields.stream().map(a -> {
            String canonicalText = a.getType().getPresentableText();
            return "\t@Mock\n" + String.format("\tprivate %s %s; \n\n", canonicalText,
                a.getName());
        }).collect(Collectors.joining());

        return collect;
    }

    /**
     * 初始化测试对象
     *
     * @return
     */
    private String generateSetUpMethod() {
        return BEFORE_SETUP + "\n";
    }

    private void saveTestUtils() {
        String path = psiFile.getParent().getVirtualFile().getPath();
        String s = path.replaceAll("/src/main/java.*", "/src/test/java/com/util/");
        ApplicationManager.getApplication().runWriteAction(
            new FileCreateTask(s, "TestUtils.java", MockitoConstants.TEST_UTILS_CLASS));
    }
}
