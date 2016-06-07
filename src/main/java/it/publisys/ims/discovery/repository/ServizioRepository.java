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

package it.publisys.ims.discovery.repository;

import it.publisys.ims.discovery.domain.Servizio;
import it.publisys.ims.discovery.domain.ServizioRaggiungibilita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>08/02/16</pre>
 */
@Repository
public interface ServizioRepository extends JpaRepository<Servizio, Long> {

    Optional<Servizio> findByServizioRaggiungibilita(ServizioRaggiungibilita sr);

}
