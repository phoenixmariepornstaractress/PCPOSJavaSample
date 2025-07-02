package ir.co.ecd.sample.pcpos;

public class Main {

    public interface SerialAfterReceivedListener {
        void afterReceived(Serial serial);
    }

    public static class Serial {

        public enum PaymentType {
            Sale, Refund
        }

        private String portName;
        private String serialNumber;
        private String terminalNumber;
        private String merchantNumber;
        private PaymentType paymentType;
        private String amount;
        private String STAN = "123456";
        private String RRN = "654321";
        private String resultCode = "00";
        private String dateTime = "202507021200";
        private String PAN = "603799******1234";
        private String balance = "50000";
        private String description = "Transaction Approved";

        private SerialAfterReceivedListener listener;

        public void setPortName(String portName) {
            this.portName = portName;
        }

        public void setSerialAfterReceivedListener(SerialAfterReceivedListener listener) {
            this.listener = listener;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public void setTerminalNumber(String terminalNumber) {
            this.terminalNumber = terminalNumber;
        }

        public void setMerchantNumber(String merchantNumber) {
            this.merchantNumber = merchantNumber;
        }

        public void setPaymentType(PaymentType paymentType) {
            this.paymentType = paymentType;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public boolean initCommunication() {
            System.out.printf("[INFO] Initializing communication on port: %s%n", portName);
            return true;
        }

        public void payment() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("[ERROR] Payment thread interrupted.");
                return;
            }
            if (listener != null) listener.afterReceived(this);
        }

        public boolean closeCommunication() {
            System.out.printf("[INFO] Closing communication on port: %s%n", portName);
            return true;
        }

        public boolean validateParameters() {
            return isNotEmpty(portName) && isNotEmpty(serialNumber)
                    && isNotEmpty(terminalNumber) && isNotEmpty(merchantNumber)
                    && paymentType != null && isNotEmpty(amount);
        }

        private boolean isNotEmpty(String value) {
            return value != null && !value.trim().isEmpty();
        }

        public String buildTransactionSummary() {
            return String.format("[SUMMARY]\nAmount: %s\nType: %s\nMerchant: %s\nTerminal: %s",
                    amount, paymentType, merchantNumber, terminalNumber);
        }

        public void reset() {
            serialNumber = terminalNumber = merchantNumber = amount = null;
            paymentType = null;
            STAN = RRN = resultCode = dateTime = PAN = balance = description = "";
        }

        public void simulateFailure() {
            resultCode = "99";
            description = "Transaction Failed";
            balance = PAN = "";
        }

        public String getMaskedPAN() {
            return PAN != null && PAN.length() >= 10
                    ? PAN.replaceAll("(?<=\\d{6})\\d(?=\\d{4})", "*")
                    : PAN;
        }

        public void generateNewSTAN() {
            STAN = String.valueOf((int) (Math.random() * 900000 + 100000));
        }

        public String exportTransactionDataCSV() {
            return String.join(",",
                    serialNumber, terminalNumber, merchantNumber,
                    paymentType != null ? paymentType.name() : "",
                    amount, STAN, RRN, resultCode, dateTime,
                    PAN, balance, description);
        }

        // Getters
        public PaymentType getPaymentType() { return paymentType; }
        public String getSerialNumber() { return serialNumber; }
        public String getTerminalNumber() { return terminalNumber; }
        public String getMerchantNumber() { return merchantNumber; }
        public String getSTAN() { return STAN; }
        public String getRRN() { return RRN; }
        public String getResultCode() { return resultCode; }
        public String getDateTime() { return dateTime; }
        public String getPAN() { return PAN; }
        public String getBalance() { return balance; }
        public String getAmount() { return amount; }
        public String getDescription() { return description; }
    }

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {
        Serial serial = new Serial();
        serial.setPortName("COM3");
        serial.setSerialAfterReceivedListener(this::afterReceived);
        serial.setSerialNumber("003000009592");
        serial.setTerminalNumber("96090001");
        serial.setMerchantNumber("017379960902001");
        serial.setPaymentType(Serial.PaymentType.Sale);
        serial.setAmount("1000");

        if (!serial.validateParameters()) {
            System.err.println("[ERROR] Invalid serial parameters.");
            return;
        }

        serial.generateNewSTAN();

        if (serial.initCommunication()) {
            System.out.println(serial.buildTransactionSummary());
            serial.payment();
        } else {
            System.err.println("[ERROR] Failed to initialize communication.");
        }
    }

    public void afterReceived(Serial serial) {
        System.out.printf("%n[RESULT]\n%-15s%s%n", "PAYMENT_TYPE:", serial.getPaymentType());
        System.out.printf("%-15s%s%n", "SERIAL_NO:", serial.getSerialNumber());
        System.out.printf("%-15s%s%n", "MERCHANT_NO:", serial.getMerchantNumber());
        System.out.printf("%-15s%s%n", "TERMINAL_NO:", serial.getTerminalNumber());
        System.out.printf("%-15s%s%n", "STAN:", serial.getSTAN());
        System.out.printf("%-15s%s%n", "RRN:", serial.getRRN());
        System.out.printf("%-15s%s%n", "RES_CODE:", serial.getResultCode());
        System.out.printf("%-15s%s%n", "AMOUNT:", serial.getAmount());
        System.out.printf("%-15s%s%n", "DATETIME:", serial.getDateTime());
        System.out.printf("%-15s%s%n", "PAN:", serial.getMaskedPAN());
        System.out.printf("%-15s%s%n", "BALANCE:", serial.getBalance());
        System.out.printf("%-15s%s%n", "DESCRIPTION:", serial.getDescription());

        System.out.printf("%n[CSV] %s%n", serial.exportTransactionDataCSV());

        if (serial.closeCommunication()) {
            System.out.println("[INFO] Communication closed successfully.");
        }

        serial.reset();
        System.out.println("[INFO] Serial state reset.");
    }
}
 
