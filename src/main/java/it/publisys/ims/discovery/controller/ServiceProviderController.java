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
import it.publisys.ims.discovery.domain.SAMLProxyPass;
import it.publisys.ims.discovery.dto.ShibAttribute;
import it.publisys.ims.discovery.dto.ShibUser;
import it.publisys.ims.discovery.error.ResourceNotFound;
import it.publisys.ims.discovery.service.SAMLProxyPassService;
import it.publisys.ims.discovery.util.RequestUtil;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.guanxi.common.GuanxiException;
import org.guanxi.common.Utils;
import org.guanxi.common.definitions.EduPersonOID;
import org.guanxi.common.definitions.Guanxi;
import org.guanxi.common.definitions.SAML;
import org.guanxi.common.entity.EntityFarm;
import org.guanxi.common.entity.EntityManager;
import org.guanxi.common.metadata.SPMetadata;
import org.guanxi.common.security.SecUtils;
import org.guanxi.common.security.SecUtilsConfig;
import org.guanxi.xal.saml_2_0.assertion.*;
import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
import org.guanxi.xal.saml_2_0.metadata.IndexedEndpointType;
import org.guanxi.xal.saml_2_0.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.w3c.dom.Document;
import org.w3c.dom.Text;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>04/02/16</pre>
 */
@Controller
@RequestMapping("/sp")
public class ServiceProviderController {

    private static final Logger log = LoggerFactory.getLogger(ServiceProviderController.class);

    @Value("${idp.discovery.issuer}")
    private String idpDiscoveryIssuer;

    @Autowired
    private SAMLProxyPassService samlProxyPassService;

    @Autowired
    private SecUtilsConfig secUtilsConfig;

    //
    private static final Map<String, String> saml2Namespaces;
    private static final XmlOptions xmlOptions;
    //
    private static final Map<String, String> attributeMappings;
    private static final List<String> excludeAttributeMappings =
            Arrays.asList("shib-cookie-name", "shib-session-index", "shib-session-index",
                    "shib-session-index", "shib-session-index", "shib-authncontext-decl",
                    "shib-assertion-count", "shib-application-id", "shib-session-id",
                    "shib-authentication-method", "shib-authentication-instant");

    static {

        saml2Namespaces = new HashMap<>();
        saml2Namespaces.put(SAML.NS_SAML_20_PROTOCOL, SAML.NS_PREFIX_SAML_20_PROTOCOL);
        saml2Namespaces.put(SAML.NS_SAML_20_ASSERTION, SAML.NS_PREFIX_SAML_20_ASSERTION);

        xmlOptions = new XmlOptions();
        xmlOptions.setSaveSuggestedPrefixes(saml2Namespaces);
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSavePrettyPrintIndent(2);
        xmlOptions.setUseDefaultNamespace();
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setSaveNamespacesFirst();

        attributeMappings = new HashMap<>();
        //
        attributeMappings.put("shib-identity-provider", "identity-provider");
        //
        // SPID
        attributeMappings.put("shib-spidCode", "spidCode");
        attributeMappings.put("shib-familyName", "familyName");
        attributeMappings.put("shib-placeOfBirth", "placeOfBirth");
        attributeMappings.put("shib-countyOfBirth", "countyOfBirth");
        attributeMappings.put("shib-dateOfBirth", "dateOfBirth");
        attributeMappings.put("shib-companyName", "companyName");
        attributeMappings.put("shib-registeredOffice", "registeredOffice");
        attributeMappings.put("shib-fiscalNumber", "fiscalNumber");
        attributeMappings.put("shib-ivaCode", "ivaCode");
        attributeMappings.put("shib-idCard", "idCard");
        attributeMappings.put("shib-mobilePhone", "mobilePhone");
        attributeMappings.put("shib-expirationDate", "expirationDate");
        attributeMappings.put("shib-digitalAddress", "digitalAddress");
//        attributeMappings.put("shib-authncontext-class", "validate");
        attributeMappings.put("Shib-AuthnContext-Class", "validate");
        // SPID
        attributeMappings.put("shib-spidcode", "spidCode");
        attributeMappings.put("shib-name", "name");
        attributeMappings.put("shib-familyname", "familyName");
        attributeMappings.put("shib-placeofbirth", "placeOfBirth");
        attributeMappings.put("shib-countyofbirth", "countyOfBirth");
        attributeMappings.put("shib-dateofbirth", "dateOfBirth");
        attributeMappings.put("shib-gender", "gender");
        attributeMappings.put("shib-companyname", "companyName");
        attributeMappings.put("shib-registeredoffice", "registeredOffice");
        attributeMappings.put("shib-fiscalnumber", "fiscalNumber");
        attributeMappings.put("shib-ivacode", "ivaCode");
        attributeMappings.put("shib-idcard", "idCard");
        attributeMappings.put("shib-mobilephone", "mobilePhone");
        attributeMappings.put("shib-email", "email");
        attributeMappings.put("shib-address", "address");
        attributeMappings.put("shib-expirationdate", "expirationDate");
        attributeMappings.put("shib-digitaladdress", "digitalAddress");
        //
        attributeMappings.put("shib-codicefiscale", "fiscalNumber");
        attributeMappings.put("shib-cognome", "familyName");
        attributeMappings.put("shib-nome", "name");
        attributeMappings.put("shib-mail", "email");
        attributeMappings.put("shib-indirizzoresidenza", "address");
        attributeMappings.put("shib-comuneresidenza", "placeOfResidence");
        attributeMappings.put("shib-userid", "uid");
        attributeMappings.put("shib-username", "username");
        attributeMappings.put("shib-useridentified", "userIdentified");
        attributeMappings.put("shib-userenabled", "userEnabled");
        attributeMappings.put("shib-pec", "digitalAddress");
        attributeMappings.put("shib-mobile", "mobilePhone");
        attributeMappings.put("shib-homephone", "homePhone");
        attributeMappings.put("shib-userIdentified", "validate");
        //
        attributeMappings.put("shib-uid", "uid");
        //attributeMappings.put("shib-pec", "digitalAddress");
        //attributeMappings.put("shib-email", "email");
        //attributeMappings.put("shib-username", "username");
        attributeMappings.put("shib-fiscalcode", "fiscalNumber");
        attributeMappings.put("shib-lastname", "familyName");
        attributeMappings.put("shib-firstname", "name");
        attributeMappings.put("shib-sex", "gender");
        attributeMappings.put("shib-birthdate", "dateOfBirth");
        attributeMappings.put("shib-birthplace", "placeOfBirth");
        attributeMappings.put("shib-code-istat-birthplace", "codeIstatPlaceOfBirth");
        attributeMappings.put("shib-cap-birthplace", "capPlaceOfBirth");
        attributeMappings.put("shib-provincia-birthplace", "provinciaPlaceOfBirth");
        attributeMappings.put("shib-provincia-sigla-birthplace", "provinciaSiglaPlaceOfBirth");
        attributeMappings.put("shib-stato-birthplace", "countyOfBirth");
        attributeMappings.put("shib-code-istat-residencecity", "codeIstatPlaceOfResidence");
        attributeMappings.put("shib-cap-residencecity", "capPlaceOfResidence");
        attributeMappings.put("shib-provincia-residencecity", "provinciaPlaceOfResidence");
        attributeMappings.put("shib-provincia-sigla-residencecity", "provinciaSiglaPlaceOfResidence");
        attributeMappings.put("shib-stato-residencecity", "countyOfResidence");
        attributeMappings.put("shib-residencecity", "placeOfResidence");
        attributeMappings.put("shib-residenceaddress", "addressOfResidence");
        attributeMappings.put("shib-telephonenumber", "homePhone");
        attributeMappings.put("shib-cellularnumber", "mobilePhone");
        attributeMappings.put("shib-lastupdate", "lastupdate");
        attributeMappings.put("shib-CATEGORY", "category");
        attributeMappings.put("shib-validate", "validate");
    }

    /**
     * Path che gestisce il ritorno da Shibboleth, geneera la Response e la inoltra al Service Provider di partenza
     *
     * @param rs      identificativo della richiesta di partenza
     * @param request {@link HttpServletRequest}
     * @param model   spring model
     * @return in base al binding di partenza effettua una redirect o una post verso il Service Provider di partenza
     */
    @RequestMapping
    public String login(
            @RequestParam(value = "rs") String rs,
            HttpServletRequest request, Model model) {

        if (log.isDebugEnabled())
            RequestUtil.printHeader(request);

        ShibUser su = new ShibUser();
        readShibAttribute(su, request);

        model.addAttribute("shibuser", su);

        try {
            String requestId = Utils.decodeBase64(rs);
            SAMLProxyPass samlProxyPass = samlProxyPassService.findById(Long.parseLong(requestId));

            String requestBinding = samlProxyPass.getRequestBinding();

            String requestDocB64 = samlProxyPass.getRequestDoc();
            String requestDocStr = Utils.decodeBase64(requestDocB64);

            AuthnRequestDocument requestDoc = AuthnRequestDocument.Factory.parse(new StringReader(requestDocStr));
            String[] acs = getAcsURL(samlProxyPass.getAuthnReqIssuer(),
                    samlProxyPass.getRequestBinding(),
                    requestDoc, request);

            ResponseDocument responseDocument = buildResponseDocument(samlProxyPass.getAuthnReqId(), samlProxyPass.getAuthnReqIssuer(),
                    acs[0], acs[1], su, request);

            StringWriter sw = new StringWriter();
            responseDocument.save(sw, xmlOptions);

            String swString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + sw.toString();

            if (log.isDebugEnabled()) {
                log.debug("=======================================================");
                log.debug("Response to SAML2 WBSSO request by " + requestId);
                log.debug("");
                log.debug(swString);
                log.debug("");
                log.debug("=======================================================");
            }

            // TODO: Sign Response in SAML2_BINDING_HTTP_POST
            Document signedDoc = null;
            if (requestBinding.equals(SAML.SAML2_BINDING_HTTP_POST)) {
                try {
                    // Need to use newDomNode to preserve namespace information
                    signedDoc = SecUtils.getInstance().saml2Sign(secUtilsConfig,
                            (Document) responseDocument.newDomNode(xmlOptions),
                            responseDocument.getResponse().getID());
                } catch (GuanxiException ge) {
                    log.error("Could not sign Response", ge);
                    // TODO: gestire errore
                }
            }

            String b64SAMLResponse = null;
            if (requestBinding.equals(SAML.SAML2_BINDING_HTTP_POST)) {
                b64SAMLResponse = Utils.base64(signedDoc);
            } else if (requestBinding.equals(SAML.SAML2_BINDING_HTTP_REDIRECT)) {
                String deflatedResponse = Utils.deflate(responseDocument.toString(), Utils.RFC1951_DEFAULT_COMPRESSION_LEVEL, Utils.RFC1951_NO_WRAP);
                b64SAMLResponse = Utils.base64(deflatedResponse.getBytes("UTF-8"));
                b64SAMLResponse = b64SAMLResponse.replaceAll(System.getProperty("line.separator"), "");
                b64SAMLResponse = URLEncoder.encode(b64SAMLResponse, "UTF-8");
            }

            // Salvo l'asserzione di risposta sul DB
            String responseDocB64 = Utils.base64(swString.getBytes());

            String respId = requestDoc.getAuthnRequest().getID();
            Date respIssueInstant = requestDoc.getAuthnRequest().getIssueInstant().getTime();
            String respIssuer = requestDoc.getAuthnRequest().getIssuer().getStringValue();

            respIssuer = respIssuer.replaceAll("\\s+", "");

            samlProxyPass = samlProxyPassService.update(samlProxyPass, responseDocB64, respId, respIssueInstant, respIssuer);

            if (requestBinding.equals(SAML.SAML2_BINDING_HTTP_POST)) {
                model.addAttribute("requestBinding", requestBinding);
                model.addAttribute("RelayState", samlProxyPass.getRelayState());
                model.addAttribute("wbsso_acs_endpoint", acs[0]);

                model.addAttribute("SAMLResponse", b64SAMLResponse);

                return "/binding-post";
            } else if (requestBinding.equals(SAML.SAML2_BINDING_HTTP_REDIRECT)) {
                String urlRedirect = acs[0] + "?SAMLResponse=" + b64SAMLResponse + "&RelayState=" + samlProxyPass.getRelayState();

                return "redirect:" + urlRedirect;
            }

        } catch (ResourceNotFound rnf) {
            log.warn(rnf.getMessage(), rnf);
            // TODO: gestire pagina di errore
        } catch (XmlException | IOException xe) {
            log.error("Richiesta non valida.", xe);
        }

        return "/user";
    }

    private ResponseDocument buildResponseDocument(String requestID, String spEntityID,
                                                   String acsURL, String binding,
                                                   ShibUser su,
                                                   HttpServletRequest request) {

        int assertionTimeLimit = 60;
        Calendar calendar = Calendar.getInstance();

        ResponseDocument responseDoc = ResponseDocument.Factory.newInstance();

        ResponseType wbssoResponse = responseDoc.addNewResponse();
        wbssoResponse.setID(generateStringID());
        wbssoResponse.setVersion("2.0");
        wbssoResponse.setDestination(acsURL);
        wbssoResponse.setIssueInstant(calendar);
        wbssoResponse.setInResponseTo(requestID);
        Utils.zuluXmlObject(wbssoResponse, 0);

        // Response/Issuer
        NameIDType issuer = wbssoResponse.addNewIssuer();
        issuer.setFormat(SAML.URN_SAML2_NAMEID_FORMAT_ENTITY);
        issuer.setStringValue(idpDiscoveryIssuer);

        // Response/Status
        StatusDocument statusDoc = StatusDocument.Factory.newInstance();
        StatusType status = statusDoc.addNewStatus();
        StatusCodeType topLevelStatusCode = status.addNewStatusCode();
        topLevelStatusCode.setValue(SAML.SAML2_STATUS_SUCCESS);
        wbssoResponse.setStatus(status);

        // Response/Assertion
        AssertionDocument assertionDoc = AssertionDocument.Factory.newInstance();
        AssertionType assertion = assertionDoc.addNewAssertion();
        assertion.setID(generateStringID());
        assertion.setIssueInstant(calendar);
        assertion.setIssuer(issuer);
        assertion.setVersion("2.0");
        Utils.zuluXmlObject(assertion, 0);

        // Response/Assertion/Subject
        SubjectType subject = assertion.addNewSubject();
        SubjectConfirmationType subjectConfirmation = subject.addNewSubjectConfirmation();
        subjectConfirmation.setMethod(SAML.URN_SAML2_CONFIRMATION_METHOD_BEARER);
        SubjectConfirmationDataType subjectConfirmationData = subjectConfirmation.addNewSubjectConfirmationData();
        subjectConfirmationData.setInResponseTo(requestID);
        subjectConfirmationData.setNotOnOrAfter(calendar);
        subjectConfirmationData.setRecipient(acsURL);
        Utils.zuluXmlObject(subjectConfirmationData, assertionTimeLimit);

        // Response/Assertion/Conditions
        ConditionsType conditions = assertion.addNewConditions();
        conditions.setNotBefore(calendar);
        conditions.setNotOnOrAfter(calendar);
        AudienceRestrictionType audienceRestriction = conditions.addNewAudienceRestriction();
        audienceRestriction.addAudience(spEntityID);
        Utils.zuluXmlObject(conditions, assertionTimeLimit);

        // Response/Assertion/AuthnStatement
        AuthnStatementType authnStatement = assertion.addNewAuthnStatement();
        authnStatement.setAuthnInstant(calendar);
        authnStatement.addNewAuthnContext().setAuthnContextClassRef(SAML.URN_SAML2_PASSWORD_PROTECTED_TRANSPORT);
        authnStatement.setSessionIndex(request.getSession().getId());
        Utils.zuluXmlObject(authnStatement, 0);

        AttributeStatementDocument attributeStatementDocument = buildSAML2AttributeStatement(su);

        if (attributeStatementDocument != null) {
            // Response/Assertion/AttributeStatement
            assertion.setAttributeStatementArray(new AttributeStatementType[]{attributeStatementDocument.getAttributeStatement()});
        }

        responseDoc.getResponse().addNewAssertion();
        responseDoc.getResponse().setAssertionArray(0, assertionDoc.getAssertion());

        return responseDoc;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private AttributeStatementDocument buildSAML2AttributeStatement(ShibUser su) {

        AttributeStatementDocument attrStatementDoc = AttributeStatementDocument.Factory.newInstance();
        AttributeStatementType attrStatement = attrStatementDoc.addNewAttributeStatement();

        boolean hasAttrs = false;

        List<ShibAttribute> attributesByPrefix = su.getAttributesByPrefix(Keys.SHIB_ATTR_PREFIX)
                .stream().filter(sa -> !excludeAttributeMappings.contains(sa.getKey()))
                .collect(Collectors.toList());

        if (!attributesByPrefix.isEmpty()) hasAttrs = true;

        attributesByPrefix.forEach(
                a -> {
                    String mappedName = attributeMappings.getOrDefault(a.getKey(), a.getKey());
                    String value = a.getValue() != null ? a.getValue() + "" : "";
                    if ("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport".equals(value)) {
                        // se il CF non arriva da SPID non devo ripulirlo del prefisso TINIT-
                    } else {
                        AttributeType attribute = attrStatement.addNewAttribute();
                        attribute.setName(EduPersonOID.ATTRIBUTE_NAME_PREFIX + mappedName);
                        attribute.setNameFormat(SAML.SAML2_ATTRIBUTE_NAME_FORMAT_URI);
                        XmlObject attrValue = attribute.addNewAttributeValue();

                        if ("fiscalNumber".equals(mappedName)) {
                            // TINIT-FISCALNUMBER
                            value = value.replaceFirst("TINIT-", "");
                        }

                        Text valueNode = attrValue.getDomNode().getOwnerDocument().createTextNode(value);
                        attrValue.getDomNode().appendChild(valueNode);
                    }
                }
        );

        if (hasAttrs)
            return attrStatementDoc;
        else
            return null;
    }

    /**
     * Preleva l'acsURL e il binding dall'AuthnRequest o, se non presente, dai metadati del Service Provider
     *
     * @param entityID       entity ID
     * @param requestBinding binding della richiesta
     * @param requestDoc     AuthnRequest di partenza
     * @param request        HttpServletRequest
     * @return 0=AssertionConsumerServiceURL - 1=ProtocolBinding
     * @throws ResourceNotFound
     */
    private String[] getAcsURL(String entityID, String requestBinding, AuthnRequestDocument requestDoc, HttpServletRequest request)
            throws ResourceNotFound {
        String[] acs = new String[2];

        if ((requestDoc.getAuthnRequest().getAssertionConsumerServiceURL() != null) &&
                (!requestDoc.getAuthnRequest().getAssertionConsumerServiceURL().equals("")) &&
                (requestDoc.getAuthnRequest().getProtocolBinding() != null) &&
                (!requestDoc.getAuthnRequest().getProtocolBinding().equals(""))) {

            acs[0] = requestDoc.getAuthnRequest().getAssertionConsumerServiceURL();
            acs[1] = requestDoc.getAuthnRequest().getProtocolBinding();

        } else {
            EntityFarm farm = (EntityFarm) request.getServletContext().getAttribute(Guanxi.CONTEXT_ATTR_ENGINE_ENTITY_FARM);
            if (farm == null) {
                throw new ResourceNotFound("EntityFarm non caricata nel sistema.");
            }

            // ... or if the information is missing, try to work it out from the metadata
            EntityManager manager = farm.getEntityManagerForID(entityID);
            if (manager == null) {
                throw new ResourceNotFound(String.format("Metadata non presenti per l'entityID %s", entityID));
            }
            SPMetadata metadata = (SPMetadata) manager.getMetadata(entityID);

            String acsURL = null;
            EntityDescriptorType saml2Metadata = (EntityDescriptorType) metadata.getPrivateData();
            IndexedEndpointType[] acss = saml2Metadata.getSPSSODescriptorArray(0).getAssertionConsumerServiceArray();
            String defaultAcsURL = null;
            for (IndexedEndpointType iet : acss) {
                if (iet.getBinding().equalsIgnoreCase(requestBinding)) {
                    acsURL = iet.getLocation();
                }
                // Find the default binding endpoint in case we need it
                if (iet.getBinding().equalsIgnoreCase(SAML.SAML2_BINDING_HTTP_POST)) {
                    defaultAcsURL = iet.getLocation();
                }
            }

            // If there's no Response endpoint binding to match the binding used for the Request, use the default
            if (acsURL == null) {
                acs[0] = defaultAcsURL;
                acs[1] = SAML.SAML2_BINDING_HTTP_POST;
            } else {
                acs[0] = acsURL;
                acs[1] = requestBinding;
            }
        }

        return acs;
    }

    /**
     * Generazione random ID
     *
     * @return generated id
     */
    private String generateStringID() {
        SecureRandom random = new SecureRandom();
        return "gx" + new BigInteger(130, random).toString(32);
    }

    /**
     * Lettura dei dati dell'header della richiesta da cui verranno estratti gli attibuti inoltrati da ShibbolethSP
     *
     * @param su      wrapper per utente shibboleth
     * @param request HttpServletRequest
     */
    private void readShibAttribute(ShibUser su, HttpServletRequest request) {
        String name, value;

        Enumeration<String> e = request.getHeaderNames();
        while (e.hasMoreElements()) {
            name = e.nextElement();
            value = request.getHeader(name);

            su.addAttribute(name, value);
        }
    }

}
