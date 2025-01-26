package me.sayandas.commandline;

import me.sayandas.FindFast;
import me.sayandas.processing.ResultItem;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "fif",
        mixinStandardHelpOptions = true,
        description = "Search for all occurrences of a pattern appearing in files in a directory")
public class CommandFindInFiles implements Callable<List<ResultItem>> {

    @CommandLine.Parameters(index = "0",
            description = "Absolute path to the directory inside which search is to be conducted"
    )
    private String directoryPath;

    @CommandLine.Parameters(index = "1",
        description = "Text you want to search for"
    )
    private String queryString;

    private FindFast fif;

    public CommandFindInFiles(int maxThreads){
        this.fif = new FindFast(maxThreads);
    }

    @Override
    public List<ResultItem> call() throws Exception {
        return fif.findInDirectory(directoryPath, queryString);
    }

}
