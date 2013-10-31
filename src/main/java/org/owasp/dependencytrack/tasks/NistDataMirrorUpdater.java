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
package org.owasp.dependencytrack.tasks;

import org.apache.commons.io.IOUtils;
import org.owasp.dependencytrack.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;


public class NistDataMirrorUpdater {

    private static final String CVE_12_MODIFIED_URL = "http://nvd.nist.gov/download/nvdcve-modified.xml";
    private static final String CVE_20_MODIFIED_URL = "http://static.nvd.nist.gov/feeds/xml/cve/nvdcve-2.0-modified.xml";
    private static final String CVE_12_BASE_URL = "http://nvd.nist.gov/download/nvdcve-%d.xml";
    private static final String CVE_20_BASE_URL = "http://static.nvd.nist.gov/feeds/xml/cve/nvdcve-2.0-%d.xml";
    private static final int START_YEAR = 2002;
    private static final int END_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    /**
     * Setup logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NistDataMirrorUpdater.class);


    /**
     * Updates the NIST data directory.
     */
    @Scheduled(fixedRate = 86400000) // every 24 hours
    public void doUpdates() {
        try {
            doDownload(CVE_12_MODIFIED_URL);
            doDownload(CVE_20_MODIFIED_URL);
            for (int i=START_YEAR; i<=END_YEAR; i++) {
                String cve12BaseUrl = CVE_12_BASE_URL.replace("%d", String.valueOf(i));
                String cve20BaseUrl = CVE_20_BASE_URL.replace("%d", String.valueOf(i));
                doDownload(cve12BaseUrl);
                doDownload(cve20BaseUrl);
            }
        } catch (IOException e) {
            LOGGER.warn("An error occurred during the NIST data mirror update process: " + e.getMessage());
        }
    }

    private void doDownload(String cveUrl) throws IOException {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;

        try {
            URL url = new URL(cveUrl);
            URLConnection urlConnection = url.openConnection();
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Downloading " + url.toExternalForm());
            }

            String filename = url.getFile();
            filename = filename.substring(filename.lastIndexOf('/') + 1);

            bis = new BufferedInputStream(urlConnection.getInputStream());

            File dir = new File(Constants.NIST_DIR);
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(Constants.NIST_DIR + File.separator + filename);
            bos = new BufferedOutputStream(new FileOutputStream(file));

            int i;
            while ((i = bis.read()) != -1) {
                bos.write( i );
            }
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("An error occurred during the download or saving of NIST XML data: " + e.getMessage());
            }
        } finally {
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(bos);
        }
    }

    public static boolean isValidNistFile(String filename) {
        if (filename.equals(CVE_12_MODIFIED_URL.substring(CVE_12_MODIFIED_URL.lastIndexOf('/') + 1)) ||
                filename.equals(CVE_20_MODIFIED_URL.substring(CVE_20_MODIFIED_URL.lastIndexOf('/') + 1))) {
            return true;
        }
        for (int i=START_YEAR; i<=END_YEAR; i++) {
            String cve12BaseUrl = CVE_12_BASE_URL.replace("%d", String.valueOf(i));
            String cve20BaseUrl = CVE_20_BASE_URL.replace("%d", String.valueOf(i));

            if (filename.equals(cve12BaseUrl.substring(cve12BaseUrl.lastIndexOf('/') + 1)) ||
                    filename.equals(cve20BaseUrl.substring(cve20BaseUrl.lastIndexOf('/') + 1))) {
                return true;
            }
        }
        return false;
    }
}