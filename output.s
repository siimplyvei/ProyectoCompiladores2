.data
a: .space 48
g: .space 4
.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
la $t0, g
li $t1, 1
sw $t1, 0($t0)
li $t2, 1
li $t3, 4
mul $t4, $t2, $t3
li $t5, 2
add $t6, $t4, $t5
li $t7, 4
mul $t8, $t6, $t7
la $t9, a
add $t0, $t9, $t8
li $t1, 5
sw $t1, 0($t0)
la $t2, g
lw $t3, 0($t2)
move $v0, $t3
li $v0, 10
syscall
