#!/usr/bin/env groovy

import fun.aipark.devops.MathArithmetic

int add(int a, int b) {
//    List<String> x = ["a"]
//    x.add("test")
//    println(x)
    return new MathArithmetic(script: this).add(a, b)
}

return this
