# Java Game Boy Emulator

Este projeto é um emulador de Game Boy em Java, desenvolvido como estudo de arquitetura de computadores, CPU, memória, periféricos e emulação de hardware. O objetivo principal é aprender como um console antigo funciona internamente, em vez de apenas usar um software pronto.

O projeto foi construído para estudar:

- a CPU do Game Boy
- a memória e o mapeamento de endereços
- interrupções e ciclos de máquina
- renderização e PPU
- timers e sincronização
- cartuchos e ROMs
- debugging em nível de hardware

## Objetivo do projeto

O foco não é entregar um emulador perfeito, mas sim desenvolver uma base realista para entender como um Game Boy funciona por dentro. Esse tipo de projeto ajuda a compreender conceitos de baixo nível que muitas vezes ficam invisíveis em aplicações de alto nível.

## O que você aprende com um projeto como este

### 1. Arquitetura de CPU

Você aprende como a CPU:

- lê instruções da memória
- atualiza o Program Counter (PC)
- altera registradores
- manipula flags
- executa operações aritméticas, lógicas e de transferência
- faz saltos, chamadas e retornos

No Game Boy, a CPU é uma variação do Z80. Isso permite estudar conceitos essenciais como:

- registradores de 8 bits e 16 bits
- endereçamento de memória
- instruções condicionais
- stack
- flags Zero, Carry, Half Carry e Subtract
- ciclos de instrução

### 2. Organização da memória

O emulador precisa modelar a memória do Game Boy com precisão suficiente para reproduzir o comportamento do hardware:

- ROM do cartucho
- RAM interna
- VRAM
- WRAM
- memória de I/O
- mapeamento de endereços especiais
- áreas de controle e periféricos

Você passa a entender que a memória não é um espaço "genérico": ela é organizada por região, e cada região tem um papel específico.

### 3. Interrupções

As interrupções são fundamentais em qualquer sistema embarcado. No Game Boy, elas podem acontecer por:

- VBlank
- LCD STAT
- Timer
- Serial
- Joypad

Aqui você aprende:

- como o processador identifica interrupções pendentes
- como ele salva o estado atual
- como o fluxo da CPU é redirecionado para um vetor específico
- como o mecanismo IME/DI/EI funciona

### 4. PPU e renderização

A PPU (Picture Processing Unit) é responsável por desenhar o conteúdo da tela. Nesse estudo, você aprende:

- linhas de varredura
- frames
- HBlank e VBlank
- sprites e tiles
- background e window
- prioridade visual
- timing de renderização

Este é um dos pontos mais importantes do projeto, porque a tela do Game Boy é muito mais do que um buffer simples: ela é um sistema sincronizado com o processador e com o relógio do console.

### 5. Timing e ciclos de máquina

No hardware real, tudo acontece em tempo. Cada instrução precisa de um número de ciclos, e a PPU, o timer e a CPU precisam cooperar. Isso ensina a importância de:

- ciclos de máquina
- sincronização de componentes
- temporização em sistemas embarcados
- debugging baseado em estados e timeline

### 6. Cartuchos e ROMs

Os cartuchos não são apenas arquivos binários; eles possuem vários tipos de mapeamento e comportamento. Ao estudar isso, você entende:

- como a ROM é lida
- como o endereço do cartucho é mapeado
- como a RAM externa funciona
- como diferentes tipos de cartuchos mudam o acesso à memória

### 7. Debugging de baixo nível

Projeto de emulação exige muita observação do estado interno do sistema. Você aprende a depurar:

- valores de registradores
- flags
- PC e SP
- memória em endereços específicos
- instruções não implementadas
- comportamento estranho em condicional e loops

Essa prática melhora muito a habilidade de encontrar bugs em software real.

### 8. Leitura de especificações e documentação técnica

Emuladores reais dependem de documentação técnica. Esse projeto força você a ler e interpretar:

- tabelas de instrução
- descrições de hardware
- diagramas de memória
- regras de interrupção
- comportamento de PPU e timer

Isso ajuda a desenvolver a capacidade de transformar especificações em código funcional.

## Estrutura do projeto

A estrutura básica do projeto costuma ser organizada em módulos, como:

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

### Fluxo típico de execução

```text
1. Carregar ROM
2. Mapeamento de memória
3. CPU busca instrução
4. Decodificação do opcode
5. Execução da instrução
6. Atualização de flags
7. Progressão do clock
8. Atualização de timer/PPU/interrupções
```

## Por que é um bom projeto para estudo

Este tipo de projeto combina diversas áreas do conhecimento:

- arquitetura de computadores
- lógica digital
- sistemas embarcados
- desenvolvimento em Java
- organização de software em módulos
- debug e testes
- leitura de documentação técnica

Além disso, ele ensina a pensar em termos de:

- bits
- bytes
- estados
- ciclos
- timing
- hardware e memória

Isso aumenta muito a compreensão de como sistemas computacionais realmente funcionam.

## Estado do projeto

Este repositório está em desenvolvimento e foi pensado como estudo prático. Isso significa que ele pode estar incompleto em algumas partes, em especial em áreas como:

- PPU
- MMU
- timer
- interrupts
- cartuchos com MBC
- compatibilidade com ROMs reais

Essa limitação faz parte do processo de aprendizagem. O objetivo central é aprender, testar e evoluir a implementação aos poucos.

## Roadmap de aprendizado sugerido

1. Implementar a CPU básica
2. Adicionar registradores e flags
3. Mapear memória e ROM
4. Implementar instruções de carga e controle
5. Executar testes simples de opcode
6. Adicionar interrupções
7. Estudar PPU e timing
8. Implementar timer e entrada
9. Melhorar a compatibilidade com cartuchos
10. Depurar e validar comportamento real

## Conclusão

Um emulador de Game Boy é um projeto excelente para estudo porque reúne muitos conceitos importantes de computação em uma aplicação concreta e visual. Ele ensina mais do que programação em Java: ensina como um sistema computacional funciona por dentro.

Se você quiser desenvolver um projeto como esse, não precisa começar do zero no mundo inteiro. O caminho mais eficiente é quebrar em módulos pequenos, testar cada parte isoladamente e evoluir junto com o entendimento do hardware.

---

Este README foi feito para ajudar outros estudantes a entender o valor didático do projeto e os conceitos que ele aborda.
