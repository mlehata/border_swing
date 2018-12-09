package wtc.mk;

import org.apache.commons.codec.digest.DigestUtils;

class Trade {
    private String[] fix;
    private Database database = new Database();

    Trade(String[] fix) {
        this.fix = fix;
    }

    boolean process() {
        String temp = "";
        for (int i = 0; i < fix.length - 1; i++) {
            temp = temp.concat(fix[i]).concat("|");
        }
        String checksum = DigestUtils.md5Hex(temp);
        if (checksum.equals(fix[8])) {
            switch (fix[2].split("=")[1]) {
                case "BUY":
                    return database.buyStock(fix[5].split("=")[1], Integer.parseInt(fix[6].split("=")[1]), Integer.parseInt(fix[7].split("=")[1]));
                case "SELL":
                    return database.sellStock(fix[5].split("=")[1], Integer.parseInt(fix[6].split("=")[1]), Integer.parseInt(fix[7].split("=")[1]));
                default:
                    System.out.println("Error");
                    return false;
            }
        } else {
            System.out.println("Error: Packet data loss occurred.");
        }
        return false;
    }
}
