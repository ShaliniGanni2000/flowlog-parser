import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// FlowLogGenerator is used to create a 10mb log file to assess performance of FlowLogParser
public class FlowLogGenerator {
    public static void main(String[] args) {
        String inputFile = "src/flowlog.txt";
        String outputFile = "src/flowlog_10mb.txt";
        long ts = 9 * 1024 * 1024; // about 10 MB in bytes

        try {
            List<String> sampleLogs = readLogs(inputFile);
            generateFlowLogs(sampleLogs, outputFile, ts);
            System.out.println("Generated log file of size approximately 10 MB");
        } catch (IOException e) {
            System.err.println("Error generating flow log file: " + e.getMessage());
        }
    }

    private static List<String> readLogs(String inputFile) throws IOException {
        List<String> sl = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sl.add(line);
            }
        }
        return sl;
    }

    private static void generateFlowLogs(List<String> sampleLogs, String outputFile, long ts) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            Random random = new Random();
            long cs = 0;

            while (cs <= ts) {
                String log = sampleLogs.get(random.nextInt(sampleLogs.size()));
                String flog = log + "\n";
                writer.write(flog);
                cs += flog.getBytes().length;
            }
        }
    }
}