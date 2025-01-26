package me.sayandas;

import me.sayandas.commandline.CommandFindInFiles;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("args = " + Arrays.toString(args));
        CommandLine commandLine = new CommandLine(new CommandFindInFiles(30));
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }
}