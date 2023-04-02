package com.matzip.server.global.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

@Configuration
@Profile("prod")
public class SecretsManagerConfig implements BeanFactoryPostProcessor {
    private final SecretsManagerClient client = SecretsManagerClient.builder().region(Region.AP_NORTHEAST_2).build();

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        new Gson().<Map<String, String>>fromJson(getSecret(), new TypeToken<Map<String, String>>() {}.getType())
                .forEach(System::setProperty);
    }

    private String getSecret() {
        return client.getSecretValue(GetSecretValueRequest.builder().secretId("matzip").build()).secretString();
    }
}
