package com.sasinduprasad.pockethost;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.sasinduprasad.androidserver.WebServerService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;

public class AndroidWebServer extends NanoHTTPD {

    private final File rootDir;

    private final ServerViewModel serverViewModel;

    public AndroidWebServer(int port, ServerViewModel serverViewModel) {
        super(port);
        this.serverViewModel = serverViewModel;
        rootDir = new File("/data/data/com.sasinduprasad.pockethost/files");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        long requestStartTime = System.currentTimeMillis();
        String uri = session.getUri();
//        Log.i("check", "URI requested: " + uri);
        Method method = session.getMethod();

        Response response;

        if(uri.endsWith("html")){
            long requestEndTime = System.currentTimeMillis();
            serverViewModel.recordRequest(requestStartTime, requestEndTime);
//        Log.i("check", "Recorded request from: " + uri);
        }

        // Handle the file upload (POST request)
        if (Method.POST.equals(method)) {
            if (uri.equals("/upload")) {
                try {
                    Map<String, String> files = new HashMap<>();
                    session.parseBody(files);
                    String filePath = files.get("uploadedFile");

                    String projectName = uri.split("/")[2]; // assuming uri is /upload/{projectName}
                    File projectDir = new File(rootDir, projectName);
                    if (!projectDir.exists()) {
                        projectDir.mkdirs();
                    }

                    File dest = new File(projectDir, "uploaded.html");
                    FileInputStream fis = new FileInputStream(filePath);
                    FileOutputStream fos = new FileOutputStream(dest);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }

                    fis.close();
                    fos.close();

                    response = newFixedLengthResponse("File uploaded successfully");

                } catch (IOException | ResponseException e) {
                    e.printStackTrace();
                    response = newFixedLengthResponse("Error uploading file");
                }

            } else {
                response = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "<h1>404 Not Found</h1>");
            }
        }

        else {
            // Handle GET requests
            String[] pathParts = uri.split("/", 2);
            if (pathParts.length > 1) {
                String projectName = pathParts[0];
                String fileName = pathParts[1];
                if (uri.endsWith(".css")) {
                    return serveStaticFile(uri, "text/css");
                } else if (uri.endsWith(".js")) {
                    return serveStaticFile(uri, "application/javascript");
                } else if (uri.endsWith(".html") || uri.equals("/")) {
                    return serveFileFromProject(projectName,uri);
                } else {
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "<h1>404 Not Found</h1>");
                }
            } else {
                response = newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "<h1>404 Not Found</h1>");
            }
        }




        return response;
    }

    private Response serveStaticFile(String uri, String contentType) {
        File file = new File(rootDir, uri);
        if (file.exists() && file.isFile()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                return newFixedLengthResponse(Response.Status.OK, contentType, fis, file.length());
            } catch (IOException e) {
                return newFixedLengthResponse("Error reading file");
            }
        } else {
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "<h1>404 Not Found</h1>");
        }
    }
    private Response serveFileFromProject(String projectName, String fileName) {
        File projectDir = new File(rootDir, projectName);
        File file = new File(projectDir, fileName);

        if (file.exists() && file.isFile()) {
            Log.i("check", "File found: " + file.getAbsolutePath());
            try {
                FileInputStream fis = new FileInputStream(file);
                return newFixedLengthResponse(Response.Status.OK, MIME_HTML, fis, file.length());
            } catch (IOException e) {
                Log.e("webserver_error", Objects.requireNonNull(e.getMessage()));
                return newFixedLengthResponse("Error reading file");
            }
        } else {
            Log.i("check", "File not found: " + file.getAbsolutePath());
            return newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_HTML, "<h1>404 Not Found</h1>");
        }
    }
}
