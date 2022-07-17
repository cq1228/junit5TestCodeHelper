package com.cq.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.cq.valuegenerator.impl.ValueContext;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.lang3.StringUtils;

/**
 * @author chenqiong
 * @date 2021/9/27
 */
public class FileCreateTask implements Runnable {
    private final String path;
    private final String name;
    private final String content;

    public FileCreateTask(String path, String name, String content) {
        this.path = path;
        this.name = name;
        this.content = content;
    }

    @Override
    public void run() {
        Path path = Paths.get(this.path);
        try {
            if (StringUtils.isEmpty(content)) {
                return;
            }
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            Files.write(Paths.get(this.path, name), content.getBytes());
            if (name.endsWith(".java")&&!name.contains("TestUtil")) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                        Paths.get(this.path, name).toString());
                Project project = ValueContext.getEvent().getProject();
                new OpenFileDescriptor(project, virtualFile).navigate(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPath() {
        return path;
    }
}
