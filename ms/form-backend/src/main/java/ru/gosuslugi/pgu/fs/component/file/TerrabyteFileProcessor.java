package ru.gosuslugi.pgu.fs.component.file;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import ru.gosuslugi.pgu.common.esia.search.dto.UserPersonalData;
import ru.gosuslugi.pgu.terrabyte.client.TerrabyteClient;
import ru.gosuslugi.pgu.terrabyte.client.model.FileInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class TerrabyteFileProcessor {

    private final TerrabyteClient client;
    private final UserPersonalData userPersonalData;

    public static TerrabyteFileProcessor of(TerrabyteClient client, UserPersonalData userPersonalData) {
        return new TerrabyteFileProcessor(client, userPersonalData);
    }

    public void replaceByZipped(FileInfo fileInfo) {
        File tempFile = null;
        File tempZipFile = null;
        try {
            byte[] origFileBytes = client.getFile(fileInfo, userPersonalData.getUserId(), userPersonalData.getToken());
            tempFile = createTempFile("src", ".temp");
            writeByteArrayToFile(tempFile, origFileBytes);

            String targetZipName = toZipFileName(fileInfo);
            tempZipFile = createTempFile("zip", ".temp");

            zipFile(tempFile, tempZipFile, fileInfo.getFileName());
            removeFromTerrabyte(fileInfo);
            saveZipToTerrabyte(fileInfo, tempZipFile, targetZipName);
        } finally {
            if (Objects.nonNull(tempFile)) tryToDeleteFile(tempFile);
            if (Objects.nonNull(tempZipFile)) tryToDeleteFile(tempZipFile);
        }
    }

    public String toZipFileName(FileInfo fileInfo) {
        String fileName = fileInfo.getFileName();
        String fileExt = fileInfo.getFileExt();
        int extIdx = fileName.lastIndexOf("." + fileExt);
        return fileName.substring(0, extIdx) + ".zip";
    }

    private void tryToDeleteFile(File file) {
        if (!file.delete()) log.error("Ошибка удаления временного файла, file: {}", file.getAbsolutePath());
    }

    private void zipFile(File srcFile, File dstFile, String targetName) {
        int bufferSize = 4096;
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dstFile))) {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(srcFile), bufferSize)) {
                ZipEntry zipEntry = new ZipEntry(targetName);
                zos.putNextEntry(zipEntry);
                zos.setLevel(Deflater.BEST_SPEED);
                byte[] bytes = new byte[bufferSize];
                int length;
                while ((length = bis.read(bytes)) >= 0) {
                    zos.write(bytes, 0, length);
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new FileProcessingException("Cannot create zip archive", e);
        }
    }

    private void writeByteArrayToFile(File target, byte[] from) {
        try {
            FileUtils.writeByteArrayToFile(target, from);
        } catch (IOException e) {
            throw new FileProcessingException("Cannot write data to target file", e);
        }
    }

    private File createTempFile(String prefix, String suffix) {
        try {
            return Files.createTempFile(prefix, suffix).toFile();
        } catch (IOException e) {
            throw new FileProcessingException("Cannot create tempFile", e);
        }
    }

    private void removeFromTerrabyte(FileInfo fileInfo) {
        client.deleteFile(fileInfo, userPersonalData.getUserId(), userPersonalData.getToken());
    }

    private void saveZipToTerrabyte(FileInfo origFileInfo, File fileToZip, String tempZipFileName) {
        byte[] fileToZipBytes = readAllBytesFromFile(fileToZip);
        String mnemonic = origFileInfo.getMnemonic();
        String mimeType = "application/zip";
        long objectId = origFileInfo.getObjectId();
        int objectTypeId = origFileInfo.getObjectTypeId();

        client.internalSaveFile(fileToZipBytes, tempZipFileName, mnemonic, mimeType, objectId, objectTypeId);
    }

    private byte[] readAllBytesFromFile(File zippedSig) {
        try {
            return Files.readAllBytes(zippedSig.toPath());
        } catch (IOException e) {
            throw new FileProcessingException("Error while moving bytes to file", e);
        }
    }
}
