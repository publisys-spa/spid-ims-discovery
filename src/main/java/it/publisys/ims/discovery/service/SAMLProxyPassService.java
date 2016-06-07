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

import it.publisys.ims.discovery.domain.SAMLProxyPass;
import it.publisys.ims.discovery.error.ResourceNotFound;
import it.publisys.ims.discovery.repository.SAMLProxyPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>08/02/16</pre>
 */
@Service
public class SAMLProxyPassService {

    @Autowired
    private SAMLProxyPassRepository samlProxyPassRepository;

    public SAMLProxyPass findById(Long id) throws ResourceNotFound {
        SAMLProxyPass sr = samlProxyPassRepository.findOne(id);
        if (sr == null) {
            throw new ResourceNotFound(String.format("ID '%s' non presente in archivio. Richiesta non valida.", id));
        }
        return sr;
    }

    public SAMLProxyPass findByRelayState(String relayState) throws ResourceNotFound {
        return samlProxyPassRepository.findByRelayState(relayState)
                .orElseThrow(() -> new ResourceNotFound(String.format("RelayState '%s' non presente in archivio. Richiesta non valida.", relayState)));
    }

    public SAMLProxyPass create(String relayState, String requestBinding, String requestDoc,
                                String authnReqId, Date authnReqIssueInstant, String authnReqIssuer) {
        SAMLProxyPass samlProxyPass = new SAMLProxyPass();
        samlProxyPass.setRequestBinding(requestBinding);
        samlProxyPass.setRelayState(relayState);
        samlProxyPass.setRequestDoc(requestDoc);
        samlProxyPass.setAuthnReqId(authnReqId);
        samlProxyPass.setAuthnReqIssueInstant(authnReqIssueInstant);
        samlProxyPass.setAuthnReqIssuer(authnReqIssuer);

        return samlProxyPassRepository.save(samlProxyPass);
    }

    public SAMLProxyPass update(SAMLProxyPass samlProxyPass, String responseDoc,
                                String respId, Date respIssueInstant, String respIssuer) {
        samlProxyPass.setRespId(respId);
        samlProxyPass.setRespIssueInstant(respIssueInstant);
        samlProxyPass.setRespIssuer(respIssuer);
        samlProxyPass.setResponseDoc(responseDoc);

        return samlProxyPassRepository.save(samlProxyPass);
    }

}
