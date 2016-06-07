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

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>11/02/16</pre>
 */
public interface Keys {

    String IDPS = "idps";
    //
    String SHIB_ATTR_PREFIX = "shib-";
    //
    String SHIB_SSO_LOGIN = "shibsso";
    //
    String AUTHN_CONTEXT_CLASS_REF = "authnContextClassRef";
    //
    String AUTHN_CONTEXT_CLASS_REF_SPIDL1 = "urn:oasis:names:tc:SAML:2.0:ac:classes:SpidL1";
    String AUTHN_CONTEXT_CLASS_REF_SPIDL2 = "urn:oasis:names:tc:SAML:2.0:ac:classes:SpidL2";
    //
    String ENTITY_ID_INFOCERT = "eidInfocert";
    String ENTITY_ID_POSTE = "eidPoste";
    String ENTITY_ID_TELECOM = "eidTelecom";
    //
    String TARGET = "target"; // ${backurl}
    String FORCE_AUTHN = "forceAuthn"; // true / false
    //
    String AUTHN_CONTEXT_COMPARISON = "authnContextComparison";
    String AUTHN_CONTEXT_COMPARISON_EXACT = "exact";

}
