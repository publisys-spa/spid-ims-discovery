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

package it.publisys.ims.discovery.controller;

import it.publisys.ims.discovery.Keys;
import it.publisys.ims.discovery.component.LogoutComponent;
import it.publisys.ims.discovery.domain.SAMLProxyPass;
import it.publisys.ims.discovery.domain.Servizio;
import it.publisys.ims.discovery.dto.xml.Idps;
import it.publisys.ims.discovery.error.ResourceNotFound;
import it.publisys.ims.discovery.service.SAMLProxyPassService;
import it.publisys.ims.discovery.service.ServizioService;
import it.publisys.ims.discovery.util.RequestUtil;
import org.apache.xmlbeans.XmlException;
import org.guanxi.common.GuanxiException;
import org.guanxi.common.Utils;
import org.guanxi.common.definitions.Guanxi;
import org.guanxi.common.definitions.SAML;
import org.guanxi.common.entity.EntityFarm;
import org.guanxi.common.entity.EntityManager;
import org.guanxi.xal.saml_2_0.protocol.AuthnRequestDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>05/02/16</pre>
 */
@Controller
@RequestMapping("/shib")
public class SSOController {

    private static final Logger log = LoggerFactory.getLogger(SSOController.class);
    //
    private static final String RELAY_STATE = "RelayState";
    private static final String SAML_REQUEST = "SAMLRequest";
    private static final String SIGALG = "SigAlg";
    private static final String SIGNATURE = "Signature";

    //
    @Value("${spid.backurl}")
    private String backurl;
    @Value("${spid.shib.logout}")
    private String shibLogoutUrl;
    @Value("${spid.shib.sso.login}")
    private String shibSSOLogin;
    // EID SPID
    @Value("${spid.entityid.infocert}")
    private String entityidInfocert;
    @Value("${spid.entityid.poste}")
    private String entityidPoste;
    @Value("${spid.entityid.telecom}")
    private String entityidTelecom;
    // LIST CUSTOM IDP
    @Value("classpath:${spid.idp.file}")
    private Resource idpFile;

    //
    @Autowired
    private SAMLProxyPassService samlProxyPassService;
    @Autowired
    private ServizioService servizioService;
    @Autowired
    private LogoutComponent logoutComponent;

    /**
     * Riceve l'AuthnRequest tramite POST
     *
     * @param model   spring model
     * @param request {@link HttpServletRequest}
     * @return return della view di discovery
     */
    @RequestMapping(path = "/wbsso", method = RequestMethod.POST)
    public String wbssoPost(Model model, HttpServletRequest request) {
        String _method = "[POST] wbsso";

        String relayState = request.getParameter(RELAY_STATE);
        String SAMLRequest = request.getParameter(SAML_REQUEST);
        String sigAlg = request.getParameter(SIGALG);
        String signature = request.getParameter(SIGNATURE);

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s: %s=%s", _method, RELAY_STATE, relayState));
            log.debug(String.format("%s: %s=%s", _method, SAML_REQUEST, SAMLRequest));
            log.debug(String.format("%s: %s=%s", _method, SIGALG, sigAlg));
            log.debug(String.format("%s: %s=%s", _method, SIGNATURE, signature));
        }

        // HTTP-POST binding means a base64 encoded SAML Request
        try {
            AuthnRequestDocument requestDoc = AuthnRequestDocument.Factory.parse(new StringReader(Utils.decodeBase64(request.getParameter(SAML_REQUEST))));

            String requestBinding;
            if (requestDoc.getAuthnRequest() != null &&
                    requestDoc.getAuthnRequest().getProtocolBinding() != null
                    && !"".equals(requestDoc.getAuthnRequest().getProtocolBinding())) {
                requestBinding = requestDoc.getAuthnRequest().getProtocolBinding();
            } else {
                requestBinding = SAML.SAML2_BINDING_HTTP_POST;
            }

            return wbsso(requestDoc, relayState, requestBinding, model, request);

        } catch (XmlException | IOException xe) {
            log.error("Richiesta non valida.", xe);
        }

        // TODO: costruire risposta SAML con errore generico
        return null;
    }

    /**
     * Riceve l'AuthnRequest tramite GET
     *
     * @param model   spring model
     * @param request {@link HttpServletRequest}
     * @return return della view di discovery
     */
    @RequestMapping(path = "/wbsso", method = RequestMethod.GET)
    public String wbssoGet(Model model, HttpServletRequest request) {
        String _method = "[GET] wbsso";

        String relayState = request.getParameter(RELAY_STATE);
        String SAMLRequest = request.getParameter(SAML_REQUEST);
        String sigAlg = request.getParameter(SIGALG);
        String signature = request.getParameter(SIGNATURE);

        if (log.isDebugEnabled()) {
            log.debug(String.format("%s: %s=%s", _method, RELAY_STATE, relayState));
            log.debug(String.format("%s: %s=%s", _method, SAML_REQUEST, SAMLRequest));
            log.debug(String.format("%s: %s=%s", _method, SIGALG, sigAlg));
            log.debug(String.format("%s: %s=%s", _method, SIGNATURE, signature));
        }

        try {
            byte[] decodedRequest = Utils.decodeBase64b(request.getParameter(SAML_REQUEST));
            AuthnRequestDocument requestDoc = AuthnRequestDocument.Factory.parse(Utils.inflate(decodedRequest, Utils.RFC1951_NO_WRAP));

            String requestBinding;
            if (requestDoc.getAuthnRequest() != null &&
                    requestDoc.getAuthnRequest().getProtocolBinding() != null
                    && !"".equals(requestDoc.getAuthnRequest().getProtocolBinding())) {
                requestBinding = requestDoc.getAuthnRequest().getProtocolBinding();
            } else {
                requestBinding = SAML.SAML2_BINDING_HTTP_REDIRECT;
            }

            return wbsso(requestDoc, relayState, requestBinding, model, request);

        } catch (XmlException | GuanxiException xe) {
            log.error("Richiesta non valida.", xe);
        }

        // TODO: costruire risposta SAML con errore generico
        return null;
    }

    private String wbsso(AuthnRequestDocument requestDoc, String relayState, String requestBinding,
                         Model model, HttpServletRequest request) {
        if (log.isTraceEnabled())
            RequestUtil.printAll(request);

        if (log.isDebugEnabled())
            log.debug(requestDoc.xmlText());

        String requestDocB64 = Utils.base64(requestDoc.xmlText().getBytes());

        String authnReqId = requestDoc.getAuthnRequest().getID();
        Date authnReqIssueInstant = requestDoc.getAuthnRequest().getIssueInstant().getTime();
        String authnReqIssuer = requestDoc.getAuthnRequest().getIssuer().getStringValue();

        authnReqIssuer = authnReqIssuer.replaceAll("\\s+", "");

        SAMLProxyPass samlProxyPass = samlProxyPassService.create(relayState, requestBinding, requestDocB64, authnReqId, authnReqIssueInstant, authnReqIssuer);

        try {
            Servizio servizio = servizioService.findByApplId(authnReqIssuer);

            int permissionLevel = servizio.getServizioSicurezza().getPermissionLevel();
            switch (permissionLevel) {
                case 11: // identifica il livello SPID 1
                    model.addAttribute(Keys.AUTHN_CONTEXT_CLASS_REF, Keys.AUTHN_CONTEXT_CLASS_REF_SPIDL1);
                    model.addAttribute(Keys.AUTHN_CONTEXT_COMPARISON, Keys.AUTHN_CONTEXT_COMPARISON_EXACT);
                    break;
                case 12: // identifica il livello SPID 2
                    model.addAttribute(Keys.AUTHN_CONTEXT_CLASS_REF, Keys.AUTHN_CONTEXT_CLASS_REF_SPIDL2);
                    model.addAttribute(Keys.AUTHN_CONTEXT_COMPARISON, Keys.AUTHN_CONTEXT_COMPARISON_EXACT);
                    model.addAttribute(Keys.FORCE_AUTHN, true);
                    break;
                default:
                    break;
            }

            EntityFarm farm = (EntityFarm) request.getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_ENGINE_ENTITY_FARM);
            if (farm == null) {
                throw new ResourceNotFound("EntityFarm non caricata nel sistema.");
            }

            EntityManager manager = farm.getEntityManagerForID(authnReqIssuer);

            if (manager == null) {
                throw new ResourceNotFound(String.format("Metadata non presenti per l'entityID %s", authnReqIssuer));
            }

        } catch (ResourceNotFound rnf) {
            String _msg = String.format("Servizio %s non registrato sul sistema o metadati non presenti.", authnReqIssuer);
            log.warn(_msg, rnf);
            model.addAttribute("message", _msg);
            return "/messages/errors";
        }

        // url return post login
        model.addAttribute(Keys.TARGET, String.format("%s?rs=%s", backurl, Utils.base64(samlProxyPass.getId().toString().getBytes())));
        // shibboleth Login servlet
        model.addAttribute(Keys.SHIB_SSO_LOGIN, shibSSOLogin);
        // default SPID IdP
        model.addAttribute(Keys.ENTITY_ID_INFOCERT, entityidInfocert);
        model.addAttribute(Keys.ENTITY_ID_POSTE, entityidPoste);
        model.addAttribute(Keys.ENTITY_ID_TELECOM, entityidTelecom);

        if (idpFile.exists()) {
            log.debug("Resource: " + idpFile.getFilename());

            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Idps.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Idps idps = (Idps) jaxbUnmarshaller.unmarshal(idpFile.getFile());

                log.debug(String.format("Numero di Idp Custom caricati: %d", idps.size()));

                model.addAttribute(Keys.IDPS, idps);

            } catch (Exception ex) {
                log.warn("Non sono riuscito a caricare le configurazioni degli IdP custom.", ex);
            }

        } else {
            log.warn(String.format("File [%s] contenente elenco IdP custom non presente.", idpFile.getFilename()));
        }

        return "/discovery";
    }

    /**
     * Riceve la richiesta di logout ed effettua una chiamata all'Handler Shibboleth opportuno
     *
     * @param request {@link HttpServletRequest}
     * @return logout view
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest request) {
        logoutComponent.logout(shibLogoutUrl, request);
        return "/logout";
    }

    /**
     * Pagina di utilita' che mostra la sessione attiva su Shibboleth
     *
     * @return session view
     */
    @RequestMapping(path = "/session", method = RequestMethod.GET)
    public String session() {
        return "/session";
    }

    /**
     * Esempio di JSON restituito da Shibboleth (va configurato sull'Handler Session il contentType application/json)
     *
     * @param request {@link HttpServletRequest}
     * @return sample json
     */
    @RequestMapping(path = "/test", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String test(HttpServletRequest request) {
        RequestUtil.printHeader(request);
        return "{ \"expiration\": 479, \"client_address\": \"127.0.0.1\", \"protocol\": \"urn:oasis:names:tc:SAML:2.0:protocol\", \"identity_provider\": \"GUANXI-1944500581\", \"authn_instant\": \"2016-05-26T07:31:05Z\", \"authncontext_class\": \"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport\", \"attributes\": [ { \"name\": \"shib-firstname\", \"values\": [ \"MARIO\" ] }, { \"name\": \"shib-fiscalcode\", \"values\": [ \"QQQWWW80A01A123A\" ] }, { \"name\": \"shib-lastname\", \"values\": [ \"ROSSI\" ] } ] }";
    }

}
