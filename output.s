.data
.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
jal __read_int
move $t0, $v0
addi $t1, $sp, -4
sw $t0, 0($t1)
addi $t2, $sp, -4
lw $t3, 0($t2)
move $a0, $t3
jal __print_int
move $t4, $v0
li $t5, 0
move $v0, $t5
li $v0, 10
syscall

__print_int:
li $v0, 1
syscall
jr $ra
__print_char:
li $v0, 11
syscall
jr $ra
__print_string:
li $v0, 4
syscall
jr $ra
__read_int:
li $v0, 5
syscall
jr $ra
__read_char:
li $v0, 12
syscall
jr $ra
