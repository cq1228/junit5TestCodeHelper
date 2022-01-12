package com.cq.toolwindow;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

/**
 * @author chenqiong
 * @date 2022/1/12
 */
public class MethodSelectDialog extends DialogWrapper {
    private final CollectionListModel<PsiMethod> methodList;
    private final LabeledComponent<JPanel> pannel;

    public MethodSelectDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        this.setTitle("配置测试用例");
        List<PsiMethod> collect = Arrays.asList(psiClass.getMethods()).stream().filter(
            (a) -> !a.getModifierList().hasModifierProperty("private")).collect(Collectors.toList());
        this.methodList = new CollectionListModel(collect);
        JBList fieldList = new JBList(this.methodList);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        JPanel panel = decorator.createPanel();
        this.pannel = LabeledComponent.create(panel, "选择要测试的方法");
        this.init();
    }

    @Override
    protected JComponent createCenterPanel() {
        return this.pannel;
    }

    public List<PsiMethod> getMethods() {
        return this.methodList.getItems();
    }
}
