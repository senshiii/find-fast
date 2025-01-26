import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProgressBarDemo {

    static final Logger log = LogManager.getLogger(ProgressBarDemo.class);

    public static void main(String[] args) throws InterruptedException {
        int n = 10;
        ProgressBar pb = new ProgressBarBuilder()
                .setTaskName("Searching Files")
                .hideEta()
                .setStyle(ProgressBarStyle.ASCII)
                .setInitialMax(n)
                .setUnit(" Hits", 1)
                .build();
        boolean processing = true;
        Random r = new Random();
        BlockingQueue<Integer> q = new LinkedBlockingQueue<>();
        while(processing){
            Thread.sleep(1000);
            int i = r.nextInt();
            q.add(i);
            pb.step();
            if(q.size() == n) processing = false;
        }
        log.info("Hits found at: {}", q);
    }
}
