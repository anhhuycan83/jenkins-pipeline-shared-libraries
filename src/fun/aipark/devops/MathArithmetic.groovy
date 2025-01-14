#!/usr/bin/env groovy

package fun.aipark.devops

/**
 * @author LDC
 */
class MathArithmetic implements Serializable {

    Object script

    MathArithmetic(script) {
        this.script = script
    }

    int add(int a, int b) {
        echo "Adding ${a} to ${b} = ${a + b}"
        return a + b
    }

}
