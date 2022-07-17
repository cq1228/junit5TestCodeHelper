package com.cq.toolwindow;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import com.cq.common.CodeUtils;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

/**
 * @author 有尘
 * @date 2021/11/29
 */
public class CodeGeneratorDialog extends DialogWrapper {
    private JPanel panel1;
    private JComboBox source;
    private JLabel testSourceLable;
    private JPanel fieldPanel;
    private JPanel methodPanel;
    private JLabel methodLabel;
    private JLabel fieldLabel;

    private  CollectionListModel<PsiMethod> myMethods;
    private CollectionListModel<PsiField> myFields;
    private PsiClass psiClass;

    public CodeGeneratorDialog( PsiClass psiClass) {
        super(psiClass.getProject());
        this.psiClass=psiClass;
        initMethodPanel();
        initFieldPanel();
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return this.panel1;
    }
    public String getData(){
        return source.getSelectedItem().toString();
    }

    public List<PsiField> getFields() {
        return myFields.getItems();
    }


    public List<PsiMethod> getMethods() {
        return myMethods.getItems();
    }

    public void initMethodPanel(){
        List<PsiMethod> collect = Arrays.asList(psiClass.getMethods()).stream().filter(
            a -> !a.getModifierList().hasModifierProperty("private")).collect(
            Collectors.toList());
        myMethods = new CollectionListModel<>(collect);
        JBList jMethodList = new JBList(myMethods);
        jMethodList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(jMethodList);
        JPanel panel = decorator.createPanel();
        LabeledComponent<JPanel> labeledComponent = LabeledComponent.create(panel, "");
        methodPanel.add(labeledComponent);

    }

    public void initFieldPanel(){

        List<PsiField> collect = Arrays.stream(psiClass.getAllFields()).filter(
            a -> !CodeUtils.isPrimitiveType(a.getType())).collect(
            Collectors.toList());
        myFields = new CollectionListModel(collect);
        JList Jfields=new JBList(myFields);
        Jfields.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(Jfields);
        JPanel panel = decorator.createPanel();
        LabeledComponent<JPanel> tste = LabeledComponent.create(panel, "");
        fieldPanel.add(tste);

    }

}
