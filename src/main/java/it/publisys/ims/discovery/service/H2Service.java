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

package it.publisys.ims.discovery.service;

import it.publisys.ims.discovery.domain.Servizio;
import it.publisys.ims.discovery.domain.ServizioRaggiungibilita;
import it.publisys.ims.discovery.domain.ServizioSicurezza;
import it.publisys.ims.discovery.repository.ServizioRaggiungibilitaRepository;
import it.publisys.ims.discovery.repository.ServizioRepository;
import it.publisys.ims.discovery.repository.ServizioSicurezzaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>01/06/16</pre>
 */
@Service
@Profile(value = "dev")
public class H2Service {

    private static final Logger log = LoggerFactory.getLogger(H2Service.class);

    @Autowired
    private ServizioRepository servizioRepository;
    @Autowired
    private ServizioSicurezzaRepository servizioSicurezzaRepository;
    @Autowired
    private ServizioRaggiungibilitaRepository servizioRaggiungibilitaRepository;

    @PostConstruct
    public void init() {

        log.debug("init DB H2 for test");


        buildServizio("1.0", "Servizio 1", "Descrizione Servizio 1", 1,
                buildServizioSicurezza(11),
                buildServizioRaggiungibilita("sp1-guard", "https", 443, "www.example.it", "sp1", "......"));

        buildServizio("1.0", "Servizio 2", "Descrizione Servizio 2", 1,
                buildServizioSicurezza(12),
                buildServizioRaggiungibilita("sp2-guard", "https", 443, "www.example.it", "sp2", "......"));

        buildServizio("1.0", "Servizio Portal", "Descrizione Servizio Portal", 1,
                buildServizioSicurezza(11),
                buildServizioRaggiungibilita("ibashoportal-guard", "https", 443, "www.example.it", "ibashoportal", "......"));

    }

    private void buildServizio(String versione, String name, String description, int state,
                               ServizioSicurezza servizioSicurezza,
                               ServizioRaggiungibilita servizioRaggiungibilita) {

        Servizio serv = new Servizio();
        serv.setVersione(versione);
        serv.setName(name);
        serv.setDescription(description);
        serv.setState(state);
        serv.setServizioSicurezza(servizioSicurezza);
        serv.setServizioRaggiungibilita(servizioRaggiungibilita);

        servizioRepository.save(serv);
    }

    private ServizioSicurezza buildServizioSicurezza(int permissionLevel) {
        ServizioSicurezza ss = new ServizioSicurezza();
        ss.setPermissionLevel(permissionLevel);
        return servizioSicurezzaRepository.save(ss);
    }

    private ServizioRaggiungibilita buildServizioRaggiungibilita(String appId, String protocol, int port,
                                                                 String address, String context, String uri) {
        ServizioRaggiungibilita sr = new ServizioRaggiungibilita();
        sr.setAppId(appId);
        sr.setProtocol(protocol);
        sr.setPort(port);
        sr.setAddress(address);
        sr.setContext(context);
        sr.setUri(uri);
        return servizioRaggiungibilitaRepository.save(sr);
    }

}
