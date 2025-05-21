# Multiplication Optimization in ARM64 Assembly

## Description

This project automatically generates an ARM64 assembly function `kefel` that multiplies its argument by a constant integer `k`, **without using the multiply instruction**. Instead, it uses only bit shifts (`lsl`), additions (`add`), and subtractions (`sub`), producing an optimized multiplication routine.

The generator (`Kefel.java`) creates the `kefel.s` assembly file, which can be compiled and tested using a provided C test program.

## Important Principle

**NOTE:**  
- The generated assembly file `kefel.s` is valid **only for the specific value of `k`** you choose at generation time (for example, `k=14`). It can only multiply by this constant.
- If you want to test another multiplier (for example, `k=4`), you **must** re-run `java Kefel 4` to generate a new `kefel.s` and then recompile your test program.  
- **If you do not regenerate and recompile, the results will be incorrect.**

## Files

- `Kefel.java` : Java generator for the optimized assembly code.
- `kefel.s`   : Assembly file generated for your chosen multiplier `k`.
- `test.c`    : C test file to validate the function.
- `README.md` : This help file.


## Full Compilation and Usage

1. **Compile the Java generator:**
   ```bash
   javac Kefel.java
   ```

2. **Generate the assembly code for k=14 (for example):**
   ```bash
   java Kefel 14
   ```

3. **Compile the assembly code with the C test program:**
   ```bash
   gcc test.c kefel.s -o test
   ```

4. **Run the test:**
   ```bash
   ./test
   ```

5. **IMPORTANT:**  
   When running the test program, you must enter the **same value of `k`** that you used to generate `kefel.s` (for example, if you did `java Kefel 14`, you should enter `14` in the test program).

---

## Example

```bash
javac Kefel.java
java Kefel 14
gcc test.c kefel.s -o test
./test

Enter k and x: 14 10

Using k * x:
14 * 10 = 140

Using kefel(10):
14 * 10 = 140
```

---

## Notes

- To change the multiplier, repeat the process starting from `java Kefel <k>`, then recompile with `gcc`.
- This program works **only on ARM64 systems** (e.g., Raspberry Pi, Mac M1/M2 running Linux ARM, ARM-based VM, etc.).

---

## Authors

- NatanePartouche
