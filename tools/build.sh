#!/bin/bash

# run from the root of the project

javac -d build tools/GenerateAST.java
pushd build
java tools.GenerateAST ../jlox/
popd
javac -d build jlox/Lox.java