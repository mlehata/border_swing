package wtc.br;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Stock {
    private String[] fix;
    private File data = new File("broker/stock.txt");

    Stock(String[] fix) {
        this.fix = fix;
    }

    void process() {
        if (fix[8].equals("success")) {
            switch (fix[2].split("=")[1]) {
                case "BUY":
                    try {
                        List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

                        for (int i = 0; i < filecontent.size(); i++) {
                            if (fix[5].split("=")[1].equals(filecontent.get(i).split(" ")[0])) {
                                int oldnum = Integer.parseInt(filecontent.get(i).split(" ")[1]);
                                int newnum = oldnum + Integer.parseInt(fix[7].split("=")[1]);
                                filecontent.set(i, fix[5].split("=")[1] + " " + newnum);
                                break;
                            } else if (i == filecontent.size() - 1) {
                                filecontent.add(fix[5].split("=")[1] + " " + fix[7].split("=")[1]);
                                break;
                            }
                        }
                        if (filecontent.size() == 0) {
                            filecontent.add(fix[5].split("=")[1] + " " + fix[7].split("=")[1]);
                        }
                        Files.write(Paths.get(data.getAbsolutePath()), filecontent, StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        System.out.println("There was a problem: " + e);
                    }
                    break;
                case "SELL":
                    try {
                        List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

                        for (int i = 0; i < filecontent.size(); i++) {
                            if (fix[5].split("=")[1].equals(filecontent.get(i).split(" ")[0])) {
                                int oldnum = Integer.parseInt(filecontent.get(i).split(" ")[1]);
                                int newnum = oldnum - Integer.parseInt(fix[7].split("=")[1]);
                                filecontent.set(i, fix[5].split("=")[1] + " " + newnum);
                                Files.write(Paths.get(data.getAbsolutePath()), filecontent, StandardCharsets.UTF_8);
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("There was a problem: " + e);
                    }
                    break;
                default:
                    System.out.println("Error");
                    break;
            }
        }
    }

    boolean check() {
        if (fix[2].split("=")[1].equals("SELL")) {
            try {
                List<String> filecontent = new ArrayList<>(Files.readAllLines(Paths.get(data.getAbsolutePath()), StandardCharsets.UTF_8));

                for (String line : filecontent) {
                    if (fix[5].split("=")[1].equals(line.split(" ")[0])) {
                        return  Integer.parseInt(fix[7].split("=")[1]) <= Integer.parseInt(line.split(" ")[1]);
                    }
                }
            } catch (IOException e) {
                System.out.println("There was a problem: " + e);
            }
            return false;
        }
        return true;
    }
}
