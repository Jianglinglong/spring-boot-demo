package com.jiang.generator.callback;

import org.mybatis.generator.api.ShellCallback;
import org.mybatis.generator.eclipse.core.merge.JavaFileMerger;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;

import java.io.*;

/**
 * @author Jiang
 * @date 2018/12/11
 * @time 11:08
 */
public class GeneratorShellCallback extends DefaultShellCallback implements ShellCallback {

    /**
     * Instantiates a new default shell callback.
     *
     * @param overwrite the overwrite
     */
    public GeneratorShellCallback(boolean overwrite) {
        super(overwrite);
    }

    @Override
    public boolean isMergeSupported() {
        return true;
    }

    @Override
    public String mergeJavaFile(String newFileSource, File existingFile, String[] javadocTags, String fileEncoding) throws ShellException {
        if (existingFile!=null){
            JavaFileMerger fileMerger = new JavaFileMerger(newFileSource, existingFile.getAbsolutePath(), javadocTags, fileEncoding);
            return fileMerger.getMergedSource().replaceAll(" private ","\r\n\tprivate ")
                    .replaceAll("@mbg.generated","\r\n\t * @mbg.generated")
                    .replaceAll("\\*/","*/\r\n\t")
                    ;
        }else {
            return newFileSource;
        }
    }

    private String getExistingFileContents(File existingFile, String fileEncoding) throws ShellException {
        StringBuilder sb = new StringBuilder();
        try {
            FileInputStream fileInputStream = new FileInputStream(existingFile);
            InputStreamReader reader;
            if (fileEncoding == null) {
                reader = new InputStreamReader(fileInputStream);
            } else {
                reader = new InputStreamReader(fileInputStream, fileEncoding);
            }
            BufferedReader br = new BufferedReader(reader);
            char[] buffer = new char[1024];
            for (int returnedBytes = br.read(buffer); returnedBytes != -1; returnedBytes = br.read(buffer)) {
                sb.append(buffer, 0, returnedBytes);
            }
            br.close();
        } catch (IOException e) {
            throw new ShellException(String.format("read file %s error!", existingFile.getAbsoluteFile()));
        }
        return sb.toString();
    }
}
