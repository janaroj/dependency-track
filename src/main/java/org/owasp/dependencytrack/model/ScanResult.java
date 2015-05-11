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

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Entity
@Table(name = "SCANRESULT")
@Data
public class ScanResult {

    /**
     * The unique identifier of the persisted object.
     */
    @Id
    @Column(name = "ID", unique = true)
    @GeneratedValue
    private Integer id;

    /**
     * The date of the scan
     */
    @Temporal(TemporalType.DATE)
    @Column(name = "SCANDATE")
    private Date scanDate;

    /**
     * The parent application version.
     */
    @ManyToOne
    @JoinColumn(name = "LIBRARYVERSIONID", nullable = false)
    private LibraryVersion libraryVersion;

    /**
     * The vulnerability recorded in this scan.
     */
    @ManyToOne
    @JoinColumn(name = "VULNERABILITYID", nullable = false)
    private Vulnerability vulnerability;

}
