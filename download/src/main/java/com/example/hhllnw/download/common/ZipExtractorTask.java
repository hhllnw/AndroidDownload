package com.example.hhllnw.download.common;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by hhl on 2018/5/4.
 */

public class ZipExtractorTask extends AsyncTask<Void, Integer, Long> {
    private final String TAG = ZipExtractorTask.class.getSimpleName();
    private final File mInput;
    private final File mOutput;
    private int mProgress = 0;
    private long totalLength;

    public ZipExtractorTask(String in, String out) {
        super();
        mInput = new File(in);
        mOutput = new File(out);
        if (!mOutput.exists()) {
            if (!mOutput.mkdirs()) {
                Log.e(TAG, "Failed to make directories:" + mOutput.getAbsolutePath());
            }
        }
    }

    @Override
    protected Long doInBackground(Void... params) {
        // TODO Auto-generated method stub
        return unzip();
    }

    @Override
    protected void onPostExecute(Long result) {
        // TODO Auto-generated method stub
        //super.onPostExecute(result);
        if (isCancelled())
            return;
    }

    @Override
    protected void onPreExecute() {
        //super.onPreExecute();
        if (onResolveZipListener != null){
            onResolveZipListener.startResolveZip();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        //super.onProgressUpdate(values);
        if (values.length > 1) {
            int max = values[1];
        } else {
            int length = values[0].intValue();
            if (onResolveZipListener != null){
                onResolveZipListener.progress(length,totalLength);
                if (length == totalLength){
                    onResolveZipListener.complete();
                }
            }
        }

    }

    private long unzip() {
        long extractedSize = 0L;
        Enumeration<ZipEntry> entries;
        ZipFile zip = null;
        try {
            zip = new ZipFile(mInput);
            totalLength = getOriginalSize(zip);
            publishProgress(0, (int) totalLength);
            entries = (Enumeration<ZipEntry>) zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                File destination = new File(mOutput, entry.getName());
                if (!destination.getParentFile().exists()) {
                    Log.e(TAG, "make=" + destination.getParentFile().getAbsolutePath());
                    destination.getParentFile().mkdirs();
                }
                ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
                extractedSize += copy(zip.getInputStream(entry), outStream);
                outStream.close();
            }
        } catch (ZipException e) {
            e.printStackTrace();
            if (onResolveZipListener != null){
                onResolveZipListener.Exception(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (onResolveZipListener != null){
                onResolveZipListener.Exception(e);
            }
        } finally {
            try {
                zip.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (onResolveZipListener != null){
                    onResolveZipListener.Exception(e);
                }
            }
        }

        return extractedSize;
    }

    private long getOriginalSize(ZipFile file) {
        Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
        long originalSize = 0l;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getSize() >= 0) {
                originalSize += entry.getSize();
            }
        }
        return originalSize;
    }

    private int copy(InputStream input, OutputStream output) {
        byte[] buffer = new byte[1024 * 8];
        BufferedInputStream in = new BufferedInputStream(input, 1024 * 8);
        BufferedOutputStream out = new BufferedOutputStream(output, 1024 * 8);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, 1024 * 8)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    private final class ProgressReportingOutputStream extends FileOutputStream {

        public ProgressReportingOutputStream(File file)
                throws FileNotFoundException {
            super(file);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            super.write(buffer, byteOffset, byteCount);
            mProgress += byteCount;
            publishProgress(mProgress);
        }
    }

    /**
     * 解压zip包监听
     */
    public interface OnResolveZipListener {
        /**
         * 开始解压
         */
        void startResolveZip();

        /**
         * 解压过程
         *
         * @param currentLength
         * @param totalLength
         */
        void progress(long currentLength, long totalLength);

        /**
         * 解压完成
         */
        void complete();

        /**
         * 解压异常
         *
         * @param e
         */
        void Exception(Exception e);
    }

    public OnResolveZipListener onResolveZipListener;

    public void setOnResolveZipListener(OnResolveZipListener onResolveZipListener){
        this.onResolveZipListener = onResolveZipListener;
    }


}
