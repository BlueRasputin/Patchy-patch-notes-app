package com.barrcon.patchy;

import com.intellij.ide.BrowserUtil;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class PatchyActions {
    private PatchyActions() {
    }

    public static final class Check extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            if (e.getProject() != null) {
                PatchyScanner.get(e.getProject()).scan(true);
            }
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(e.getProject() != null);
        }

        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    }

    public static final class SignIn extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            Project project = e.getProject();
            PatchySettings.Values settings = PatchySettings.get();
            BrowserUtil.browse(settings.websiteUrl + "/connect?client=IntelliJ");
            String token = Messages.showPasswordDialog(project,
                    "Copy the token shown on the Patchy page that just opened and paste it here:", "Patchy Sign In", null);
            if (token == null || token.isBlank()) {
                return;
            }
            try {
                PatchyStore.update(PatchyStore.CREDENTIALS, credentials -> {
                    credentials.addProperty("apiUrl", settings.apiUrl);
                    credentials.addProperty("token", token.strip());
                });
            } catch (IOException ex) {
                Messages.showErrorDialog(project, "Could not save credentials: " + ex.getMessage(), "Patchy");
                return;
            }
            balloon(project, "Signed in to Patchy.");
            if (project != null) {
                PatchyScanner.get(project).scan(false);
            }
        }
    }

    public static final class SignOut extends AnAction {
        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            try {
                PatchyStore.update(PatchyStore.CREDENTIALS, credentials -> credentials.remove("token"));
            } catch (IOException ex) {
                Messages.showErrorDialog(e.getProject(), "Could not update credentials: " + ex.getMessage(), "Patchy");
                return;
            }
            balloon(e.getProject(), "Signed out of Patchy.");
        }
    }

    private static void balloon(Project project, String message) {
        NotificationGroupManager.getInstance().getNotificationGroup("Patchy")
                .createNotification(message, NotificationType.INFORMATION)
                .notify(project);
    }
}
