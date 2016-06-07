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

package it.publisys.ims.discovery.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mcolucci
 * @version 1.0
 * @since <pre>04/02/16</pre>
 */
public class ShibUser {

    private final List<ShibAttribute> attributes;

    public ShibUser() {
        this.attributes = new ArrayList<>();
    }

    public List<ShibAttribute> getAttributesByPrefix(String prefix) {
        return attributes.stream()
                .filter(a -> a.getKey().startsWith(prefix) || a.getKey().equals("Shib-AuthnContext-Class"))
                .filter(a -> a.getValue() != null && !"".equals(a.getValue()))
                .collect(Collectors.toList());
    }

    public void addAttribute(String key, Object val) {
        ShibAttribute sa = new ShibAttribute();
        sa.setKey(key);
        sa.setValue(val);
        attributes.add(sa);
    }
}
