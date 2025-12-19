int main() {
    int a[5];
    int i;

    for (i = 0; i < 5; i = i + 1) {
        a[i] = i * 2;
    }

    for (i = 0; i < 5; i = i + 1) {
        print_int(a[i]);
        println();
    }

    return 0;
}