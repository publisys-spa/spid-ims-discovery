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

package it.publisys.ims.discovery.component;

import it.publisys.ims.discovery.dto.Entity;
import org.guanxi.common.definitions.Guanxi;
import org.guanxi.common.entity.EntityFarm;
import org.guanxi.common.entity.EntityManager;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>18/02/16</pre>
 */
@Component
public class EntityComponent implements ServletContextAware {

    //
    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * Restituisce l'elenco dei Metadati
     *
     * @return entities
     */
    @SuppressWarnings("unchecked")
    public List<Entity> entities() {
        EntityFarm farm = (EntityFarm) servletContext.getAttribute(Guanxi.CONTEXT_ATTR_ENGINE_ENTITY_FARM);

        List<Entity> entities = new ArrayList<>();

        if (farm != null) {
            Set<Map.Entry<String, EntityManager>> entrySet = farm.getEntityManagers().entrySet();

            entrySet.stream()
                    .forEach(entry ->
                            {
                                String key = entry.getKey();
                                EntityManager value = entry.getValue();

                                String filename = new File(key).getName();

                                Arrays.stream(value.getEntityIDs()).forEach(e ->
                                        entities.add(new Entity(filename, e))
                                );
                            }
                    );
        }

        return entities;
    }

}
