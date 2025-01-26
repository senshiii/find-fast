package me.sayandas.processing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.sayandas.textsearch.BoyerMoore;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilesChunkProcessor implements Runnable{

    private List<Path> pathsInChunk;
    private int chunkId;
    private int nFiles;
    private String searchString;
    private ExecutorService executorService;
    private CountDownLatch cdl;
    private AtomicReference<List<ResultItem>> results;

    @Data
    @AllArgsConstructor
    static class FileSearchTask implements Runnable {

        private Path filePath;
        private String query;
        private AtomicReference<List<ResultItem>> results;

        @Override
        public void run() {
            BoyerMoore boyerMoore = new BoyerMoore(query);
            try(Stream<String> lines = Files.lines(filePath, Charset.defaultCharset())){
                AtomicInteger lineNumber = new AtomicInteger(1);
                lines.forEach((line) -> {
                    System.out.println("\nProcessing file : " + filePath + " and line: " + lineNumber.get() + ". Text: " + line);

                    List<Integer> hitIndices = boyerMoore.searchQuery(line, query);
                    System.out.println("[FileSearchTask#run] hitIndices = " + hitIndices);

                    List<ResultItem> resultItems = hitIndices
                            .stream()
                            .map(
                                    (index) -> new ResultItem(filePath.toAbsolutePath().toString(), lineNumber.get(), index))
                            .toList();
//                    System.out.println("FileSearchTask.run Result Items: " + resultItems);

//                    System.out.println("results = " + results.get());
                    results.getAndUpdate(resultItems1 -> {
                        resultItems1.addAll(resultItems);
                        return resultItems1;
                    });
                    System.out.println("results = " + results.get());

                    lineNumber.getAndIncrement();
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public void run() {
        System.out.println("Thread: " + Thread.currentThread().getName() + ". Processing Chunk: " + chunkId);
        pathsInChunk.forEach((path) -> new FileSearchTask(path, searchString, results).run());
        System.out.println("Counting down latch: " + cdl.getCount());
        cdl.countDown();
    }
}

