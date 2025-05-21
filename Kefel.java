import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Kefel.java
 *
 * ARM64 assembly code generator for optimized multiplication without using the 'mul' instruction.
 * Automatically produces a kefel.s file that contains an assembly function able to compute k * x
 * with a minimal number of instructions (shifts, additions, subtractions).
 *
 * General operation:
 * - For a power-of-two multiplier (k = 2^n), uses a single shift.
 * - Otherwise, decomposes k into a sum and/or difference of powers of two
 *   to minimize the number of assembly instructions.
 * - Exclusively compatible with ARM64 (AArch64).
 *
 * Author: Natane Partouche
 * Date: 22/05/2025
 */
public class Kefel {

    /**
     * Represents a single term in the decomposition:
     * "x << shift", either to be added or subtracted.
     * For example, for k = 14: 14*x = (x << 4) - (x << 1) - x
     * This would result in three terms, some positive, some negative.
     */
    static class Term {
        int shift;           // How many bits to shift left (equals multiplying by 2^shift)
        boolean isNegative;  // true if this term is to be subtracted, false otherwise

        Term(int shift, boolean isNegative) {
            this.shift = shift;
            this.isNegative = isNegative;
        }
    }

    /**
     * Finds the most compact decomposition (with the fewest instructions) of k
     * into powers of two, allowing subtractions (e.g., 14 = 16 - 2)
     * and not just the classic binary sum (e.g., 14 = 8 + 4 + 2).
     *
     * @param k The integer multiplier
     * @return An ordered list of Term objects describing the optimal combination
     */
    private static List<Term> decomposeOptimally(int k) {
        // First attempt: simple binary sum (bit decomposition)
        List<Term> best = decomposeAsSum(k);

        // Possible improvement: try 2^m - remainder, which may be shorter using subtractions.
        // (e.g., 15 = 16 - 1, so (x << 4) - x)
        for (int m = 1; m <= 31; m++) {
            int pow2 = 1 << m;        // 2 to the power of m
            int delta = pow2 - k;     // The "remainder" to subtract
            if (delta > 0) {
                List<Term> temp = new ArrayList<>();
                temp.add(new Term(m, false));             // x << m (positive)
                temp.addAll(decomposeAsSum(delta));       // other terms

                // All the "remainder" terms are negative (to subtract)
                for (int i = 1; i < temp.size(); i++) {
                    temp.get(i).isNegative = true;
                }
                // If this decomposition is shorter, keep it
                if (temp.size() < best.size()) {
                    best = temp;
                }
            }
        }
        return best;
    }

    /**
     * Decomposes an integer as a sum of powers of two, using its binary representation.
     * Each '1' bit generates a positive term.
     * Example: 13 = 1101₂ → [x<<3, x<<2, x<<0]
     */
    private static List<Term> decomposeAsSum(int n) {
        List<Term> result = new ArrayList<>();
        for (int i = 31; i >= 0; i--) {
            if (((n >> i) & 1) == 1) {
                result.add(new Term(i, false)); // Positive term corresponding to x << i
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // Check number of arguments (must be 1: the value of k)
        if (args.length != 1) {
            System.out.println("Usage: java Kefel <k>");
            return;
        }

        // Convert input text argument to integer (implicit error handling via NumberFormatException)
        int k = Integer.parseInt(args[0]);
        if (k <= 0) {
            System.out.println("k must be a strictly positive integer.");
            return;
        }

        // CASE 1: If k is a power of two, use a single shift (optimal).
        // This is the fastest and most efficient possible solution.
        if ((k & (k - 1)) == 0) {
            int shift = Integer.numberOfTrailingZeros(k); // E.g., k=8 -> shift=3 (since 8=2^3)
            try (FileWriter writer = new FileWriter("kefel.s")) {
                writer.write(".text\n");
                writer.write(".globl kefel\n");
                writer.write(".type kefel, %function\n");
                writer.write("kefel:\n");
                writer.write("    lsl x0, x0, #" + shift + "\n"); // x0 = x0 << shift
                writer.write("    ret\n");
                System.out.println("✅ kefel.s ARM64 file generated for power of two k = " + k);
            } catch (IOException e) {
                System.err.println("File write error: " + e.getMessage());
            }
            return;
        }

        // CASE 2: For arbitrary k > 0, decompose k optimally into shifts/adds/subs.
        List<Term> terms = decomposeOptimally(k);

        try (FileWriter writer = new FileWriter("kefel.s")) {
            writer.write(".text\n");
            writer.write(".globl kefel\n");
            writer.write(".type kefel, %function\n");
            writer.write("kefel:\n");

            // Save the original value (x0) in x2 for multiple shift operations starting from x
            writer.write("    mov x2, x0\n");

            boolean firstTerm = true;
            // For each term, construct the assembly:
            // - The first positive term initializes x0 (the result)
            // - The others are added or subtracted as appropriate
            for (Term term : terms) {
                // Prepare x1 = x << shift (or just x if shift=0)
                if (term.shift == 0) {
                    writer.write("    mov x1, x2\n"); // x1 = x
                } else {
                    writer.write("    lsl x1, x2, #" + term.shift + "\n"); // x1 = x << shift
                }

                if (firstTerm && !term.isNegative) {
                    // First positive term: initializes the result
                    writer.write("    mov x0, x1\n");
                    firstTerm = false;
                } else if (term.isNegative) {
                    // Negative term: subtract from current result
                    writer.write("    sub x0, x0, x1\n");
                } else {
                    // Otherwise, add to the result
                    writer.write("    add x0, x0, x1\n");
                }
            }

            writer.write("    ret\n"); // Return result in x0
            System.out.println("✅ kefel.s ARM64 file generated and optimized for k = " + k);
        } catch (IOException e) {
            System.err.println("File write error: " + e.getMessage());
        }
    }
}