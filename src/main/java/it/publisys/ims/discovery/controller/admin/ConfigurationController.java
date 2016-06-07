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

package it.publisys.ims.discovery.controller.admin;

import it.publisys.ims.discovery.dto.Entity;
import it.publisys.ims.discovery.service.admin.ConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>11/02/16</pre>
 */
@Controller
@RequestMapping("/admin/config")
public class ConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationController.class);

    private static final String ACTION_PARAM = "action";
    private static final String RELOAD_VALUE = "metadata-reload";

    @Autowired
    private ConfigurationService configurationService;

    @RequestMapping(method = RequestMethod.GET)
    public String goConfig(@RequestParam(value = ACTION_PARAM, defaultValue = "-", required = false) String action,
                           Model model, HttpServletRequest request) {

        boolean _forceRedirect = false;

        switch (action) {
            case RELOAD_VALUE:
                configurationService.forceReloadEntities();
                _forceRedirect = true;
                break;
            default:
                break;
        }

        return _forceRedirect ? "redirect:/admin/config" : "/admin/configuration";
    }

    @ModelAttribute("entities")
    public List<Entity> listMetadata() {
        return configurationService.listMetadata();
    }

}
