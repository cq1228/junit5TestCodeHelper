package com.cq.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.intellij.psi.PsiBlockStatement;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIfStatement;
import com.intellij.psi.PsiLoopStatement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiStatement;
import com.intellij.psi.PsiType;

/**
 * @author 有尘
 * @date 2021/9/28
 */
public class CodeUtils {
    public static Set<String> PRIMITIVE_TYPES = new HashSet() {{
        add("Boolean");
        add("Float");
        add("Double");
        add("Integer");
        add("Long");
        add("Number");
        add("Character");
        add("CharSequence");
        add("String");
        add("Date");
        add("Byte");
    }};

    public static String getCamelCase(String str) {
        String substring = str.substring(1);
        return Character.toLowerCase(str.charAt(0)) + substring;
    }

    public static boolean isPrimitiveType(PsiType type) {
        return PRIMITIVE_TYPES.contains(type.getPresentableText()) || (type instanceof PsiPrimitiveType);
    }

    public static int getCount(String source, String c) {
        int length = source.replaceAll("\\{.*}", "").split(c).length;
        if (length > 1) {
            return length;
        } else if (source.endsWith("()")) {
            return 0;
        } else {
            return 1;
        }
    }

    public static String filterGeneric(String canonicalText) {
        int i = canonicalText.indexOf("<");
        if (i >= 0) {
            canonicalText = canonicalText.substring(0, i);
        }
        return canonicalText;
    }

    public static String getBody(PsiMethod method, Map<PsiMethod, Boolean> codeBlockMap) {
        PsiCodeBlock body = method.getBody();
        if(body==null){
            return "";
        }
        codeBlockMap.put(method, false);

        PsiStatement[] statements = body.getStatements();
        String collect = Arrays.stream(statements).map(
            a -> getString(a)).collect(Collectors.joining());

        String s = codeBlockMap.entrySet().stream().filter(
            a -> a.getValue() && (collect.contains(a.getKey().getName() + "(") || collect.contains(
                "super." + a.getKey().getName() + "("))).map(
            a -> getBody(a.getKey(), codeBlockMap)).collect(Collectors.joining()) + collect;
        return s;
    }

    public static String getString(PsiStatement psiStatement) {
        StringBuilder sb = new StringBuilder();
        PsiElement[] children = psiStatement.getChildren();
        for (int i = 0; i < children.length; i++) {
            PsiElement child = children[i];
            if (child instanceof PsiLoopStatement) {
                sb.append(getString(((PsiLoopStatement)child).getBody()));
            } else if (child instanceof PsiBlockStatement) {
                PsiStatement[] statements = ((PsiBlockStatement)child).getCodeBlock().getStatements();
                String collect = Arrays.stream(statements).map(a ->
                {
                    if ((a instanceof PsiLoopStatement) || (a instanceof PsiIfStatement)) {
                        return getString(a);
                    } else {
                        return "\n" + a.getText().replace("\n", "");
                    }
                }).collect(Collectors.joining("\n"));
                sb.append(collect);
            } else {
                sb.append("\n" + child.getText().replace("\n", ""));
            }
        }
        return "\n" + sb.toString();
    }

    public static String getStringOnlyBlock(String line, String name) {
        System.out.println("line:" + line);
        System.out.println("name:" + name);

        line = line.replaceAll("\\{.*}", "");
        Stack<Character> stack = new Stack<>();
        int count = -1;
        int index = 0;
        while (!stack.isEmpty() || index < line.length()) {
            char c = line.charAt(index);
            if (c == '(') {
                count++;
                stack.push('(');
            } else if (c == ')') {
                if (count == 0) {
                    stack.push(c);
                    break;
                }
                while (!stack.empty() && stack.peek() != '(') {
                    stack.pop();
                }
                if (stack.peek() == '(') {
                    stack.pop();
                    count--;
                }
            } else {
                stack.push(c);
            }
            index++;
        }
        StringBuilder sb = new StringBuilder();
        while (!stack.isEmpty()) {
            sb.append(stack.pop());
        }
        return sb.reverse().toString();
    }

}
