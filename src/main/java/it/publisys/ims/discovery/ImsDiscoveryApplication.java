/*
 * IMS SPID Discovery
 * Copyright (c) 2016 Publisys S.p.A. srl (http://www.publisys.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.publisys.ims.discovery;

import org.guanxi.common.security.SecUtilsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ImsDiscoveryApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(ImsDiscoveryApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(ImsDiscoveryApplication.class, args);
    }

    @Bean
    public SecUtilsConfig secUtilsConfig() {
        // Initialise the signing information
        SecUtilsConfig secUtilsConfig = new SecUtilsConfig();
        secUtilsConfig.setKeystoreFile(idpSecKeystoreFile);
        secUtilsConfig.setKeystorePass(idpSecKeystorePass);
        secUtilsConfig.setKeystoreType(idpSecKeystoreType);
        secUtilsConfig.setPrivateKeyAlias(idpSecPrivateKeyAlias);
        secUtilsConfig.setPrivateKeyPass(idpSecPrivateKeyPass);
        secUtilsConfig.setCertificateAlias(idpSecCertificateAlias);
        return secUtilsConfig;
    }

    //
    @Value("${idp.sec.keystore.file}")
    private String idpSecKeystoreFile;
    @Value("${idp.sec.keystore.pass}")
    private String idpSecKeystorePass;
    @Value("${idp.sec.keystore.type}")
    private String idpSecKeystoreType;
    @Value("${idp.sec.private.key.alias}")
    private String idpSecPrivateKeyAlias;
    @Value("${idp.sec.private.key.pass}")
    private String idpSecPrivateKeyPass;
    @Value("${idp.sec.certificate.alias}")
    private String idpSecCertificateAlias;
}
