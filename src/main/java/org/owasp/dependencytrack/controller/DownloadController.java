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
package org.owasp.dependencytrack.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.owasp.dependencytrack.Constants;
import org.owasp.dependencytrack.tasks.NistDataMirrorUpdater;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller logic for all download-related requests.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
@Controller
@Log
public class DownloadController extends AbstractController {

    /**
     * Service to download the Dependency-Check datafile archive.
     *
     * @param response an HttpServletResponse object
     */
    @RequiresPermissions("dcdata")
    @RequestMapping(value = "/dcdata", method = RequestMethod.GET)
    public void getDataMirrorFile(HttpServletResponse response) {
        InputStream fis = null;
        OutputStream out = null;
        try {
            fis = new FileInputStream(Constants.DATA_ZIP);
            response.setHeader("Content-Disposition", "inline;filename=\"" + Constants.DATA_FILENAME + "\"");
            response.setHeader("Content-Type", "application/octet-stream;");
            out = response.getOutputStream();
            IOUtils.copy(fis, out);
            out.flush();
        } catch (IOException ex) {
            log.info("Error writing Dependency-Check datafile to output stream.");
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * Service to download NIST CPE/CVE XML data files.
     *
     * @param response an HttpServletResponse object
     * @param filename the xml file to download
     * @throws java.io.IOException bad robot
     */
    @RequestMapping(value = "/nist/{filename:.+}", method = RequestMethod.GET)
    public void getNistFile(HttpServletResponse response,
                            @PathVariable("filename") String filename) throws IOException {
        final File canonicalizedFile = new File(filename).getCanonicalFile();
        if (!NistDataMirrorUpdater.isValidNistFile(canonicalizedFile.getName())) {
            response.sendError(404);
        }
        InputStream fis = null;
        OutputStream out = null;
        try {
            fis = new FileInputStream(Constants.NIST_DIR + File.separator + filename);
            if (filename.endsWith(".gz")) {
                response.setHeader("Content-Type", "application/x-gzip;");
            } else if (filename.endsWith(".xml")) {
                response.setHeader("Content-Type", "application/xml;");
            }
            out = response.getOutputStream();
            IOUtils.copy(fis, out);
            out.flush();
        } catch (IOException ex) {
            log.severe("Error writing NIST datafile to output stream.");
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(fis);
        }
    }

}
