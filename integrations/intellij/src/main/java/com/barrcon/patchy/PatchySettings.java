package com.barrcon.patchy;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.util.Objects;

@Service
@State(name = "PatchySettings", storages = @Storage("patchy.xml"))
public final class PatchySettings implements PersistentStateComponent<PatchySettings.Values> {

    public static final class Values {
        public String apiUrl = "http://localhost:8080";
        public String websiteUrl = "http://localhost:5173";
        public int pollMinutes = 60;
    }

    private Values values = new Values();

    static Values get() {
        return ApplicationManager.getApplication().getService(PatchySettings.class).values;
    }

    @Override
    public @NotNull Values getState() {
        return values;
    }

    @Override
    public void loadState(@NotNull Values state) {
        values = state;
    }

    public static final class Page implements Configurable {
        private final JBTextField apiUrl = new JBTextField();
        private final JBTextField websiteUrl = new JBTextField();
        private final JSpinner pollMinutes = new JSpinner(new SpinnerNumberModel(60, 1, 24 * 60, 1));

        @Override
        public String getDisplayName() {
            return "Patchy";
        }

        @Override
        public JComponent createComponent() {
            return FormBuilder.createFormBuilder()
                    .addLabeledComponent("API URL:", apiUrl)
                    .addLabeledComponent("Website URL:", websiteUrl)
                    .addLabeledComponent("Check every (minutes):", pollMinutes)
                    .addComponentFillVertically(new JPanel(), 0)
                    .getPanel();
        }

        @Override
        public boolean isModified() {
            Values v = get();
            return !Objects.equals(clean(apiUrl.getText()), v.apiUrl)
                    || !Objects.equals(clean(websiteUrl.getText()), v.websiteUrl)
                    || (int) pollMinutes.getValue() != v.pollMinutes;
        }

        @Override
        public void apply() {
            Values v = get();
            v.apiUrl = clean(apiUrl.getText());
            v.websiteUrl = clean(websiteUrl.getText());
            v.pollMinutes = (int) pollMinutes.getValue();
        }

        @Override
        public void reset() {
            Values v = get();
            apiUrl.setText(v.apiUrl);
            websiteUrl.setText(v.websiteUrl);
            pollMinutes.setValue(v.pollMinutes);
        }

        private static String clean(String url) {
            String trimmed = url.strip();
            return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        }
    }
}
