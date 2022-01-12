package com.cq;

import java.nio.file.Files;

import com.cq.file.CodeGenerator;
import com.cq.file.FileCreateTask;
import com.cq.toolwindow.CodeGeneratorDialog;
import com.cq.valuegenerator.ValueContext;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.PsiFile;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 单元测试生成的action
 *
 * @author chenqiong
 */
@Slf4j
public class ClassCodeGenerateAction extends AnAction {

    public ClassCodeGenerateAction() {
        getTemplatePresentation().setText("生成单元测试类");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        ValueContext.setEvent(e);
        if (Files.exists(ValueContext.getPath())) {
            System.out.println("文件已经存在需要覆盖吗？");
            new OpenFileDescriptor(project,
                LocalFileSystem.getInstance().refreshAndFindFileByPath(ValueContext.getPath().toString()))
                .navigate(true);
            return;
        }

        CodeGeneratorDialog radioSelection = new CodeGeneratorDialog(ValueContext.getPsiClass());
        ValueContext.getContext().loadClass(ValueContext.getPsiClass());
        radioSelection.show();
        if (radioSelection.isOK()) {
            String source = radioSelection.getData();
            ValueContext.setIsJsonFileSource(source.equals("JsonFileSource"));
            // 生成java文件
            CodeGenerator codeGenerator = new CodeGenerator(radioSelection.getFields(), radioSelection.getMethods());
            ApplicationManager.getApplication().runWriteAction(
                new FileCreateTask(ValueContext.getFilePath(), ValueContext.getFileName(), codeGenerator.genContent()));

        }

    }
    /**
     * 只有java文件才能使用该功能
     *
     * @see com.intellij.openapi.actionSystem.AnAction#update(com.intellij.openapi.actionSystem.AnActionEvent)
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
