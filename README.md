# Java Game Boy Emulator

This project is a Game Boy emulator written in Java as a study of computer architecture, CPU design, memory mapping, peripherals, and hardware emulation. The goal is to learn how an old console works internally instead of only using a ready-made emulator.

It was built to study:

- the Game Boy CPU
- memory organization and address mapping
- interrupts and machine cycles
- PPU rendering and screen updates
- timers and synchronization
- cartridges and ROM handling
- low-level debugging and hardware emulation

## Project Goal

This project is not intended to be a perfect production-grade emulator. Its main purpose is to provide a realistic learning experience about how the Game Boy works internally. This kind of project helps understand areas of computer science that are often hidden behind high-level software abstractions.

## What You Learn by Building a Project Like This

### 1. CPU Architecture

You learn how the CPU:

- fetches instructions from memory
- updates the Program Counter (PC)
- manipulates registers
- updates flags
- executes arithmetic, logical, and data-transfer operations
- performs jumps, calls, and returns
- uses the stack

The Game Boy CPU is based on a Z80-like architecture, which makes it a great platform to study:

- 8-bit and 16-bit registers
- memory addressing modes
- conditional execution
- stack behavior
- Zero, Carry, Half Carry, and Subtract flags
- instruction timing and cycles

### 2. Memory Organization

An emulator must model the Game Boy memory system in a way that reflects the actual hardware:

- cartridge ROM
- RAM
- VRAM
- WRAM
- I/O memory
- special address ranges
- control areas and peripheral mappings

This teaches that memory is not just a generic array. It is organized by region, and each region has a specific function within the system.

### 3. Interrupts

Interrupts are fundamental in embedded systems. In the Game Boy, interrupts can happen due to:

- VBlank
- LCD STAT
- Timer
- Serial
- Joypad

You learn:

- how pending interrupts are detected
- how the processor saves its current state
- how execution is redirected to the correct interrupt vector
- how IME, DI, and EI behave

### 4. PPU and Rendering

The PPU (Picture Processing Unit) is responsible for drawing the screen. Studying it teaches:

- scanlines
- frames
- HBlank and VBlank
- tiles and backgrounds
- sprites and object rendering
- window rendering
- visual priorities
- timing between CPU and display updates

This is one of the most important parts of the project because the Game Boy display is not just a simple buffer; it is a hardware subsystem synchronized with CPU execution and clock timing.

### 5. Timing and Machine Cycles

In real hardware, everything happens in time. Each instruction consumes a number of cycles, and the CPU, timer, and PPU must all cooperate. This introduces concepts such as:

- machine cycles
- synchronization between components
- timing in embedded systems
- debugging using state progression and timeline analysis

### 6. Cartridges and ROMs

Cartridges are more than binary files. They include different memory layouts and mapping behavior. By studying them, you understand:

- how the ROM is read
- how address mapping behaves
- how external RAM works
- how different cartridge types affect memory access

### 7. Low-Level Debugging

Emulator projects require a lot of internal state observation. You learn how to debug:

- register values
- flag states
- PC and SP
- memory contents at specific addresses
- unimplemented instructions
- strange conditional behavior and loops

This develops practical debugging habits that transfer to real software engineering and hardware-related work.

### 8. Reading Technical Specifications

A real emulator depends on technical documentation. This project forces you to interpret:

- instruction tables
- hardware behavior
- memory maps
- interrupt rules
- PPU and timer behavior

This develops the ability to translate specifications into working code.

## Typical Project Structure

A project like this is usually organized into modules such as:

```text
src/
├── cpu/
│   ├── CPU.java
│   ├── Registers.java
│   ├── Flags.java
│   └── Instruction.java
├── memory/
│   ├── MMU.java
│   └── Cartridge.java
├── ppu/
│   └── PPU.java
├── timer/
│   └── Timer.java
├── interrupts/
│   └── InterruptController.java
├── input/
│   └── Joypad.java
├── tests/
│   └── CPUTest.java
└── Main.java
```

### Typical Execution Flow

```text
1. Load ROM
2. Map memory regions
3. CPU fetches instruction
4. Decode opcode
5. Execute instruction
6. Update flags
7. Advance clock cycle
8. Update timer / PPU / interrupts
```

## Why This Is a Great Learning Project

This kind of project combines many important areas of computer science:

- computer architecture
- digital logic
- embedded systems
- Java development
- modular software design
- testing and debugging
- technical documentation reading

It also teaches you to think in terms of:

- bits
- bytes
- states
- cycles
- timing
- hardware and memory behavior

This deepens your understanding of how computing systems really work.

## Project Status

This repository is currently in active development and is intended as a practical learning project. That means some parts may still be incomplete or simplified, especially in areas such as:

- PPU emulation
- MMU behavior
- timer accuracy
- interrupt handling
- cartridge support
- compatibility with real ROMs

This limitation is part of the learning process. The goal is to understand, test, and improve the implementation step by step.

## Suggested Learning Roadmap

1. Implement the basic CPU
2. Add registers and flags
3. Map memory and ROM access
4. Implement load and control instructions
5. Run simple opcode tests
6. Add interrupts
7. Study and implement PPU timing
8. Add timer and input handling
9. Improve cartridge compatibility
10. Debug and validate behavior against expected hardware semantics

## Conclusion

A Game Boy emulator is an excellent study project because it brings together many essential ideas in computing in one concrete, visual application. It teaches more than Java programming: it teaches how a computing system works from the inside.

If you want to build a project like this, the best approach is to break it down into small modules, test each part in isolation, and evolve the system as your understanding of the hardware improves.

---

This README is designed to explain the educational value of the project and the concepts it covers.
