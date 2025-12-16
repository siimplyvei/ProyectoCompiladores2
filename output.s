.data
.text
.globl main
main:
addi $sp, $sp, -32
sw $ra, 28($sp)
addi $t0, $sp, -4
li $t1, 7
sw $t1, 0($t0)
addi $t2, $sp, -4
lw $t3, 0($t2)
move $v0, $t3
li $v0, 10
syscall
