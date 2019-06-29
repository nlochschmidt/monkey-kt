TARGET		:= monkey
MAIN_DIR	:= src/commonMain/kotlin/
DEPS			:= $(wildcard $(MAIN_DIR)*/*.kt)
KEXE			:= $(TARGET).kexe 

.PHONY: all clean

%.kexe : $(MAIN_DIR)%.kt $(DEPS)
	@echo $(DEPS)
	kotlinc-native -Werror -progressive -o $@ $^ 

clean:
	rm -f $(KEXE)

all : $(KEXE)