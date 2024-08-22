import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

public class FlowLogParser {

    private static Map<String, String> lookupTable = new HashMap<>();

    private static Map<String, Integer> tagCounts = new HashMap<>();
    private static Map<String, Integer> portProtocolCounts = new HashMap<>();
    private static Map<String, String> protocolMap = new HashMap<>();

    public static void main(String[] args) {
        if(args.length < 3){
            System.out.println("Usage: java FlowLogParser <lookup-table-file> <protocol-numbers-file> <flow-log-file>");
            return;
        }

        String lookupTableFile = args[0];
        String protocolNumbersFile = args[1];
        String flowLogFile = args[2];

        if (!validateInputFiles(lookupTableFile, protocolNumbersFile, flowLogFile)) {
            return;
        }


        try {
            // Step 1: Will load the lookup table
            loadLookupTable(lookupTableFile);

            // Step 2: Will load the protocol numbers map
            loadProtocolMap(protocolNumbersFile);

            // Step 3: Processing the flow log file to generate the counts
            processFlowLogFile(flowLogFile);

            // Step 4: Output the results
            outputResults("output.txt");
        } catch (IOException e) {
            System.err.println("An error occurred during file processing: " + e.getMessage());
        }
    }

    public static boolean validateInputFiles(String... files) {
        for (String file : files) {
            File f = new File(file);
            if (!f.exists() || !f.canRead()) {
                System.err.println("Error: File " + file + " does not exist or is not readable.");
                return false;
            }
        }
        return true;
    }

    public static void loadLookupTable(String lookupTableFile) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(lookupTableFile))) {
            String line;
            while ((line = br.readLine())!= null){
                String[] parts = line.split(",");
                if (parts.length != 3) continue;

                String dstport = parts[0].trim();
                String protocol = parts[1].trim().toLowerCase();
                String tag = parts[2].trim();

                String key = dstport + "," +protocol;
                lookupTable.put(key, tag);
            }
        }
    }

    public static void loadProtocolMap(String protocolNumbersFile) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(protocolNumbersFile))) {
            String line;
            // Skip the header line
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String decimal = parts[0].trim();
                String keyword = parts[1].trim().toLowerCase();

                protocolMap.put(decimal, keyword);
            }
        }
    }

    public static String mapProtocolNumberToName(String protocolNumber) {
        return protocolMap.getOrDefault(protocolNumber, "unknown");
    }


    public static void processFlowLogFile(String flowLogFile) throws IOException{
        try(BufferedReader br = new BufferedReader(new FileReader(flowLogFile))) {
            String line;
            while ((line=br.readLine())!= null){
                String[] parts = line.split("\\s+");
                if(parts.length < 8) continue;

                String dstport = parts[6].trim();
                String protocol = mapProtocolNumberToName(parts[7].trim());

                String key = dstport + "," + protocol;
                String tag = lookupTable.getOrDefault(key, "Untagged");

                tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);

                portProtocolCounts.put(key, portProtocolCounts.getOrDefault(key, 0)+ 1);

            }

        }
    }


    public static void outputResults(String outputFilePath) throws IOException{
        try (PrintWriter writer = new PrintWriter(outputFilePath)) {
            writer.println("Tag Counts:");
            writer.println("Tag,Count");
            for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
                writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
            }

            writer.println("\nPort/Protocol Combination Counts:");
            writer.println("Port,Protocol,Count");
            for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
                String[] keyParts = entry.getKey().split(",");
                String dstport = keyParts[0];
                String protocol = keyParts[1];
                writer.printf("%s,%s,%d%n", dstport, protocol, entry.getValue());
            }
        }
    }

}