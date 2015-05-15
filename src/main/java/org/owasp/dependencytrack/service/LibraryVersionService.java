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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.dependency.Identifier;
import org.owasp.dependencytrack.dao.LibraryVersionDao;
import org.owasp.dependencytrack.model.ApplicationDependency;
import org.owasp.dependencytrack.model.ApplicationVersion;
import org.owasp.dependencytrack.model.FileData;
import org.owasp.dependencytrack.model.Library;
import org.owasp.dependencytrack.model.LibraryVendor;
import org.owasp.dependencytrack.model.LibraryVersion;
import org.owasp.dependencytrack.model.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
        ApplicationDependency applicationDependency = libraryVersionDao.getApplicationDependency(appversionid, libversionid);
        if (applicationDependency == null) {
            libraryVersionDao.addDependency(appversionid, libversionid);
        }
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
    public void updateLibrary(int vendorid, int licenseid, int libraryid, int libraryversionid, String libraryname, String libraryversion, String vendor, String license,  String language) {
        libraryVersionDao.updateLibrary(vendorid, licenseid, libraryid, libraryversionid, libraryname, libraryversion, vendor, license, language);
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
                FileData fileData = getIdentifiedLibrary(dependency);
                Integer libVersionId = addLibraries(fileData.getName(), fileData.getVersion(), fileData.getVendor(), "UNKNOWN", null, null);
                addDependency(appVersionId, libVersionId);
                count++;
            }
            else {
                log.warn("No identifiers found for " + dependency.getFileName());
            }
        }
        log.info("{}/{} dependencies successfully processed. No known identifiers for others.", count, dependencies.size());
    }

    private FileData getIdentifiedLibrary(Dependency dependency) { //Probably better logic needed
        Iterator<Identifier> iterator = dependency.getIdentifiers().iterator();
        FileData bestMatch = FileData.getFileData(dependency.getFileName());
        while (iterator.hasNext()) {
            Identifier identifier = iterator.next();
            if (identifier.getType().equalsIgnoreCase("maven")) {
                FileData temp = FileData.getFileData(fixIdentifierVersionIfNeeded(identifier, bestMatch.getVersion()));
                if (temp.getVendor().contains(".")) {
                    return temp;
                }
                bestMatch = temp;
            }
        }
        return bestMatch;
    }

    private Identifier fixIdentifierVersionIfNeeded(Identifier identifier, String version) {
        String value = identifier.getValue();
        if (value.split(":")[2].startsWith("$")) { //Millegi pärast on mõne teegi versioon jäänud ${muutuja}
            identifier.setValue(value.replace(value.split(":")[2], version));
        }
        return identifier;
    }

}
