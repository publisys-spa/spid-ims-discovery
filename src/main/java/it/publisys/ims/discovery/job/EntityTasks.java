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

package it.publisys.ims.discovery.job;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.xmlbeans.XmlException;
import org.guanxi.common.GuanxiException;
import org.guanxi.common.Utils;
import org.guanxi.common.definitions.Guanxi;
import org.guanxi.common.entity.EntityFarm;
import org.guanxi.common.entity.EntityManager;
import org.guanxi.common.entity.impl.GuanxiEntityFarmImpl;
import org.guanxi.common.entity.impl.GuanxiEntityManagerImpl;
import org.guanxi.common.metadata.Metadata;
import org.guanxi.xal.saml_2_0.metadata.EntitiesDescriptorDocument;
import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorDocument;
import org.guanxi.xal.saml_2_0.metadata.EntityDescriptorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Scandisce la cartella dei metadati e ne carica il contenuto
 *
 * @author mcolucci
 * @version 1.0
 * @since <pre>11/02/16</pre>
 */
@Component
public class EntityTasks implements ServletContextAware {

    private static final Logger log = LoggerFactory.getLogger(EntityTasks.class);

    private static final String METADATA_DIR = "metadata";
    private static final String GUARDS_DIR = "guards";
    //
    private static final String PROTECTEDAPP_GUARD_XML = "protectedapp-guard.xml";
    private static final String PROTECTEDAPP_GUARD_CACHED_XML = "protectedapp-guard-cached-metadata.xml";

    //
    private ServletContext servletContext;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Scheduled(fixedRate = 600000, initialDelay = 5000)
    public void reloadEntities() {
        log.debug("Reload Entities");

        log.debug("ServletContext: " + servletContext);

        try {
            Resource resourceDir = new ClassPathResource(METADATA_DIR + "/" + GUARDS_DIR);

            if (!resourceDir.exists()) {
                throw new FileNotFoundException(String.format("Directory dei Medatada non presente. [%s]", resourceDir));
            }

            List<File> files = loadMetadataXml(resourceDir.getFile());
            EntitiesDescriptorDocument entitiesDescriptorDocument = storeEntitiesFile(files);

            EntityDescriptorType[] entityDescriptorTypes = loadAndCacheEntities(entitiesDescriptorDocument);
            loadEntities(entityDescriptorTypes);

        } catch (FileNotFoundException fnfe) {
            log.warn(fnfe.getMessage(), fnfe);
        } catch (IOException ioe) {
            log.warn("Caricamento Metadata non riuscito.", ioe);
        }

    }

    private void loadEntities(EntityDescriptorType[] entityDescriptorTypes) {
        Resource resource = new ClassPathResource(METADATA_DIR + "/" + PROTECTEDAPP_GUARD_XML);

        try {
            final EntityManager manager = loadEntityManager(Guanxi.CONTEXT_ATTR_IDP_ENTITY_FARM, resource.getFile().getAbsolutePath());

            // Store the new entity IDs for cleaning out old ones later
            List<String> newEntityIDs = new ArrayList<>();

            Arrays.stream(entityDescriptorTypes)
                    .filter(entityDescriptor -> entityDescriptor.getSPSSODescriptorArray().length > 0)
                    .forEach(entityDescriptor -> {
                        log.info("Loading SP metadata for : " + entityDescriptor.getEntityID());
                        try {
                            Metadata metadataHandler = manager.createNewEntityHandler();
                            metadataHandler.setPrivateData(entityDescriptor);

                            manager.addMetadata(metadataHandler);

                            newEntityIDs.add(entityDescriptor.getEntityID());
                        } catch (GuanxiException ge) {
                            log.warn(String.format("Non sono riuscito a caricari i metadati del Service Provider %s", entityDescriptor.getEntityID()), ge);
                        }
                    });

            // Remove expired entities from the manager
            String[] oldEntityIDs = manager.getEntityIDs();
            for (String oldEntityID : oldEntityIDs) {
                if (!newEntityIDs.contains(oldEntityID)) {
                    manager.removeMetadata(oldEntityID);
                }
            }
        } catch (Exception ge) {
            log.error("Could not get an entity handler from the metadata manager", ge);
        }
    }

    private EntityManager loadEntityManager(String contextKey, String metadataURL) {
        EntityFarm farm = (EntityFarm) servletContext.getAttribute(contextKey);
        if (farm == null) {
            farm = new GuanxiEntityFarmImpl();

            GuanxiEntityManagerImpl entityManager = new GuanxiEntityManagerImpl();
            entityManager.init();
            entityManager.setEntityHandlerClass("org.guanxi.common.metadata.impl.GuanxiSAML2MetadataImpl");
            //entityManager.setTrustEngine(new org.guanxi.idp.trust.impl.IdPTrustEngineImpl);

            Map<String, EntityManager> entityManagers = new HashMap<>();
            entityManagers.put(metadataURL, entityManager);

            farm.setEntityManagers(entityManagers);

            servletContext.setAttribute(Guanxi.CONTEXT_ATTR_ENGINE_ENTITY_FARM, farm);
        }
        return farm.getEntityManagerForSource(metadataURL);
    }


    private EntitiesDescriptorDocument storeEntitiesFile(List<File> files) {
        Resource resource = new ClassPathResource(METADATA_DIR + "/" + PROTECTEDAPP_GUARD_XML);

        EntitiesDescriptorDocument entities = EntitiesDescriptorDocument.Factory.newInstance();

        EntityDescriptorType[] arrayEntities = new EntityDescriptorType[files.size()];

        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);

            log.debug(String.format("Metadata: %s", f.getAbsolutePath()));

            try {
                EntityDescriptorDocument singolo = EntityDescriptorDocument.Factory.parse(f);
                arrayEntities[i] = singolo.getEntityDescriptor();
            } catch (IOException ioe) {
                log.error(String.format("Errore di lettura del file %s", f.getAbsolutePath()), ioe);
            } catch (XmlException xmle) {
                log.error(String.format("Errore durante il parsing del file %s", f.getAbsolutePath()), xmle);
            }

        }

        entities.addNewEntitiesDescriptor().setEntityDescriptorArray(arrayEntities);

        // scrivo il nuovo file corrispondente alle entity ottenute
        try {
            Utils.writeSAML2MetadataToDisk(entities, resource.getFile().getAbsolutePath());
        } catch (Exception ex) {
            log.error(String.format("Errore durante la scrittura del file '%s' su disco", PROTECTEDAPP_GUARD_XML), ex);
        }

        return entities;

    }

    private EntityDescriptorType[] loadAndCacheEntities(EntitiesDescriptorDocument entitiesDescriptorDocument) {
        Resource resourceCache = new ClassPathResource(METADATA_DIR + "/" + PROTECTEDAPP_GUARD_CACHED_XML);

        EntityDescriptorType[] entityDescriptors = entitiesDescriptorDocument.getEntitiesDescriptor().getEntityDescriptorArray();

        // Cache the metadata locally
        try {
            Utils.writeSAML2MetadataToDisk(entitiesDescriptorDocument, resourceCache.getFile().getAbsolutePath());
        } catch (Exception ex) {
            log.error(String.format("Errore durante la scrittura del file di cache '%s' su disco", PROTECTEDAPP_GUARD_CACHED_XML), ex);
        }

        return entityDescriptors;
    }

    private List<File> loadMetadataXml(File dir) {
        Collection<File> filesAndDirs =
                FileUtils.listFilesAndDirs(dir,
                        FileFilterUtils.suffixFileFilter(".xml"),
                        FileFilterUtils.suffixFileFilter("-guard")
                );

        return filesAndDirs.stream().filter(File::isFile).collect(Collectors.toList());
    }


}
