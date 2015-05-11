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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "USERS")
@Data
public final class User {

    /**
     * The unique identifier of the persisted object.
     */
    @Id
    @Column(name = "ID")
    @GeneratedValue
    private Integer id;

    /**
     * The name users use to logon.
     */
    @Column(name = "USERNAME", unique = true)
    private String username;

    /**
     * The password associated with the username.
     */
    @Column(name = "PASSWORD")
    private String password;

    /**
     * Admin validates a registered user and gives him access to the website
     */
    @Column(name = "CHECKVALID")
    private boolean checkvalid;  //todo delete this field

    /**
     * Specifies if the username is a pointer to an external LDAP entity
     */
    @Column(name = "ISLDAP")
    private boolean isLdap;

    /**
     * The license the library is licensed under.
     */
    @ManyToOne
    @JoinColumn(name = "ROLEID")
    @OrderBy
    private Roles roles;

    public void setIsLdap(boolean isLdap) {
        this.isLdap = isLdap;
    }
    
}
