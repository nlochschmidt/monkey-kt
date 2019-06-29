# monkey-kt
Writing an Interpreter in ~Go~ Kotlin


## Install Kotlin/Native compiler

```
brew cask install kotlin-native
```

## Test

Run tests once:

```
make test
````

Watch source folder and run tests on change

```
watch "make test" ./src
```

## Compile

```
make
```

## Run

```
./monkey.kexe
```