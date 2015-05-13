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

package org.owasp.dependencytrack.service;

import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.dependency.Identifier;
import org.owasp.dependencytrack.dao.LibraryVersionDao;
import org.owasp.dependencytrack.model.ApplicationVersion;
import org.owasp.dependencytrack.model.Library;
import org.owasp.dependencytrack.model.LibraryVendor;
import org.owasp.dependencytrack.model.LibraryVersion;
import org.owasp.dependencytrack.model.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class LibraryVersionService {

    @Autowired
    private LibraryVersionDao libraryVersionDao;


    @Transactional
    public List<LibraryVersion> getDependencies(ApplicationVersion version) {
        return libraryVersionDao.getDependencies(version);
    }

    @Transactional
    public void addDependency(int appversionid, int libversionid) {
        libraryVersionDao.addDependency(appversionid, libversionid);
    }

    @Transactional
    public void deleteDependency(int appversionid, int libversionid) {
        libraryVersionDao.deleteDependency(appversionid, libversionid);
    }

    /*
        Returns a List of all LibraryVendors available in the application along with all child objects
     */
    @Transactional
    public List<LibraryVendor> getLibraryHierarchy() {
        return libraryVersionDao.getLibraryHierarchy();
    }



    @Transactional
    public void updateLibrary(int vendorid, int licenseid, int libraryid,
                              int libraryversionid, String libraryname, String libraryversion,
                              String vendor, String license,  String language) {

        libraryVersionDao.updateLibrary(vendorid, licenseid, libraryid,
                libraryversionid, libraryname, libraryversion, vendor, license, language);
    }

    @Transactional
    public void removeLibrary(Integer id) {
        libraryVersionDao.removeLibrary(id);
    }

    @Transactional
    public List<License> listLicense(Integer id) {
        return libraryVersionDao.listLicense(id);
    }

    @Transactional
    public List<LibraryVersion> allLibrary() {
        return libraryVersionDao.allLibrary();
    }

    @Transactional
    public List<Library> uniqueLibrary() {
        return libraryVersionDao.uniqueLibrary();
    }

    @Transactional
    public List<License> uniqueLicense() {
        return libraryVersionDao.uniqueLicense();
    }

    @Transactional
    public List<LibraryVendor> uniqueVendor() {
        return libraryVersionDao.uniqueVendor();
    }

    @Transactional
    public List<String> uniqueLang() {
        return libraryVersionDao.uniqueLang();
    }

    @Transactional
    public List<String> uniqueVer() {
        return libraryVersionDao.uniqueVer();
    }

    @Transactional
    public Integer addLibraries(String libraryName, String libraryVersion, String vendor, String license, MultipartFile file, String language) {
        LibraryVersion library = libraryVersionDao.getLibrary(libraryName, libraryVersion, vendor);
        if (library == null) {
            return libraryVersionDao.addLibraries(libraryName, libraryVersion, vendor,  license,  file,  language);
        }
        return library.getId();
    }

    @Transactional
    public void uploadLicense(int licenseid, MultipartFile file, String editlicensename) {
        libraryVersionDao.uploadLicense(licenseid, file, editlicensename);
    }

    @Transactional
    public List<LibraryVersion> keywordSearchLibraries(String searchTerm) {
        return libraryVersionDao.keywordSearchLibraries(searchTerm);
    }
    
    @Transactional
    public void addDependenciesToApplication(List<Dependency> dependencies, int appVersionId) {
        int count = 0;
        for (Dependency dependency : dependencies) {
            if (dependency.getIdentifiers().size() > 0) {
                String[] identifierParts = getIdentifier(dependency).getValue().split(":");
                Integer libVersionId = addLibraries(identifierParts[1], identifierParts[2], identifierParts[0], "UNKNOWN", null, null);
                addDependency(appVersionId, libVersionId);
                count++;
            }
            else {
                log.warn("No identifiers found for " + dependency.getFileName());
            }
        }
        log.info("{}/{} dependencies added to application {}", count, dependencies.size(),appVersionId);
    }

    private Identifier getIdentifier(Dependency dependency) {
        Iterator<Identifier> iterator = dependency.getIdentifiers().iterator();
        Identifier identifier = iterator.next();
        try {
            String[] splitFileName = dependency.getFileName().split(" ");
            String lib = splitFileName[splitFileName.length - 1];
            String libName = lib.substring(0, lib.lastIndexOf("-"));
            String version = lib.substring(lib.lastIndexOf("-") + 1, lib.lastIndexOf("."));
            while (iterator.hasNext()) {
                Identifier temp = iterator.next();
                String[] parts = temp.getValue().split(":");
                if (parts.length == 3 && parts[1].equalsIgnoreCase(libName) && parts[2].equalsIgnoreCase(version) && !temp.getType().equals("cpe")) {
                    return temp;
                }
                if (identifier.getType().equals("cpe")) {
                    identifier = temp;
                }
            }
        }
        catch (Exception ex) {
            log.warn(ex.getMessage());
        }
        return identifier; //What to do when more than 1 identifier? Currently tries to get the one with right version and libName
    }

}
