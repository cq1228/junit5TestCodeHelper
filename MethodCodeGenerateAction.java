package com.cq;

import java.nio.file.Files;
import java.util.List;

import com.cq.file.CodeGenerator;
import com.cq.file.FileAppendTask;
import com.cq.toolwindow.MethodSelectDialog;
import com.cq.valuegenerator.ValueContext;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 单元测试生成的action
 *
 * @author youchen
 */
@Slf4j
public class MethodCodeGenerateAction extends AnAction {

    public MethodCodeGenerateAction() {
        getTemplatePresentation().setText("增加测试方法");
    }

    @SneakyThrows
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        ValueContext.setEvent(e);
        if (!Files.exists(ValueContext.getPath())) {
            String message = ("测试类不存在，请先生成单元测试类");
            Messages.showMessageDialog(project, message, "Generate Failed", null);
            return;
        }
        boolean valueSource = Files.readAllLines(ValueContext.getPath()).stream().anyMatch(
            a -> a.contains("ValueSource"));
        ValueContext.setIsJsonFileSource(!valueSource);
        PsiClass psiClass = ValueContext.getPsiClass();
        MethodSelectDialog methodSelectDialog = new MethodSelectDialog(psiClass);
        ValueContext.getContext().loadClass(psiClass);

        methodSelectDialog.show();
        if (methodSelectDialog.isOK()) {
            List<PsiMethod> methods = methodSelectDialog.getMethods();
            // 生成方法
            CodeGenerator codeGenerator = new CodeGenerator(psiClass, methods, ValueContext.getPsiFile());

            String s = codeGenerator.genMethodBody();
            //VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByIoFile(
            //    ValueContext.getPath().toFile());
            //
            //PsiJavaFile file = (PsiJavaFile)PsiManager.getInstance(ValueContext.getEvent().getProject()).findFile(
            //    virtualFile);
            //
            //PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(file.getProject());
            //String body="public void test(){}";
            //PsiMethod methodFromText = elementFactory.createMethodFromText(s, file.getClasses()[0]);
            //psiClass.add(methodFromText);

            //ApplicationManager.getApplication().runWriteAction(
                new FileAppendTask(ValueContext.getPath().toFile(),s).run();
        }
    }

    /**
     * 只有java文件才能使用该功能
     *
     * @see AnAction#update(AnActionEvent)
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        FileType fileType = psiFile.getFileType();
        if (!((fileType instanceof JavaFileType) || (fileType instanceof JavaClassFileType))) {
            e.getPresentation().setEnabled(false);
            return;
        }

    }
}
