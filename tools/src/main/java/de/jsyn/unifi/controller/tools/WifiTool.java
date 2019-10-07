package de.jsyn.unifi.controller.tools;

import de.jsyn.unifi.controller.client.ApiClient;
import de.jsyn.unifi.controller.client.ApiException;
import de.jsyn.unifi.controller.client.JSON;
import de.jsyn.unifi.controller.client.api.DefaultApi;
import de.jsyn.unifi.controller.client.model.Login;
import de.jsyn.unifi.controller.client.model.WlanConf;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientConfiguration;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class WifiTool {

    private DefaultApi controller;

    WifiTool(String basePath, Login login, boolean insecure) throws ApiException {
        ApiClient apiClient = new ApiClient().setBasePath(basePath);
        if (insecure) {
            apiClient.setHttpClient(getNonSslClient(apiClient.isDebugging(), apiClient.getJSON()));
        }

        controller = new DefaultApi(apiClient);
        controller.login(login);
    }

    private Client getNonSslClient(boolean debugging, JSON provider) {
        final ClientConfiguration clientConfig = new ClientConfiguration(ResteasyProviderFactory.getInstance());
        clientConfig.register(provider);
        if (debugging) {
            clientConfig.register(Logger.class);
        }
        return new ResteasyClientBuilder().withConfig(clientConfig)
                .disableTrustManager().build();
    }

    void updatePassword(String siteName, String wifiName, String password) throws ApiException {
        final Optional<WlanConf> wlanConf = controller.listWlanConfigs(siteName).getData()
                .stream()
                .filter(config -> config.getName().equals(wifiName))
                .findFirst();
        wlanConf.ifPresent(conf -> {
            conf.setxPassphrase(password);
            try {
                controller.updateWlanConfig(siteName, conf.getId(), conf);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    List<String> getWifiList(String siteName) throws ApiException {
        return controller.listWlanConfigs(siteName).getData()
                .stream()
                .map(WlanConf::getName)
                .collect(Collectors.toList());
    }
}
