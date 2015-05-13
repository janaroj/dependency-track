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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.owasp.dependencycheck.dependency.Dependency;
import org.owasp.dependencycheck.reporting.ReportGenerator;
import org.owasp.dependencytrack.dao.ReportDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReportService {

    @Autowired
    private ReportDao reportDao;

    @Transactional
    public String generateDependencyCheckReport(int applicationVersionId, ReportGenerator.Format format) {
        return reportDao.generateDependencyCheckReport(applicationVersionId, format);
    }

    @Transactional
    public List<Dependency> getDependencies(MultipartFile multipartFile) {
        File file = null;
        try {
            file = convert(multipartFile);
            return reportDao.getDependencies(file);
        }
        catch (IOException ex) {
            throw new RuntimeException("Error transfering multipartFile to file", ex);
        }
        finally {
            file.delete();
        }
    }
    
    private File convert(MultipartFile file) throws IOException {    
        File convFile = new File(System.currentTimeMillis() + "-" + file.getOriginalFilename());
        convFile.createNewFile(); 
        FileOutputStream fos = new FileOutputStream(convFile); 
        fos.write(file.getBytes());
        fos.close(); 
        return convFile;
    }
    
}
