package com.cq;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

import com.cq.file.JsonFileGenerator;
import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_ELEMENT;

/**
 * @author cq
 */
@Slf4j
public class ModelToJsonAction extends AnAction {
    public static final NotificationGroup notificationGroup = new NotificationGroup("junitCode",
        NotificationDisplayType.BALLOON, true);

    ModelToJsonAction(){
        getTemplatePresentation().setText("生成json数据");
    }

    /**
     * @see com.intellij.openapi.actionSystem.AnAction#actionPerformed(com.intellij.openapi.actionSystem.AnActionEvent)
     */
    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiClass topLevelClass = PsiUtil.getTopLevelClass(e.getData(PSI_ELEMENT));
        Map<String, Object> fieldMap = Arrays.stream(topLevelClass.getAllFields()).collect(
            Collectors.toMap(field -> field.getName(), field -> JsonFileGenerator.typeResolve(field.getType(), 0),(a,b)->b));
        String s = JSONObject.toJSONString(fieldMap, true);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(s);
        clipboard.setContents(selection, selection);
        String message = topLevelClass.getName() + "类的json格式已复制到剪切板";
        Notification success = notificationGroup.createNotification(message, NotificationType.INFORMATION);
        Notifications.Bus.notify(success, e.getProject());
    }

    /**
     * @see com.intellij.openapi.actionSystem.AnAction#update(com.intellij.openapi.actionSystem.AnActionEvent)
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        FileType fileType = psiFile.getFileType();
        if (!((fileType instanceof JavaFileType)||(fileType instanceof JavaClassFileType))) {
            log.warn("setEnabled{}", false);
            e.getPresentation().setEnabled(false);
            return;
        }

    }
}
