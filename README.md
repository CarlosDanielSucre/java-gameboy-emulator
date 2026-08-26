# java-gameboy-emulator
Game Boy emulator built from scratch in Java: CPU, memory, PPU, and more, implemented for fun


### Project Structure

```
1. Cartridge
   └── carregar ROM
   └── readByte()

2. MMU
   └── mapear endereços
   └── readByte()
   └── writeByte()

3. CPU fetch
   └── mmu.readByte(PC)
   └── PC++

4. CPU decode
   └── opcode → instruction lookup

5. Execute
   └── instruction.execute()

6. Flags
   └── atualizar Z/N/H/C conforme instrução
```