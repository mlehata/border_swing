package wtc.br.Controler;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Scanner;

public class Fix {
    private String ID;

    public Fix(String ID) {
        this.ID = ID;
    }

    public String order() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String line;
        String fix = "8=FIX.4.4|9=106";

        while (true) {
            System.out.println("1.Buy\n2.Sell");
            line = scanner.nextLine();
            if (line.equals("1")) {
                fix = fix.concat("|35=").concat("BUY");
                break;
            } else if (line.equals("2")) {
                fix = fix.concat("|35=").concat("SELL");
                break;
            } else
                System.out.println("Incorrect input");
        }

        fix = fix.concat("|56=100000").concat("|49=").concat(ID);

        while (true) {
            System.out.println("Select stock:");
            System.out.println("1.AAPL");
            System.out.println("2.TSLA");
            System.out.println("3.GOOGL");
            System.out.println("4.FB");
            System.out.println("5.V");
            line = scanner.nextLine();
            if (line.equals("1")) {
                fix = fix.concat("|11=").concat("AAPL").concat("|44=217");
                break;
            } else if (line.equals("2")) {
                fix = fix.concat("|11=").concat("TSLA").concat("|44=343");
                break;
            } else if (line.equals("3")) {
                fix = fix.concat("|11=").concat("GOOGL").concat("|44=1091");
                break;
            } else if (line.equals("4")) {
                fix = fix.concat("|11=").concat("FB").concat("|44=150");
                break;
            } else if (line.equals("5")) {
                fix = fix.concat("|11=").concat("V").concat("|44=139");
                break;
            } else
                System.out.println("Incorrect input");
        }

        while (true) {
            System.out.println("How many?");
            line = scanner.nextLine();
            if (StringUtils.isNumeric(line)) {
                fix = fix.concat("|38=").concat(line).concat("|");
                break;
            } else
                System.out.println("Incorrect input");
        }

        return fix;
    }
}
