.data
__strbuf: .space 256
.str1: .asciiz "La suma es: "
.str0: .asciiz "Ingrese numeros (0 para terminar):\n"
.text
.globl main
j main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
sw $a0, 0($sp)
sw $a1, 4($sp)
sw $a2, 8($sp)
sw $a3, 12($sp)
addi $t0, $sp, -4
li $t2, 0
sw $t2, 0($t0)
la $a0, .str0
jal __print_string
jal __read_int
move $t3, $v0
addi $t4, $sp, 0
sw $t3, 0($t4)
L1:
addi $t5, $sp, 0
lw $t6, 0($t5)
li $t8, 0
sub $t9, $t6, $t8
sltu $t9, $zero, $t9
beq $t9, $zero, L2
addi $t0, $sp, -4
lw $t1, 0($t0)
addi $t2, $sp, 0
lw $t3, 0($t2)
add $t4, $t1, $t3
addi $t5, $sp, -4
sw $t4, 0($t5)
jal __read_int
move $t6, $v0
addi $t7, $sp, 0
sw $t6, 0($t7)
j L1
L2:
la $a0, .str1
jal __print_string
addi $t8, $sp, -4
lw $t9, 0($t8)
move $a0, $t9
jal __print_int
jal __print_newline
li $t1, 0
move $v0, $t1
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
__print_newline:
li $a0, 10
li $v0, 11
syscall
jr $ra
