package wtc.mk;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Database {
    private String read;

    private File data = new File("market/database.txt");

    public String findStock(String id) {
        try {
            List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

            for (String line : filecontent) {
                read = line;
                if (id.equals(line.split(" ")[0]))
                    break;
            }
        } catch (IOException e) {
            System.out.println("There was a problem: " + e);
        }
        return read;
    }

    public boolean buyStock(String id, int price, int num) {
        try {
            List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

            for (int i = 0; i < filecontent.size(); i++) {
                if (id.equals(filecontent.get(i).split(" ")[0])) {
                    if (price >= Integer.parseInt(filecontent.get(i).split(" ")[1])) {
                        int oldnum = Integer.parseInt(filecontent.get(i).split(" ")[2]);
                        int newnum = oldnum - num;
                        if (newnum < 0)
                            return false;
                        else {
                            String[] line = filecontent.get(i).split(" ");
                            filecontent.set(i, line[0] + " " + line[1] + " " + newnum);
                            Files.write(Paths.get(data.getAbsolutePath()), filecontent, StandardCharsets.UTF_8);
                        }
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("There was a problem: " + e);
        }
        return false;
    }

    public boolean sellStock(String id, int price, int num) {
        try {
            List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

            for (int i = 0; i < filecontent.size(); i++) {
                if (id.equals(filecontent.get(i).split(" ")[0])) {
                    if (price <= Integer.parseInt(filecontent.get(i).split(" ")[1])) {
                        int oldnum = Integer.parseInt(filecontent.get(i).split(" ")[2]);
                        int newnum = oldnum + num;
                        String[] line = filecontent.get(i).split(" ");
                        filecontent.set(i, line[0] + " " + line[1] + " " + newnum);
                        Files.write(Paths.get(data.getAbsolutePath()), filecontent, StandardCharsets.UTF_8);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("There was a problem: " + e);
        }
        return false;
    }

    public void printAllStock() {
        try {
            List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

            System.out.println("All Stocks:");
            for (String line: filecontent) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("There was a problem: " + e);
        }
    }
}
