# Compilador
CC = g++
CC_FLAGS = -std=c++0x -lm -Wall
L_FLAGS = 

# Arquivos e Argumentos
EXEC = newshell
SOURCES = $(wildcard *.cpp)
OBJECTS = $(SOURCES:.cpp=.o)
RUNARGS = 

$(EXEC): $(OBJECTS)
	$(CC) $(OBJECTS) -o $(EXEC) $(L_FLAGS)

run: $(EXEC)
	@echo "O uso de make run não é recomendado uma vez que interefere no tratamento de sinais!"
	./$(EXEC) $(RUNARGS)

%.o: %.cpp
	$(CC) -c $(CC_FLAGS) $(FLAGS) $< -o $@

clean:
	rm -f $(EXEC) $(OBJECTS) 
