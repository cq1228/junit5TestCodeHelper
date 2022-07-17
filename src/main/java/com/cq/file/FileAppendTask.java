package com.cq.file;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Pattern;

import com.cq.valuegenerator.impl.ValueContext;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author 有尘
 * @date 2022/1/12
 */
public class FileAppendTask implements Runnable {
    private final File file;
    private final String text;

    public FileAppendTask(File file, String text) {
        this.file = file;
        this.text = text;
    }

    @Override
    public void run() {

        try {
            StringBuilder sb = new StringBuilder();
            List<String> strings = Files.readAllLines(ValueContext.getPath());
            for (int i = 0; i < strings.size(); i++) {
                if (Pattern.matches("^}", strings.get(i))) {
                    sb.append(text);
                    sb.append("\n");
                    sb.append("}");
                    break;
                } else {
                    sb.append(strings.get(i));
                    sb.append("\n");
                }
            }
            Files.write(ValueContext.getPath(), sb.toString().getBytes());
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                ValueContext.getPath().toString());
            new OpenFileDescriptor(ValueContext.getEvent().getProject(), virtualFile).navigate(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
