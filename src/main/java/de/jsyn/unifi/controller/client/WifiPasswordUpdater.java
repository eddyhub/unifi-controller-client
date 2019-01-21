package de.jsyn.unifi.controller.client;

import de.jsyn.unifi.controller.client.api.DefaultApi;
import de.jsyn.unifi.controller.client.model.Login;
import de.jsyn.unifi.controller.client.model.WlanConf;

import java.util.Optional;

public class WifiPasswordUpdater {

    private DefaultApi controller;

    public WifiPasswordUpdater(String basePath, Login login) throws ApiException {
        controller = new DefaultApi(new ApiClient().setBasePath(basePath));
        controller.login(login);
    }

    public void updatePassword(String siteName, String wifiName, String password) throws ApiException {
        final Optional<WlanConf> wlanConf = controller.listWlanConfigs(siteName).getData()
                .stream()
                .filter(config -> config.getName().equals(wifiName))
                .findFirst();
        wlanConf.ifPresent(conf -> {
            conf.setXPassphrase(password);
            try {
                controller.updateWlanConfig(siteName, conf.getId(), conf);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
