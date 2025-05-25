import java.io.FileWriter;
import java.io.IOException;

/**
 * Kefel.java
 * -------------------------
 * This program generates an optimized x86-64 assembly routine named `kefel`
 * that multiplies the input parameter (in %rdi) by a constant integer k,
 * using the fewest instructions possible. It writes the resulting assembly
 * code to a file called `kefel.s`.
 *
 * Detailed Algorithm:
 * 1. Handle special cases:
 *    - If k == 0, always return 0 in %rax.
 *    - If k is a power of two, perform a single left-shift on the input.
 *
 * 2. Search for two-term optimizations:
 *    - Try to express k as (2^a - 2^b). If found, generate two shifts
 *      and a subtraction: `movq %rdi,%rax; shlq $a,%rax; movq %rdi,%rcx;
 *      shlq $b,%rcx; subq %rcx,%rax; ret`.
 *    - Try to express k as (2^a + 2^b). If found, generate two shifts
 *      and an addition.
 *
 * 3. Fallback: decompose k into a sum of powers of two bits.
 *    - Iterate over each bit of k from highest to lowest.
 *    - For the first set bit, load and shift into %rax.
 *    - For each subsequent bit, compute in %rcx and add to %rax.
 *
 * This approach minimizes instruction count by preferring single-shift
 * operations and combining terms when possible.
 */
public class Kefel {
    public static void main(String[] args) throws IOException {
        // 1. Verify that exactly one argument (the multiplier k) is provided
        if (args.length != 1) {
            System.out.println("Usage: java Kefel <multiplier>");
            return;
        }

        // 2. Parse the multiplier value from the command line
        int k = Integer.parseInt(args[0]);

        // 3. Build the assembly code in a StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append(".section .text\n.globl kefel\nkefel: ");

        // 4. Case: k == 0 --> produce `movq $0,%rax; ret`
        if (k == 0) {
            sb.append("movq $0, %rax; ret\n");

        // 5. Case: k is a power of two --> single shift by log2(k)
        } else if ((k & (k - 1)) == 0) {
            int shift = Integer.numberOfTrailingZeros(k);
            sb.append("movq %rdi, %rax; shlq $" + shift + ", %rax; ret\n");

        } else {
            StringBuilder best = null;

            // 6. Try to represent k as 2^a - 2^b
            for (int a = 1; a < 64; a++) {
                for (int b = 0; b < a; b++) {
                    if ((1L << a) - (1L << b) == k) {
                        // Construct two-shift and subtract sequence
                        StringBuilder temp = new StringBuilder();
                        temp.append("movq %rdi, %rax; shlq $" + a + ", %rax; ");
                        temp.append("movq %rdi, %rcx; shlq $" + b + ", %rcx; ");
                        temp.append("subq %rcx, %rax; ret\n");
                        best = temp;
                    }
                }
            }

            // 7. Try to represent k as 2^a + 2^b
            if (best == null) {
                for (int a = 0; a < 64; a++) {
                    for (int b = 0; b < 64; b++) {
                        if (a == b) continue;
                        if ((1L << a) + (1L << b) == k) {
                            // Construct two-shift and add sequence
                            StringBuilder temp = new StringBuilder();
                            temp.append("movq %rdi, %rax;");
                            if (a != 0) temp.append(" shlq $" + a + ", %rax;");
                            temp.append(" movq %rdi, %rcx;");
                            if (b != 0) temp.append(" shlq $" + b + ", %rcx;");
                            temp.append(" addq %rcx, %rax; ret\n");
                            best = temp;
                        }
                    }
                }
            }

            // 8. Use the two-term optimization if found
            if (best != null) {
                sb.append(best);

            } else {
                // 9. Fallback: decompose k into bits
                boolean first = true;
                for (int i = 63; i >= 0; i--) {
                    if (((k >> i) & 1) == 1) {
                        if (first) {
                            sb.append("movq %rdi, %rax;");
                            if (i != 0) sb.append(" shlq $" + i + ", %rax;");
                            first = false;
                        } else {
                            sb.append(" movq %rdi, %rcx;");
                            if (i != 0) sb.append(" shlq $" + i + ", %rcx;");
                            sb.append(" addq %rcx, %rax;");
                        }
                    }
                }
                sb.append(" ret\n");
            }
        }

        // 10. Write the generated assembly code to kefel.s
        try (FileWriter writer = new FileWriter("kefel.s")) {
            writer.write(sb.toString());
        }
    }
}