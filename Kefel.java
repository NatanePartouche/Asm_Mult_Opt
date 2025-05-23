import java.io.FileWriter;
import java.io.IOException;

/**
 *  Link to my GitHub project on optimized assembly multiplication:
 *       https://github.com/NatanePartouche/Asm_Mult_Opt.git
 */

/**
 * Kefel.java
 *
 * Generates optimized ARM64 assembly code to multiply an integer by a constant k
 * using only bit shifts (lsl), addition (add), and subtraction (sub) — avoiding
 * costly multiplication instructions.
 *
 * Optimization rules:
 * 1) k with one '1' bit → single shift
 * 2) k with two consecutive '1' bits → sum of two shifts
 * 3) k with three or more consecutive '1' bits → difference of two shifts
 * 4) General case → sum of all shifts (fallback)
 *
 * Usage:
 *   javac Kefel.java
 *   java Kefel <constant k>
 */
public class Kefel {
    public static void main(String[] args) {
        // Ensure exactly one command-line argument (the constant k)
        if (args.length != 1) {
            System.err.println("Usage: java Kefel <integer_constant_k>");
            System.exit(1);
        }

        // Try to parse k as an integer
        int k;
        try {
            k = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid argument: must be an integer.");
            return;
        }

        try (FileWriter out = new FileWriter("kefel.s")) {
            // Write required ARM64 assembly header
            out.write(".text\n");
            out.write(".global kefel\n");
            out.write("kefel:\n");

            // Special case: k == 0 → return 0 directly
            if (k == 0) {
                out.write("    mov x0, #0    // 0 * x = 0\n");
                out.write("    ret\n");
                return;
            }

            // Special case: k == 1 → return x directly (already in x0)
            if (k == 1) {
                out.write("    ret    // 1 * x = x\n");
                return;
            }

            // Convert k to binary string to analyze bit patterns
            String bin = Integer.toBinaryString(k);

            // Determine the maximum number of consecutive '1's in the binary string
            int maxRunLen = 0, current = 0;
            for (char c : bin.toCharArray()) {
                if (c == '1') {
                    current++;
                    maxRunLen = Math.max(maxRunLen, current);
                } else {
                    current = 0;
                }
            }

            // Rule 3: If k has a run of ≥3 consecutive '1's → use subtraction
            if (maxRunLen >= 3) {
                int startIdx = -1, endIdx = -1;
                current = 0;
                for (int i = 0; i < bin.length(); i++) {
                    if (bin.charAt(i) == '1') {
                        current++;
                        if (current == 1) startIdx = i;
                        if (current == maxRunLen) {
                            endIdx = i;
                            break;
                        }
                    } else {
                        current = 0;
                    }
                }

                // Compute shift amounts from binary positions
                int highShift = bin.length() - startIdx;
                int lowShift  = bin.length() - endIdx - 1;

                // Generate code: (x << high) - (x << low)
                out.write("    mov x1, x0    // prepare x for subtraction\n");
                out.write(String.format("    lsl x0, x0, #%d    // x * 2^%d\n", highShift, highShift));
                if (lowShift != 0) {
                    out.write(String.format("    lsl x1, x1, #%d    // x * 2^%d\n", lowShift, lowShift));
                }
                out.write("    sub x0, x0, x1    // (x << high) - (x << low)\n");
                out.write("    ret\n");
                return;
            }

            // Rule 2: If k has exactly two consecutive '1's → use addition
            if (maxRunLen == 2) {
                int pos    = bin.indexOf("11");
                int shift1 = bin.length() - pos - 1; // leftmost bit
                int shift2 = bin.length() - pos - 2; // rightmost bit

                // Generate code: (x << shift1) + (x << shift2)
                out.write("    mov x1, x0    // first shift term\n");
                out.write(String.format("    lsl x1, x1, #%d    // x * 2^%d\n", shift1, shift1));
                out.write("    mov x2, x0    // second shift term\n");
                if (shift2 != 0) {
                    out.write(String.format("    lsl x2, x2, #%d    // x * 2^%d\n", shift2, shift2));
                }
                out.write("    add x0, x1, x2    // sum of two shifts\n");
                out.write("    ret\n");
                return;
            }

            // Rule 1: If k is a power of 2 (only one '1' bit) → use single shift
            if (Integer.bitCount(k) == 1) {
                int shift = Integer.numberOfTrailingZeros(k);
                if (shift != 0) {
                    out.write(String.format("    lsl x0, x0, #%d    // x * 2^%d\n", shift, shift));
                }
                out.write("    ret\n");
                return;
            }

            // Rule 4 (Fallback): General case — sum of all shifts
            boolean firstTerm = true;
            out.write("    mov x3, x0    // save original x\n");

            // Go through all bits of k and generate (x << bit) for each '1'
            for (int bit = 31; bit >= 0; bit--) {
                if (((k >> bit) & 1) == 1) {
                    if (firstTerm) {
                        out.write("    mov x0, x3    // first term\n");
                        if (bit != 0) {
                            out.write(String.format("    lsl x0, x0, #%d    // x * 2^%d\n", bit, bit));
                        }
                        firstTerm = false;
                    } else {
                        out.write("    mov x1, x3    // next term\n");
                        if (bit != 0) {
                            out.write(String.format("    lsl x1, x1, #%d    // x * 2^%d\n", bit, bit));
                        }
                        out.write("    add x0, x0, x1    // accumulate term\n");
                    }
                }
            }
            out.write("    ret\n");

        } catch (IOException e) {
            // If writing to kefel.s fails
            System.err.println("Error creating kefel.s: " + e.getMessage());
        }
    }
}
