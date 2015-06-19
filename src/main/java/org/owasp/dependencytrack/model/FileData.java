package org.owasp.dependencytrack.model;

import lombok.Data;

import org.owasp.dependencycheck.dependency.Identifier;
import org.springframework.util.StringUtils;

@Data
public class FileData { // Ilmselt peaks parema loogika välja mõtlema

    private final String vendor;
    private final String name;
    private final String version;

    public static FileData getFileData(Identifier identifier) {
        String type = identifier.getType();
        String[] parts = identifier.getValue().split(":");
        switch (type) {
        case "maven":
            return new FileData(parts[0], parts[1], parts[2]);
        case "cpe":
            return new FileData(parts[2], parts[3], parts[4]);
        default:
            throw new RuntimeException("Unknown type: " + type);
        }
    }

    public static FileData getFileData(String fileName) { // Sest esineb faile formaadis jdigidoc-3.8.1-709.jar.
        fileName = getFileName(fileName);
        int numberOfDashes = StringUtils.countOccurrencesOf(fileName, "-");
        String name = "";
        String version = "";

        if (numberOfDashes == 0) {
            name = fileName;
        }
        else {
            int dashIndex = getDashIndex(fileName);
            boolean containsVersion = dashIndex == fileName.length() ? false : fileName.substring(dashIndex + 1).matches(".*\\d+.*"); //Assume true when at least 1 number after the dash.
            name = containsVersion ? fileName.substring(0, dashIndex) : fileName;
            version = containsVersion ? fileName.substring(dashIndex + 1) : "";
        }
        return new FileData("", name, version);
    }

    private static String getFileName(String fileName) {
        String[] parts = fileName.substring(fileName.indexOf("-") + 1).split(":");
        fileName = parts[parts.length - 1].trim(); // Removes timestamp + archive name
        return removeFileExtension(fileName);
    }

    private static String removeFileExtension(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private static int getDashIndex(String fileName) {
        int index = fileName.lastIndexOf("-");
        if (index < 0) {
            return fileName.length();
        }
        String name = fileName.substring(0, index);
        int secondToLastDashIndex = name.lastIndexOf("-");
        if (secondToLastDashIndex > 0) {
            index = fileName.substring(secondToLastDashIndex + 1, index).matches("(\\d(\\.)?)+") ? secondToLastDashIndex : index;
        }
        if (index == secondToLastDashIndex) {
            return getDashIndex(fileName.substring(0, index));
        }
        else {
            if (Character.isDigit(fileName.charAt(index + 1))) {
                return index;
            }
            return fileName.length();
        }
    }

}
