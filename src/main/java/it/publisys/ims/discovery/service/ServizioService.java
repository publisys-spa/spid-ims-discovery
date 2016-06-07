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
import it.publisys.ims.discovery.error.ResourceNotFound;
import it.publisys.ims.discovery.repository.ServizioRaggiungibilitaRepository;
import it.publisys.ims.discovery.repository.ServizioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>08/02/16</pre>
 */
@Service
public class ServizioService {

    @Autowired
    private ServizioRepository servizioRepository;
    @Autowired
    private ServizioRaggiungibilitaRepository servizioRaggiungibilitaRepository;

    public Servizio findByApplId(String applId) throws ResourceNotFound {
        ServizioRaggiungibilita sr = servizioRaggiungibilitaRepository.findByAppId(applId)
                .orElseThrow(() -> new ResourceNotFound(String.format("ApplId '%s' non presente in archivio. Service Provider non supportato.", applId)));

        return servizioRepository.findByServizioRaggiungibilita(sr)
                .orElseThrow(() -> new ResourceNotFound(String.format("Servizio '%s' non presente in archivio. Service Provider non supportato.", applId)));
    }

}
