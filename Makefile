TARGET		:= monkey
MAIN_DIR	:= src/commonMain/kotlin/
DEPS		:= $(wildcard $(MAIN_DIR)*/*.kt)
KEXE		:= $(TARGET).kexe

TEST_DIR	:= src/commonTest/kotlin/
TESTS		:= $(wildcard $(TEST_DIR)*.kt $(TEST_DIR)*/*.kt)

.PHONY: all clean test

%.kexe : $(MAIN_DIR)%.kt $(DEPS)
	kotlinc-native -Werror -progressive -o $@ $^ 

test.kexe: $(TESTS) $(DEPS)
	kotlinc-native -tr -Werror -progressive -o $@ $^

test: test.kexe
	@./$<

clean:
	rm -f $(KEXE)

all : $(KEXE)
