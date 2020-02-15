package com.google.arcs.dfa

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference


class PopupDialogAction: AnAction() {
    override fun update(e: AnActionEvent) { // Using the event, evaluate the context, and enable or disable the action.
    }


    override fun actionPerformed(e: AnActionEvent) { // Using the event, implement an action. For example, create and show a dialog.
        // Using the event, create and show a dialog
        val currentProject: Project? = e.project
        if (currentProject == null) {
            Messages.showMessageDialog(
                "Cannot find project",
                "Error", Messages.getInformationIcon())
            return
        }
        if (e.presentation != null) {
            val dlgMsg = StringBuffer(e.presentation.text.toString() + " Selected!")
            val dlgTitle: String = e.presentation.description ?: "No description"
            // If an element is selected in the editor, add info about it.
            // If an element is selected in the editor, add info about it.
            val nav: Navigatable? = e.getData(CommonDataKeys.NAVIGATABLE)
            if (nav != null) {
                dlgMsg.append(java.lang.String.format("\nSelected Element: %s\n", nav.toString()))
            }
            val psi = e.getData(CommonDataKeys.PSI_FILE)
            if (psi != null) {
                
                psi.accept(new PsiRecursiveElementWalkingVisitor {
                    fun visitElement(element: PsiElement) {
                        for ((reference: PsiReference) in  element.getReferences()) {
                        if (reference instanceof WebReference) {
                            WebReference webReference = (WebReference) reference;
                            if (getIssueId(webReference.resolve()).isPresent()) {
                                references.add(webReference);
                            }
                        }
                    }
                        super.visitElement(element);
                    }
                });
                dlgMsg.append(java.lang.String.format("\nSelected File: %s", psi.getText()))
            }
//            val psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
//            if (psiElement != null) {
//                dlgMsg.append(java.lang.String.format("\nSelected Element: %s", psiElement.toString()))
//            }
            Messages.showMessageDialog(currentProject, dlgMsg.toString(), dlgTitle, Messages.getInformationIcon())
        } else {
            Messages.showMessageDialog(
                "Cannot find presentation",
                "Error", Messages.getInformationIcon())
        }
    }
}
