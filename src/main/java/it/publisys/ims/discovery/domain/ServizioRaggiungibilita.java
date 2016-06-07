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

package it.publisys.ims.discovery.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>08/02/16</pre>
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "services_reachability")
@Data
public class ServizioRaggiungibilita extends BaseBean {

    @NotEmpty(message = "Campo obbligatorio")
    @Column(name = "appId")
    private String appId;
    @NotEmpty(message = "Campo obbligatorio")
    @Column(name = "protocol")
    private String protocol = "http";
    @Column(name = "port")
    private int port = 80;
    @NotEmpty(message = "Campo obbligatorio")
    @Column(name = "uri")
    private String uri;
    @NotEmpty(message = "Campo obbligatorio")
    @Column(name = "address")
    private String address;
    @Column(name = "context")
    private String context;
    @Column(name = "mainPage")
    private String mainPage;

}
