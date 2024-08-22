package io.ivyteam.devops.github;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import io.ivyteam.devops.settings.SettingsManager;

public class WebhookCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return "axonivy".equals(SettingsManager.INSTANCE.get().gitHubOrg());
    }
}
