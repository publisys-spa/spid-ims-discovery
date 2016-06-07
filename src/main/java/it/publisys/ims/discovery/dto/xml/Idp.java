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

package it.publisys.ims.discovery.dto.xml;

import lombok.Data;

import javax.xml.bind.annotation.*;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>31/05/16</pre>
 */
@XmlRootElement(name = "idp")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class Idp {

    @XmlAttribute(name = "alias", required = true)
    private String alias;
    @XmlElement(name = "entity-id", required = true)
    private String entityId;
    @XmlElement(name = "name", required = true)
    private String name;
    @XmlElement(name = "description", required = true)
    private String description;
    @XmlElement(name = "signup-url")
    private String signupUrl;
    @XmlElement(name = "header-image", required = true)
    private String headerImage;
    @XmlElement(name = "label-image", required = true)
    private String labelImage;
    @XmlElement(name = "button-css-class")
    private String buttonCssClass;

}
