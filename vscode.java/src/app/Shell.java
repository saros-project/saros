package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Shell {
    public void start() throws IOException {
        BufferedReader stdin = new BufferedReader
      (new InputStreamReader(System.in));

      System.out.println("Waiting for input");
      String r = stdin.readLine();  
      System.out.println("Read: " + r);
    }
}