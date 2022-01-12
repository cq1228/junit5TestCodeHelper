package com.cq;

import java.util.ArrayList;
import java.util.List;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * @author 有尘
 * @date 2021/9/27
 */
public class CodeGroupTest extends ActionGroup {

    /**
     * @see com.intellij.openapi.actionSystem.ActionGroup#getChildren(com.intellij.openapi.actionSystem.AnActionEvent)
     */
    @NotNull
    @Override
    public AnAction[] getChildren(AnActionEvent anActionEvent) {
        if (anActionEvent == null) {
            return AnAction.EMPTY_ARRAY;
        }
        Project project = PlatformDataKeys.PROJECT.getData(anActionEvent.getDataContext());
        if (project == null) {
            return AnAction.EMPTY_ARRAY;
        }
        final List<AnAction> children = new ArrayList<>();
        AnAction classCodeGenerateAction = getAction("ClassCodeGenerateAction", ClassCodeGenerateAction.class);
        AnAction modelToJsonAction = getAction("ModelToJsonAction", ModelToJsonAction.class);
        AnAction methodCodeGenerateAction =getAction("MethodCodeGenerateAction", MethodCodeGenerateAction.class);
        children.add(classCodeGenerateAction);
        children.add(modelToJsonAction);
        children.add(methodCodeGenerateAction);
        return children.toArray(new AnAction[children.size()]);
    }

    /**
     * 获取或新增一个子元素
     *
     * @return
     */
    private AnAction getAction(String name, Class<? extends AnAction> t) {
        final String actionId = "JCode5.Menu.Action." + name;
        AnAction action = ActionManager.getInstance().getAction(actionId);
        if (action == null) {
            try {
                action = t.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("系统错误");
            }
            ActionManager.getInstance().registerAction(actionId, action);
        }
        return action;
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
        if (!(fileType instanceof JavaFileType)) {
            e.getPresentation().setEnabled(false);
            return;
        }
    }
}
