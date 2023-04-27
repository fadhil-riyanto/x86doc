import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    static final String filenameOut = ".\\signature.txt";
    static final String indexFileOut = ".\\index.html";
    static final String sourcePath = "..\\html";
    static final String fileNameHHP = "..\\html\\main.hhp";
    static final String fileNameHHC = "..\\html\\main.hhc";
    static final String fileNameHHK = "..\\html\\main.hhk";
    static final String fileNameIndex = "..\\html\\index.html";

    public static void main(String[] args) {
        new Main().convertHtml_toTabSeparated();
    }

    @SuppressWarnings("unused")
    private void convertHtml_toTabSeparated() {

        final File folder = new File(sourcePath);
        if (!folder.isDirectory()) System.out.println("folder " + sourcePath + " does not exist");

        final Collection<Signature> allSignatures = new TreeSet<Signature>();

        final StringBuilder sb = new StringBuilder();
        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sb.append("; This file is machine generated at " + dateFormat.format(new Date()) + "\n;\n");

        //final List<String> OpcodeList = new ArrayList<>();
        final SortedMap<String, String> OpcodeMap = new TreeMap<String, String>();
        final SortedMap<String, String> indexContent = new TreeMap<String, String>();


        for (final File fileEntry : folder.listFiles()) {
            final String ref = fileEntry.getName();
            System.out.println(ref);
            if (fileEntry.isDirectory()) {
                // do nothing
            } else if (ref.equalsIgnoreCase("index.html")) {
                // do nothing
            } else if (ref.endsWith(".html")) {
                try {
                    final String fileContent = new String(Files.readAllBytes(Paths.get(fileEntry.getAbsolutePath()))).replace("\r", " ").replace("\n", " ");
                    final Set<String> instructions = this.retieveInstructions(stripExtension(fileEntry.getName()), fileContent);

                    final SortedSet<String> archs = new TreeSet<String>();

                    for (final Signature entry : this.getSignatures(fileContent, instructions)) {
                        //log.info("instruction="+entry.mnemonic+"("+entry.signature +") ["+entry.cpuFlags+"] description="+entry.description);
                        allSignatures.add(entry);
                        for (final String arch : entry.cpuFlags.split(",")) {
                            archs.add(arch.trim());
                        }
                    }

                    final String general_description_string = this.extractGeneralDescription(fileContent);
                    for (final String instruction : instructions) {
                        sb.append("GENERAL\t" + instruction + '\t' + general_description_string + '\t' + ref + "\n");

                        final StringBuilder arch = new StringBuilder();
                        for (String str : archs) {
                            arch.append(str);
                            arch.append(", ");
                        }
                        if (arch.length() > 1) arch.setLength(arch.length() - 2);

                        indexContent.put(instruction, "<tr><td><a href=\"/x86doc/" + ref + "\">" + instruction + "</a></td><td>" + general_description_string + "</td><td>" + arch + "</td></tr>");
                        OpcodeMap.put(instruction, ref);
                        //OpcodeList.add(instruction);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //Collections.sort(OpcodeList);

        final StringBuilder indexFileContent = new StringBuilder();
        indexFileContent.append("<!DOCTYPE html><html><head><title>x86/x64 Instruction Set Reference</title>\n");
        indexFileContent.append("<meta charset=\"UTF-8\"><link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\"></head>\n");
        indexFileContent.append("<body>\n");
        indexFileContent.append("<div id=\"body\">\n");
        indexFileContent.append("<h1>x86/x64 Instruction Set Reference</h1>\n");
        indexFileContent.append("<table>\n");
        indexFileContent.append("<tr><th>Instruction</th><th>Description</th><th>CPUID Feature Flag</th></tr>\n");
        for (final String mnemonic : indexContent.keySet()) {
            indexFileContent.append(indexContent.get(mnemonic) + "\n");
        }
        indexFileContent.append("</table></div>\n</body></html>");

        final SortedSet<String> archs = new TreeSet<String>();
        final SortedSet<String> operandTypes = new TreeSet<String>();

        for (final Signature entry : allSignatures) {

            final String cpuFlags = entry.cpuFlags;
            sb.append(entry.mnemonic + '\t' + entry.operands + '\t' + cpuFlags + '\t' + entry.mnemonic + " " + entry.operandsDoc + '\t' + cleanupDescription(entry.description) + "\n");

            for (final String arch : entry.cpuFlags.split(",")) {
                archs.add(arch);
            }
            for (final String operandType : entry.operands.split(",")) {
                operandTypes.add(operandType);
            }
        }

        if (false) {
            for (final String arch : archs) {
                System.out.println("arch " + arch);
            }
            for (final String operandType : operandTypes) {
                System.out.println("operandType " + operandType);
            }
        }
        try {
            PrintWriter out = new PrintWriter(indexFileOut);
            out.print(indexFileContent);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            PrintWriter out = new PrintWriter(filenameOut);
            out.print(sb);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // write main.hhp
        try {
            PrintWriter out = new PrintWriter(fileNameHHP);
            out.print("[OPTIONS]\n");
            out.print("Compatibility=1.1 or later\n");
            out.print("Compiled file=x86doc.chm\n");
            out.print("Contents file=main.hhc\n");
            out.print("Default topic=index.html\n");
            out.print("Display compile notes=No\n");
            out.print("Display compile progress=No\n");
            out.print("Full-text search=Yes\n");
            out.print("Create CHI file=Yes\n"); // Create .chi
            out.print("Index file=main.hhk\n");
            out.print("Language=0x409 English (United States)\n");
            out.print("Title=x86/x64 Instruction Set Reference\n");
            out.print("\n\n[INFOTYPES]\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
  
        // write main.hhc
        try {
            PrintWriter out = new PrintWriter(fileNameHHC);
            out.print("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n");
            out.print("<HTML>\n<HEAD>\n");
            out.print("<meta name=\"GENERATOR\" content=\"Microsoft&reg; HTML Help Workshop 4.1\">\n");
            out.print("<!-- Sitemap 1.0 -->\n");
            out.print("</HEAD><BODY>\n");
            //out.print("<OBJECT type=\"text/site properties\">\n");
            out.print("<UL>\n");
            
            out.print("<LI> <OBJECT type=\"text/sitemap\">\n");
            out.print("\t<param name=\"Name\" value=\"x86/x64 Instruction Set Reference\">\n");
            out.print("\t<param name=\"Local\" value=\"index.html\">\n");
            out.print("\t</OBJECT>\n");
            out.print("<UL>\n");
            for (final String o : OpcodeMap.keySet()) {
                out.print("<LI> <OBJECT type=\"text/sitemap\">\n");
                out.print("<param name=\"Name\" value=\""+o+"\">\n");
                out.print("<param name=\"Local\" value=\""+OpcodeMap.get(o)+"\">\n");
                out.print("</OBJECT>\n");
            }
            out.print("</UL>\n");
            
            out.print("</UL>\n");
            out.print("</BODY></HTML>\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write main.hhk
        try {
            PrintWriter out = new PrintWriter(fileNameHHK);
            out.print("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">\n");
            out.print("<HTML>\n<HEAD>\n");
            out.print("<meta name=\"GENERATOR\" content=\"Microsoft&reg; HTML Help Workshop 4.1\">\n");
            out.print("<!-- Sitemap 1.0 -->\n");
            out.print("</HEAD><BODY>\n");
            out.print("<UL>\n");
            for (final String o : OpcodeMap.keySet()) {
                out.print("<LI> <OBJECT type=\"text/sitemap\">\n");
                out.print("<param name=\"Name\" value=\""+o+"\">\n");
                out.print("<param name=\"Name\" value=\""+o+"\">\n");
                out.print("<param name=\"Local\" value=\""+OpcodeMap.get(o)+"\">\n");
                out.print("</OBJECT>\n");
            }
            out.print("</UL>\n");
            out.print("</BODY></HTML>\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try {
            PrintWriter out = new PrintWriter(fileNameIndex);
            out.print(indexFileContent.toString().replace("/x86doc/", ""));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("DONE");
    }

    private List<Signature> getSignatures(final String str, final Set<String> mnemonics) {
        try {
            final String beginKeyword = "<table>";
            final String endKeyword = "</table>";

            final int begin = str.indexOf(beginKeyword);
            final int end = str.indexOf(endKeyword);

            if ((begin == -1) || (end == -1)) {
                System.out.println("getSignatures: could not find table for mnemonic " + mnemonics.iterator().next() + "; str begin=" + begin + "; end=" + end);
                return new ArrayList<Signature>();
            }

            final String str2 = str.substring(begin, end);
            //System.out.println("getSignatures: str begin="+begin+"; end="+end +"; str2="+str2);
            final String beginKeywordTr = "<tr>";
            final String endKeywordTr = "</tr>";
            int pos = 0;

            final List<List<String>> rawData = new ArrayList<List<String>>();

            boolean header = true;
            while (pos < str2.length()) {
                final int begin2 = str2.indexOf(beginKeywordTr, pos);
                final int end2 = str2.indexOf(endKeywordTr, pos);

                if ((begin2 == -1) || (end2 == -1)) {
                    break;
                } else {
                    int pos2 = begin2 + beginKeywordTr.length();
                    if (pos2 <= end2) {
                        rawData.add(this.splitTableEntry(str2.substring(pos2, end2), header));
                    }
                    pos = end2 + endKeywordTr.length();

                    if (header) header = false;
                }
            }
            return this.transformToSignatures(rawData, mnemonics);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getSignatures: Exception e=" + e.toString());
            return new ArrayList<Signature>();
        }
    }

    private List<String> splitTableEntry(final String str, boolean header) {
        String beginKeyword;
        String endKeyword;
        if (header) {
            beginKeyword = "<th>";
            endKeyword = "</th>";
        } else {
            beginKeyword = "<td>";
            endKeyword = "</td>";
        }
        List<String> list = new ArrayList<String>();

        int startPos = 0;
        while (startPos < str.length()) {
            final int begin2 = str.indexOf(beginKeyword, startPos);
            final int end2 = str.indexOf(endKeyword, startPos);

            if ((end2 == -1) || (begin2 == -1)) {
                break;
            } else {
                String raw = removeHtml(str.substring(begin2 + beginKeyword.length(), end2));
                if (raw.contains("*")) {
                    raw = raw.replaceAll("\\*", "");
                    //log.info("splitTableEntry: raw had * "+raw);
                }
                list.add(fixComma(raw));
                startPos = end2 + endKeyword.length();
            }
        }
        return list;
    }

    private List<Signature> transformToSignatures(final List<List<String>> rawData, final Set<String> mnemonics) {
        final int nElements = rawData.size();
        //log.info("transformToSignatures: mnemonic "+mnemonics.iterator().next() +"; nElements="+nElements);

        // scan the first element with header info
        if (nElements == 0) {
            System.out.println("transform: got empty rawData for mnemonic " + mnemonics.iterator().next());
            return new ArrayList<Signature>();
        } else {
            final List<Signature> list = new ArrayList<Signature>(nElements);
            final List<String> headers = rawData.get(0);
            if (headers.size() < 1) {
                System.out.println("transform: got empty header for mnemonic " + mnemonics.iterator().next());
            } else {

                String mnemonic = null;
                String arch = null;
                String operandsDoc = null;
                String description = null;

                for (int i = 0; i < nElements; ++i) {

                    List<String> element = rawData.get(i);
                    List<String> sign = null;

                    if (headers.get(0).equalsIgnoreCase("Opcode") && (headers.size() == 6)) {
                        sign = this.splitInstruction(element.get(1), mnemonics);
                        arch = "";
                        description = element.get(5);
                    } else if ((headers.size() == 5) &&
                            (headers.get(0).equalsIgnoreCase("Opcode/Instruction") || headers.get(0).equalsIgnoreCase("Opcode Instruction"))) {
                        sign = this.splitInstruction(element.get(0), mnemonics);
                        arch = element.get(3);
                        description = element.get(4);
                    } else if ((headers.size() == 4) &&
                            headers.get(0).equalsIgnoreCase("Opcode/Instruction")) {
                        sign = this.splitInstruction(element.get(0), mnemonics);
                        arch = element.get(2);
                        description = element.get(3);
                    } else if ((headers.size() == 5) &&
                            headers.get(0).equalsIgnoreCase("Opcode")) {
                        sign = this.splitInstruction(element.get(1), mnemonics);
                        arch = element.get(3);
                        description = element.get(4);
                    } else if ((headers.size() == 5) &&
                            headers.get(0).equalsIgnoreCase("Description") &&
                            headers.get(4).equalsIgnoreCase("CPUID Feature Flag")) {
                        sign = this.splitInstruction(element.get(1), mnemonics);
                        arch = element.get(4);
                        description = element.get(0);
                    } else if ((headers.size() == 5) &&
                            headers.get(0).equalsIgnoreCase("Description") &&
                            headers.get(2).equalsIgnoreCase("Opcode/Instruction")) {
                        sign = this.splitInstruction(element.get(2), mnemonics);
                        arch = element.get(1);
                        description = element.get(0);
                    } else if ((headers.size() == 5) &&
                            headers.get(0).equalsIgnoreCase("CPUID Feature Flag") &&
                            headers.get(1).equalsIgnoreCase("Description") &&
                            headers.get(2).equalsIgnoreCase("Opcode/Instruction")) {
                        sign = splitInstruction(element.get(2), mnemonics);
                        arch = element.get(0);
                        description = element.get(1);
                    } else if (mnemonics.iterator().next().equals("XORPD") ||
                            mnemonics.iterator().next().equals("UNPCKLPS") ||
                            mnemonics.iterator().next().equals("UNPCKLPD") ||
                            mnemonics.iterator().next().equals("UNPCKHPS") ||
                            mnemonics.iterator().next().equals("UNPCKHPD") ||
                            mnemonics.iterator().next().equals("PTWRITE")) {
                        if (element.size() > 4) {
                            sign = splitInstruction(element.get(0), mnemonics);
                            arch = element.get(3);
                            description = element.get(4);
                        }
                    } else {
                        System.out.println("transform: funky header for mnemonic " + mnemonics.iterator().next() + ";headers.size()=" + headers.size());
                    }
                    if (sign != null) {
                        mnemonic = sign.get(0);
                        operandsDoc = sign.get(1);
                        if (mnemonic.equals("")) {
                            // do nothing
                        } else {
                            String operandsDoc2 = sanitize_SignatureDoc(operandsDoc);
                            list.add(new Signature(mnemonic, makeOperand(operandsDoc2), sanitize_Arch(arch), operandsDoc2, description));
                        }
                    }
                }
            }
            return list;
        }
    }

    private String cleanupOperand(final String str) {
        final StringBuilder sb = new StringBuilder();
        for (final String word : str.toUpperCase().split(",")) {
            String word2 = word.trim();
            if (word2.equals("R32/M161")) {
                word2 = "R32/M16"; // get rid of silly footnote
            }
            word2 = word2.replaceAll("MM128", "M128");
            if (word2.equals("M")) {
                word2 = "MEM";
            }
            sb.append(word2);
            sb.append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private List<String> splitInstruction(final String str, final Set<String> instructions) {

        List<String> list = new ArrayList<String>();
        for (final String instruction : instructions) {

            String[] words = str.split(" ");
            for (int i = 0; i < words.length; ++i) {
                if (words[i].equalsIgnoreCase(instruction)) {
                    list.add(instruction);

                    final StringBuilder operand = new StringBuilder();
                    for (int j = i + 1; j < words.length; ++j) {
                        operand.append(this.cleanupOperand(words[j]) + " ");
                    }
                    list.add(operand.toString());
                    return list;
                }
            }
        }
        list.add("");
        list.add("");
        return list;
    }

    private static String removeHtml(final String str) {
        return str.replaceAll("<em>", "").
                replaceAll("</em>", "").
                replaceAll("</p>", "").
                replaceAll("<p>", "").
                replaceAll("<sup>", "").
                replaceAll("</sup>", "").
                replaceAll("<sub>", "").
                replaceAll("</sub>", "");
    }

    private static String sanitize_Arch(final String arch) {

        //TODO
        if (arch.startsWith("AVX512VL AVX512BW AVX512BW")) return "AVX512VL,AVX512BW";
        if (arch.equalsIgnoreCase("AVX AVX512BW Insert a word integer value from r32/m16 and")) return "AVX,AVX512BW";

        if (arch.equalsIgnoreCase("Both PCL-MULQDQ and AVX flags")) return "PCLMULQDQ,AVX";
        if (arch.equalsIgnoreCase("AVX512B W")) return "AVX512BW";
        if (arch.equalsIgnoreCase("AVX512V L AVX512B W")) return "AVX512VL,AVX512BW";
        if (arch.equalsIgnoreCase("AVX512V L AVX512F")) return "AVX512VL,AVX512F";
        if (arch.equalsIgnoreCase("AVX512D Q")) return "AVX512DQ";
        if (arch.equalsIgnoreCase("Both AES and AVX flags")) return "AES,AVX";
        if (arch.equalsIgnoreCase("N.E.")) return "";
        if (arch.equalsIgnoreCase("Valid")) return "";
        if (arch.equalsIgnoreCase("PCLMUL-QDQ")) return "PCLMULQDQ";
        if (arch.equalsIgnoreCase("HLE1")) return "HLE";
        if (arch.equalsIgnoreCase("HLE or RTM")) return "HLE,RTM";
        return arch.replaceAll(" ", ",");
    }

    private static String sanitize_SignatureDoc(final String str) {
        final StringBuilder sb = new StringBuilder();
        final String str2 = str.toUpperCase().trim();

        for (final String element : str2.split(",")) {
            final String str3 = element.trim();
            String str4 = str3.replaceAll("&LT;XMM0&GT;", "XMM_ZERO").
                    replace("M16&AMP;", "M16&").
                    replace("M32&AMP;", "M32&").
                    replace(".M256", "M256").
                    replace("/ M128", "/M128").
                    replace(" /M256", "/M256").
                    replace(" {", "{");


            if (str4.equals("IMM6")) { // fix a bug
                str4 = "IMM16";
            }
            sb.append(str4);
            sb.append(",");
        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);

        final String str5 = sb.toString();
        //log.info("makeSignatureDoc: str "+str5);
//		return str5;
        return str5.split(" ")[0].trim();
    }

    private static String makeOperand(final String str) {
        //log.info("makeOperand "+str);

        final StringBuilder sb = new StringBuilder();

        for (final String operand : str.toUpperCase().split(",")) {

            String str2 = operand.
                    replaceAll("K1", "K").
                    replaceAll("K2", "K").
                    replaceAll("K3", "K").
                    replaceAll("BND1", "BND").
                    replaceAll("BND2", "BND").

                    replaceAll("R32A", "R32").
                    replaceAll("R32B", "R32").
                    replaceAll("R64A", "R64").
                    replaceAll("R64B", "R64").

                    replaceAll("XMM0", "XMM").
                    replaceAll("XMM1", "XMM").
                    replaceAll("XMM2", "XMM").
                    replaceAll("XMM3", "XMM").
                    replaceAll("XMM4", "XMM").

                    replaceAll("YMM0", "YMM").
                    replaceAll("YMM1", "YMM").
                    replaceAll("YMM2", "YMM").
                    replaceAll("YMM3", "YMM").
                    replaceAll("YMM4", "YMM").

                    replaceAll("ZMM0", "ZMM").
                    replaceAll("ZMM1", "ZMM").
                    replaceAll("ZMM2", "ZMM").
                    replaceAll("ZMM3", "ZMM").
                    replaceAll("ZMM4", "ZMM");

            if (str2.equals("MM1")) str2 = "MM";
            if (str2.equals("MM2")) str2 = "MM";
            if (str2.equals("MM2/M64")) str2 = "MM/M64";
            sb.append(str2);
            sb.append(",");

        }
        if (sb.length() > 0) sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    private String extractGeneralDescription(final String fileContent) {
        final String beginKeyword = "<title>";
        final String endKeyword = "</title>";

        final int startPos = fileContent.indexOf(beginKeyword);
        final int endPos = fileContent.indexOf(endKeyword);

        if ((startPos == -1) || (endPos == -1)) {
            System.out.println("extractGeneralDescription: no title keywords in content=" + fileContent);
            return "";
        }
        try {
            final String title = fileContent.substring(startPos + beginKeyword.length(), endPos);
            final int posHyphen = title.indexOf("-");
            if (posHyphen != -1) {
                return title.substring(posHyphen + 1).trim();
            }
            return title;
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            System.out.println("extractGeneralDescription: content " + fileContent);
            return "";
        }
    }

    private static String stripExtension(String str) {
        if (str == null) return null;
        int pos = str.lastIndexOf(".");
        if (pos == -1) return str;
        return str.substring(0, pos);
    }

    private static String fixComma(final String str) {
        return str.replaceAll(", ", ",").trim();
    }

    private Set<String> retieveInstructions(final String filename, final String fileContent) {
        List<String> result = null;

        if (filename.equalsIgnoreCase("REP_REPE_REPZ_REPNE_REPNZ")) {
            result = Arrays.asList("REP INS", "REP MOVS", "REP OUTS", "REP LODS", "REP STOS", "REPE CMPS", "REPE SCAS", "REPNE CMPS", "REPNE SCAS");
        } else if (filename.equalsIgnoreCase("MOV-1")) {
            result = Arrays.asList("MOV");
        } else if (filename.equalsIgnoreCase("MOV-2")) {
            result = Arrays.asList("MOV");

        } else if (filename.equalsIgnoreCase("VPERMI2W_D_Q_PS_PD")) {
            result = Arrays.asList("VPERMI2W", "VPERMI2D", "VPERMI2Q", "VPERMI2PS", "VPERMI2PD");
        } else if (filename.equalsIgnoreCase("VPTESTNMB_W_D_Q")) {
            result = Arrays.asList("VPTESTNMB", "VPTESTNMW", "VPTESTNMD", "VPTESTNMQ");
        } else if (filename.equalsIgnoreCase("VPLZCNTD_Q")) {
            result = Arrays.asList("VPLZCNTD", "VPLZCNTQ");
        } else if (filename.equalsIgnoreCase("VPCONFLICTD_Q")) {
            result = Arrays.asList("VPCONFLICTD", "VPCONFLICTQ");
        } else if (filename.equalsIgnoreCase("VPBROADCASTB_W_D_Q")) {
            result = Arrays.asList("VPBROADCASTB", "VPBROADCASTW", "VPBROADCASTD", "VPBROADCASTQ");
        } else if (filename.equalsIgnoreCase("MOVDQU,VMOVDQU8_16_32_64")) {
            result = Arrays.asList("MOVDQU", "VMOVDQU8", "VMOVDQU16", "VMOVDQU32", "VMOVDQU64");
        } else if (filename.equalsIgnoreCase("MOVDQA,VMOVDQA32_64")) {
            result = Arrays.asList("MOVDQA", "VMOVDQA32", "VMOVDQA64");

        } else if (filename.equalsIgnoreCase("PREFETCHh")) {
            result = Arrays.asList("PREFETCHT0", "PREFETCHT1", "PREFETCHT2", "PREFETCHNTA");
        } else if (filename.equalsIgnoreCase("POR")) {
            result = Arrays.asList("POR", "VPORD", "VPORQ");
        } else if (filename.equalsIgnoreCase("PXOR")) {
            result = Arrays.asList("PXOR", "VPXOR", "VPXORD", "VPXORQ");
        } else if (filename.equalsIgnoreCase("LOOPcc")) {
            result = Arrays.asList("LOOPE", "LOOPNE");

        } else if (filename.equalsIgnoreCase("PAND")) {
            result = Arrays.asList("PAND", "VPAND", "VPANDD", "VPANDQ");
        } else if (filename.equalsIgnoreCase("PANDN")) {
            result = Arrays.asList("PANDN", "VPANDN", "VPANDND", "VPANDNQ");
        } else if (filename.equalsIgnoreCase("PMOVSX")) {
            result = Arrays.asList("PMOVSXBW", "PMOVSXBD", "PMOVSXBQ", "PMOVSXWD", "PMOVSXWQ", "PMOVSXDQ");
        } else if (filename.equalsIgnoreCase("PMOVZX")) {
            result = Arrays.asList("PMOVZXBW", "PMOVZXBD", "PMOVZXBQ", "PMOVZXWD", "PMOVZXWQ", "PMOVZXDQ");

        } else if (filename.equalsIgnoreCase("VBROADCAST")) {
            result = Arrays.asList("VBROADCASTSS", "VBROADCASTSD", "VBROADCASTF128", "VBROADCASTF32X2", "VBROADCASTF32X4", "VBROADCASTF64X2", "VBROADCASTF32X8", "VBROADCASTF64X4");

        } else if (filename.equalsIgnoreCase("VPBROADCAST")) {
            result = Arrays.asList("VPBROADCASTB", "VPBROADCASTW", "VPBROADCASTD", "VPBROADCASTQ", "VBROADCASTI32x2", "VBROADCASTI64X4", "VBROADCASTI32X8");
        } else if (filename.equalsIgnoreCase("VPBROADCASTM")) {
            result = Arrays.asList("VPBROADCASTMB2Q", "VPBROADCASTMW2D");
        } else if (filename.equalsIgnoreCase("VPBROADCASTMW2D")) {
            result = Arrays.asList("VBROADCASTSS", "VBROADCASTSD", "VBROADCASTF128", "VBROADCASTF32X2", "VBROADCASTF32X4");


        } else if (filename.equalsIgnoreCase("VMASKMOV")) {
            result = Arrays.asList("VMASKMOVPS", "VMASKMOVPD");
        } else if (filename.equalsIgnoreCase("VPMASKMOV")) {
            result = Arrays.asList("VPMASKMOVD", "VPMASKMOVQ");

        } else if (filename.equalsIgnoreCase("CMOVcc")) {
            result = Arrays.asList("CMOVA", "CMOVAE", "CMOVB", "CMOVBE", "CMOVC", "CMOVE", "CMOVG", "CMOVGE", "CMOVL", "CMOVLE", "CMOVNA", "CMOVNAE", "CMOVNB");
        } else if (filename.equalsIgnoreCase("FCMOVcc")) {
            result = Arrays.asList("FCMOVB", "FCMOVE", "FCMOVBE", "FCMOVU", "FCMOVNB", "FCMOVNE", "FCMOVNBE");
        } else if (filename.equalsIgnoreCase("Jcc")) {
            result = Arrays.asList("JA", "JAE", "JB", "JBE", "JC", "JE", "JG", "JGE", "JL", "JLE", "JNA", "JNAE", "JNB", "JNBE", "JNC", "JNE", "JNG", "JNGE", "JNL", "JNLE", "JNO", "JNP", "JNS", "JNZ", "JO", "JP", "JPE", "JPO", "JS", "JZ", "JA", "JCXZ", "JECXZ", "JRCXZ");
        } else if (filename.equalsIgnoreCase("SETcc")) {
            result = Arrays.asList("SETA", "SETAE", "SETB", "SETBE", "SETC", "SETE", "SETG", "SETGE", "SETL", "SETLE", "SETNA", "SETNAE", "SETNB", "SETNBE", "SETNC", "SETNE", "SETNG", "SETNGE", "SETNL", "SETLE", "SETNO", "SETNP", "SETNS", "SETNZ", "SETO", "SETP", "SETPE", "SETPO", "SETS", "SETZ", "SETA");
        } else if (filename.equalsIgnoreCase("LOOP_LOOPcc")) {
            result = Arrays.asList("LOOP", "LOOPE", "LOOPZ", "LOOPNE", "LOOPNZ");
        } else if (filename.equalsIgnoreCase("INT n_INTO_INT 3")) {
            result = Arrays.asList("INT 3", "INT", "INTO");
        } else {
            result = Arrays.asList(filename.split("_"));
        }

        final Set<String> result2 = new HashSet<String>(result);

        for (final String instruction : result) {
            String vStr = "V" + instruction;
            if (fileContent.contains(vStr)) {
                result2.add(vStr);
            }
        }
        return result2;
    }

    private String cleanupDescription(final String str) {
        return str.replaceAll("val-ues", "values").replaceAll("writ-ten", "written").replaceAll(",", ", ").replaceAll(" // ", "//");
    }
}
