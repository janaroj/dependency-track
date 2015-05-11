/*
 * This file is part of Dependency-Track.
 *
 * Dependency-Track is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Dependency-Track is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Dependency-Track. If not, see http://www.gnu.org/licenses/.
 *
 * Copyright (c) Axway. All Rights Reserved.
 */

package org.owasp.dependencytrack.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "APPLICATION")
@Data
@EqualsAndHashCode(exclude = {"versions"})
public final class Application implements Cloneable {

    /**
     * The unique identifier of the persisted object.
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    /**
     * The name of the application.
     */
    @Column(name = "NAME")
    @OrderBy
    private String name;

    /**
     * The version of the applications.
     */
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "application")
    @OrderBy("version")
    private Set<ApplicationVersion> versions;

    /**
     * Clones this specific object (minus the objects id).
     * @return a New object
     */
    @Override
    public Object clone() {
        final Application obj = new Application();
        obj.setName(this.name);
        return obj;
    }

}
