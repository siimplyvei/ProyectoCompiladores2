int m[10][10];

void fill(int x) {
    int i;
    int j;
    int val;
    val = 1;
    for (i = 1; i <= x; i = i + 1) {
        for (j = 1; j <= x; j = j + 1) {
            m[i][j] = (val * 3 + 7) % 20;
            val = val + 1;
        }
    }
}

int main() {
    int i;
    int j;
    fill(5);

    for (i = 1; i <= 5; i = i + 1) {
        for (j = 1; j <= 5; j = j + 1) {
            print_int(m[i][j]);
            print_str(" ");
        }
        println();
    }
    return 0;
}
