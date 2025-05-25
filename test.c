/**
 * test_kefel.c
 * -------------------------
 * C test driver for the external `kefel` assembly routine.
 * This program prompts the user to enter a multiplier `k` and a value `x`,
 * then prints two results:
 *   1. The product computed by the built-in C multiplication (`k * x`).
 *   2. The product computed by calling the `kefel` function (optimized assembly).
 */

#include <stdio.h>

// Declare the external assembly function 'kefel' that multiplies its argument by k.
extern int kefel(int);

int main(void)
{
    int k, x;

    // Prompt the user to input both the multiplier (k) and the operand (x).
    printf("Enter k and x: ");
    scanf("%d %d", &k, &x);

    // 1) Compute and display the result using standard C multiplication.
    printf("\nUsing k * x:\n");
    printf("%d * %d = %d\n", k, x, k * x);

    // 2) Compute and display the result using the optimized 'kefel' routine.
    //    Note: The `kefel` function must be linked from the generated assembly.
    printf("\nUsing kefel(%d):\n", x);
    printf("%d * %d = %d\n", k, x, kefel(x));

    // Return 0 to indicate successful execution.
    return 0;
}
