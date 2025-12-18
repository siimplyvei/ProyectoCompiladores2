int main() {
    int x, sum = 0;

    print_str("Ingrese numeros (0 para terminar):\n");

    x = read_int();
    while (x != 0) {
        sum = sum + x;
        x = read_int();
    }

    print_str("La suma es: ");
    print_int(sum);
    println();

    return 0;
}
