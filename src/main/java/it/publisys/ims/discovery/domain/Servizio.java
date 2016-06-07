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

import javax.persistence.*;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>08/02/16</pre>
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "services")
@Data
public class Servizio extends BaseBean {

    @Column(name = "vers")
    private String versione;

    @Column(name = "name")
    private String name;
    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "state")
    private int state = 0; // default OFF-LINE

    @Column(name = "state_message")
    private String stateMessage;

    @Lob
    @Column(name = "img")
    private byte[] img;

    @JoinColumn(name = "id_security", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ServizioSicurezza servizioSicurezza = new ServizioSicurezza();

    @JoinColumn(name = "id_reachable", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ServizioRaggiungibilita servizioRaggiungibilita = new ServizioRaggiungibilita();

}
