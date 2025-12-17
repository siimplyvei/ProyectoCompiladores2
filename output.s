.data
__strbuf: .space 256
.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
li $t0, 7
sw $t0, -4($sp)
addi $t1, $sp, -8
sw $t2, 0($t1)
addi $t3, $sp, -8
lw $t4, 0($t3)
addi $t5, $sp, -12
sw $t4, 0($t5)
addi $t6, $sp, -12
lw $t7, 0($t6)
move $v0, $t7
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
__read_string:
# $a0 = buffer
# $a1 = length
li $v0, 8
syscall
jr $ra
