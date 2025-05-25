# Kefel

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)

## Table of Contents

1. [Overview](#overview)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Build & Generate](#build--generate)
5. [Assembly & Linking](#assembly--linking)
6. [Usage Example](#usage-example)
7. [Repository Structure](#repository-structure)
8. [Performance Notes](#performance-notes)
9. [Contributing](#contributing)
10. [License](#license)

## Overview

`Kefel` is a Java utility that auto-generates an optimized x86-64 assembly routine named `kefel`. It multiplies a 64-bit integer (`%rdi`) by a constant `k` using the minimum possible instruction count.

## Features

* **Zero & Power-of-Two**: Directly returns 0 or applies a single shift when appropriate.
* **Two-Term Optimization**: Detects if `k = 2^a ± 2^b` and emits two shifts plus one add/subtract.
* **Bit Decomposition**: Decomposes any integer into sum of powers of two for generic optimization.
* **Standalone Generator**: Produces `kefel.s` without external dependencies beyond JDK and GNU toolchain.

## Prerequisites

* **Java**: JDK 8 or later
* **Assembler & Linker**: GNU `as` and `ld`, or `gcc`

## Build & Generate

```bash
# Compile the Java generator
javac Kefel.java

# Generate assembly for k = 42 (as an example)
java Kefel 42
# Output: kefel.s
```

## Assembly & Linking

```bash
# Assemble --> object file
as --64 -o kefel.o kefel.s

# Create shared library
ld -shared -o libkefel.so kefel.o
# Or with gcc:
gcc -shared -fPIC -o libkefel.so kefel.o
```

## Usage Example

1. **Sample C driver** (`test_kefel.c`):

   ```c
   #include <stdio.h>
   extern long kefel(long x);
   int main() {
       long x = 7;
       printf("kefel(%ld) = %ld\n", x, kefel(x));
       return 0;
   }
   ```
2. **Compile & Link**

   ```bash
   gcc -o test_kefel test_kefel.c -L. -lkefel -Wl,-rpath,.
   ./test_kefel
   # Output: kefel(7) = 294
   ```

## Repository Structure

```
├── Kefel.java       # Java source for the assembly generator
├── test.c           # C sample driver
└── README.md        # This documentation
```

## Performance Notes

* For large `k`, two-term optimizations reduce instruction count significantly compared to naive loops.
* Single-shift cases incur only 1 instruction (best case).
* Fall-back bit decomposition uses at most `popcount(k)` additions plus shifts.

## Contributing

1. Fork the repo
2. Create a new branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add feature..."`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request with description and tests.

## License

Distributed under the MIT License. See [LICENSE](LICENSE) for details.
