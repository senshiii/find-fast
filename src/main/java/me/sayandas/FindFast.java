package me.sayandas;

import me.tongfei.progressbar.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import me.sayandas.processing.FilesChunkProcessor;
import me.sayandas.processing.ResultItem;

import javax.xml.transform.Result;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class FindFast {

    private int maxThreads;
    private ExecutorService executorService;
    private static final Set<String> TEXT_FILE_EXTENSIONS = Set.of("txt", "java", "xml", "csv", "log", "html", "json", "yaml", "md");
    private final Logger logger = LogManager.getLogger(FindFast.class);

    public FindFast(int maxThreads){
        this.maxThreads = maxThreads;
    }

    private boolean isTextFile(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String fileExtension = getFileExtension(fileName).toLowerCase();

        // Check by extension
        if (TEXT_FILE_EXTENSIONS.contains(fileExtension)) {
            return true;
        }

        // Fallback: Check by content
        try (InputStream is = new BufferedInputStream(Files.newInputStream(filePath))) {
            int bytesRead;
            byte[] buffer = new byte[512];
            while ((bytesRead = is.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    if (buffer[i] < 32 && buffer[i] != '\t' && buffer[i] != '\n' && buffer[i] != '\r') {
                        return false; // Non-text character found
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not inspect file: " + filePath);
        }

        return false;
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    }

    private int getNumberOfThreads(int nFiles){
        int cores = Runtime.getRuntime().availableProcessors();
        return Math.min(nFiles, Math.max(cores*2, maxThreads));
    }

    private int getCountOfFiles(Path directory){
        // Find out how many files are in the directory
        int nFiles = 0;
        try(Stream<Path> s = Files.walk(directory)){
            nFiles = (int) s.count();
        }catch(IOException e){
            throw new RuntimeException("Unable to navigate the input directory tree", e);
        }
        return nFiles;
    }

    public List<ResultItem> findInDirectory(String directory, String query){
        logger.info("Reading directory - {}", directory);
        File directoryFile = new File(directory);
        if(!directoryFile.isDirectory())
            throw new RuntimeException("Provided path - " + directory + " is not a directory");

        Path directoryPath = Paths.get(directory);
        final int nFiles = this.getCountOfFiles(directoryPath);
        int nThreads = this.getNumberOfThreads(nFiles);
        int nChunks = nFiles / nThreads;
        int chunkSize = nFiles / nChunks;
        nThreads = Math.min(nChunks, nThreads);

        HashMap<Integer, List<Path>> chunks = new HashMap<>();
        for(int i = 1; i <= nChunks; i++){
            chunks.put(i, new ArrayList<>());
        }
        AtomicInteger chunkId = new AtomicInteger(1);

        try(Stream<Path> pathsStream = Files.walk(directoryPath)){
            pathsStream
                    .filter(Files::isReadable)
                    .filter(Files::isRegularFile)
                    .filter(this::isTextFile)
                    .forEach(filePath -> {
                        boolean isChunkFull = chunks.get(chunkId.get()).size() == chunkSize;
                        boolean isLastChunk = chunkId.get() == nChunks;
                        if(!isLastChunk && isChunkFull) chunkId.set(chunkId.get() + 1);
                        chunks.get(chunkId.get()).add(filePath);
                    });
        }catch(IOException e){
            throw new RuntimeException("Failed to analyze directory", e);
        }

        System.out.println("Chunks: " + chunks);

        CountDownLatch countDownChunks = new CountDownLatch(nChunks);

        AtomicReference<List<ResultItem>> searchResults = new AtomicReference<>(new ArrayList<>());
        ArrayBlockingQueue<ResultItem> searchResultsQueue = new ArrayBlockingQueue<ResultItem>(nFiles);
        this.executorService = Executors.newFixedThreadPool(nThreads);
        chunks.forEach((cId, chunk) -> {
            this.executorService.submit(
                    new FilesChunkProcessor(chunk, cId, nFiles, query, this.executorService, countDownChunks, searchResults));
        });
        try{
            countDownChunks.await();
        }catch(InterruptedException e){
            throw new RuntimeException("Unexpected error occurred", e);
        }

        return searchResults.get();
    }


}
